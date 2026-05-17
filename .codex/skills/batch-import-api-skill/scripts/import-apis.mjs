import fs from 'node:fs/promises';
import path from 'node:path';
import process from 'node:process';

const SUPPORTED_AUTH_SCHEMES = new Set(['NONE', 'HEADER_TOKEN', 'QUERY_TOKEN']);

function parseArgs(argv) {
  const args = { dryRun: false };
  for (let index = 0; index < argv.length; index += 1) {
    const token = argv[index];
    if (token === '--config') {
      args.config = argv[index + 1];
      index += 1;
    } else if (token === '--output-dir') {
      args.outputDir = argv[index + 1];
      index += 1;
    } else if (token === '--dry-run') {
      args.dryRun = true;
    } else if (token === '--help' || token === '-h') {
      args.help = true;
    } else {
      throw new Error(`Unknown argument: ${token}`);
    }
  }
  return args;
}

function printHelp() {
  console.log([
    'Usage: node .codex/skills/batch-import-api-skill/scripts/import-apis.mjs --config <config.json> [--output-dir <dir>] [--dry-run]',
    '',
    'Options:',
    '  --config      Import config JSON path',
    '  --output-dir  Directory for generated markdown process documents',
    '  --dry-run     Do not send requests, only generate plan and process document'
  ].join('\n'));
}

function nowIso() {
  return new Date().toISOString();
}

function timestampForFile() {
  return nowIso().replace(/[:]/g, '-').replace(/\.\d{3}Z$/, 'Z');
}

function ensureArray(value, label) {
  if (value == null) {
    return [];
  }
  if (!Array.isArray(value)) {
    throw new Error(`${label} must be an array`);
  }
  return value;
}

function isPlainObject(value) {
  return Boolean(value) && typeof value === 'object' && !Array.isArray(value);
}

function pickDefined(source, fields) {
  const target = {};
  for (const field of fields) {
    if (source[field] !== undefined) {
      target[field] = source[field];
    }
  }
  return target;
}

function normalizeCategoryCode(categoryCode) {
  if (typeof categoryCode !== 'string' || !categoryCode.trim()) {
    return undefined;
  }
  return categoryCode.trim().toLowerCase();
}

function resolveAssetType(api) {
  if (typeof api.assetType === 'string' && api.assetType.trim()) {
    return api.assetType.trim();
  }
  throw new Error(`assetType is required for api ${api.apiCode || '<unknown>'}`);
}

function normalizeAuthScheme(authScheme, apiCode) {
  if (authScheme == null) {
    return undefined;
  }
  if (typeof authScheme !== 'string' || !authScheme.trim()) {
    throw new Error(`authScheme must be a non-empty string for api ${apiCode || '<unknown>'}`);
  }
  const normalized = authScheme.trim().toUpperCase();
  if (!SUPPORTED_AUTH_SCHEMES.has(normalized)) {
    throw new Error(`authScheme ${authScheme} is invalid for api ${apiCode || '<unknown>'}. Use NONE, HEADER_TOKEN or QUERY_TOKEN.`);
  }
  return normalized;
}

function resolveEnvPlaceholders(value, label) {
  return value.replace(/\$\{env:([A-Za-z_][A-Za-z0-9_]*)\}/g, (_, envName) => {
    const envValue = process.env[envName];
    if (!envValue) {
      throw new Error(`${label} references env ${envName}, but it is empty or undefined`);
    }
    return envValue;
  });
}

function resolveAuthConfig(api) {
  const apiLabel = api.apiCode || '<unknown>';
  if (!api.authScheme) {
    return undefined;
  }
  if (api.authScheme === 'NONE') {
    return undefined;
  }
  if (typeof api.authConfig !== 'string' || !api.authConfig.trim()) {
    throw new Error(`authConfig is required for api ${apiLabel} when authScheme is ${api.authScheme}`);
  }

  const trimmed = api.authConfig.trim();
  if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
    throw new Error(
      `authConfig for api ${apiLabel} must be a plain string, not JSON. `
      + `HEADER_TOKEN use \"Authorization: Bearer \${env:TOKEN_ENV}\"; `
      + `QUERY_TOKEN use \"key=\${env:TOKEN_ENV}\".`
    );
  }

  const resolved = resolveEnvPlaceholders(trimmed, `authConfig for api ${apiLabel}`);
  if (/\r|\n/.test(resolved)) {
    throw new Error(`authConfig for api ${apiLabel} must stay on a single line`);
  }
  return resolved;
}

function normalizeJsonSchemaSnapshot(value, label) {
  if (value === undefined) {
    return undefined;
  }
  if (value === null) {
    return null;
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    return trimmed ? trimmed : null;
  }
  if (typeof value === 'boolean' || isPlainObject(value)) {
    return JSON.stringify(value);
  }
  throw new Error(`${label} must be a JSON string, JSON object, boolean schema, null, or omitted`);
}

function maskSecret(value) {
  if (!value) {
    return '<empty>';
  }
  if (value.length <= 8) {
    return `${'*'.repeat(Math.max(0, value.length - 2))}${value.slice(-2)}`;
  }
  return `${value.slice(0, 4)}${'*'.repeat(Math.max(0, value.length - 8))}${value.slice(-4)}`;
}

function getByPath(source, pathExpression) {
  const segments = pathExpression.split('.');
  let current = source;
  for (const segment of segments) {
    if (!current || typeof current !== 'object' || !(segment in current)) {
      return undefined;
    }
    current = current[segment];
  }
  return current;
}

function extractFirstString(source, candidates) {
  for (const candidate of candidates) {
    const value = getByPath(source, candidate);
    if (typeof value === 'string' && value.trim()) {
      return value.trim();
    }
  }
  return undefined;
}

function normalizeUrl(baseUrl, relativePath) {
  const trimmedBase = baseUrl.replace(/\/+$/, '');
  const trimmedPath = relativePath.startsWith('/') ? relativePath : `/${relativePath}`;
  return `${trimmedBase}${trimmedPath}`;
}

async function readConfig(configPath) {
  const raw = await fs.readFile(configPath, 'utf8');
  const parsed = JSON.parse(raw);
  if (!parsed.baseUrl || typeof parsed.baseUrl !== 'string') {
    throw new Error('baseUrl is required');
  }
  if (!isPlainObject(parsed.managementAuth)) {
    throw new Error('managementAuth is required');
  }
  if (!Array.isArray(parsed.apis) || parsed.apis.length === 0) {
    throw new Error('apis must be a non-empty array');
  }
  return parsed;
}

function resolveSecret(configValue, envName, label) {
  if (typeof configValue === 'string' && configValue.trim()) {
    return configValue.trim();
  }
  if (typeof envName === 'string' && envName.trim()) {
    const value = process.env[envName.trim()];
    if (!value) {
      throw new Error(`${label} env ${envName} is empty or undefined`);
    }
    return value;
  }
  throw new Error(`${label} is required`);
}

function buildManagementHeaders(managementAuth) {
  const apiKey = resolveSecret(managementAuth.apiKey, managementAuth.apiKeyEnv, 'managementAuth.apiKey');
  const headerName = managementAuth.headerName || 'Authorization';
  const prefix = managementAuth.prefix ?? 'Bearer';
  const headerValue = prefix ? `${prefix} ${apiKey}`.trim() : apiKey;
  return {
    headers: {
      [headerName]: headerValue
    },
    headerName,
    maskedValue: maskSecret(headerValue)
  };
}

function mergeApiConfig(defaults, api) {
  const merged = {
    ...defaults,
    ...api,
    aiProfile: api.aiProfile,
    proxyBinding: api.proxyBinding,
    exampleAccess: api.exampleAccess
  };
  merged.categoryCode = normalizeCategoryCode(merged.categoryCode);
  merged.assetType = resolveAssetType(merged);
  merged.authScheme = normalizeAuthScheme(merged.authScheme, merged.apiCode);
  merged.authConfig = resolveAuthConfig(merged);
  if (Object.prototype.hasOwnProperty.call(merged, 'requestJsonSchema')) {
    merged.requestJsonSchema = normalizeJsonSchemaSnapshot(
      merged.requestJsonSchema,
      `requestJsonSchema for api ${merged.apiCode || '<unknown>'}`
    );
  }
  if (Object.prototype.hasOwnProperty.call(merged, 'responseJsonSchema')) {
    merged.responseJsonSchema = normalizeJsonSchemaSnapshot(
      merged.responseJsonSchema,
      `responseJsonSchema for api ${merged.apiCode || '<unknown>'}`
    );
  }
  return merged;
}

async function parseResponse(response) {
  const text = await response.text();
  const contentType = response.headers.get('content-type') || '';
  if (contentType.includes('application/json')) {
    try {
      return { text, data: text ? JSON.parse(text) : null };
    } catch {
      return { text, data: null };
    }
  }
  return { text, data: null };
}

function appendRecord(records, entry) {
  records.push({
    timestamp: nowIso(),
    ...entry
  });
}

function summarizeResult(result) {
  const sanitized = sanitizeForLog(result);
  if (sanitized == null) {
    return '';
  }
  if (typeof sanitized === 'string') {
    return sanitized.length > 160 ? `${sanitized.slice(0, 157)}...` : sanitized;
  }
  const serialized = JSON.stringify(sanitized);
  return serialized.length > 160 ? `${serialized.slice(0, 157)}...` : serialized;
}

function sanitizeForLog(value) {
  if (Array.isArray(value)) {
    return value.map((item) => sanitizeForLog(item));
  }
  if (isPlainObject(value)) {
    const sanitized = {};
    for (const [key, nestedValue] of Object.entries(value)) {
      sanitized[key] = key === 'authConfig' ? '[REDACTED_AUTH_CONFIG]' : sanitizeForLog(nestedValue);
    }
    return sanitized;
  }
  return value;
}

function isExistingAssetConflict(result) {
  const payload = unwrapData(result?.data);
  const code = extractFirstString(payload, ['code']) || extractFirstString(result?.data, ['code']);
  const message = extractFirstString(payload, ['message']) || extractFirstString(result?.data, ['message']) || result?.text;
  return code === 'ASSET_PUBLISH_INCOMPLETE' && typeof message === 'string' && message.includes('Asset update conflict:');
}

async function requestJson(context, method, relativePath, { body, allowedStatusCodes = [] } = {}) {
  const url = normalizeUrl(context.baseUrl, relativePath);
  if (context.dryRun) {
    appendRecord(context.records, {
      step: 'dry-run',
      resource: relativePath,
      method,
      status: 'SKIPPED',
      httpStatus: '-',
      notes: summarizeResult(body)
    });
    return { ok: true, status: 0, data: { dryRun: true }, text: '' };
  }

  const headers = {
    Accept: 'application/json',
    ...context.managementHeaders.headers
  };
  let payload;
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const response = await fetch(url, {
    method,
    headers,
    body: payload
  });
  const parsed = await parseResponse(response);
  const ok = response.ok || allowedStatusCodes.includes(response.status);
  return {
    ok,
    status: response.status,
    data: parsed.data,
    text: parsed.text,
    url
  };
}

function unwrapData(responseData) {
  if (isPlainObject(responseData) && 'data' in responseData) {
    return responseData.data;
  }
  return responseData;
}

function collectItems(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }
  const unwrapped = unwrapData(payload);
  if (Array.isArray(unwrapped)) {
    return unwrapped;
  }
  if (isPlainObject(unwrapped)) {
    const itemCandidates = [unwrapped.items, unwrapped.records, unwrapped.list, unwrapped.content];
    for (const candidate of itemCandidates) {
      if (Array.isArray(candidate)) {
        return candidate;
      }
    }
  }
  return [];
}

async function ensureCategory(context, category) {
  const categoryCode = category.categoryCode;
  appendRecord(context.records, {
    step: 'ensure-category',
    resource: categoryCode,
    method: 'GET',
    status: 'STARTED',
    httpStatus: '-',
    notes: 'Check category existence'
  });

  const getResult = await requestJson(context, 'GET', `/api/v1/categories/${encodeURIComponent(categoryCode)}`, {
    allowedStatusCodes: [404]
  });
  const exists = getResult.status !== 404;
  appendRecord(context.records, {
    step: 'ensure-category',
    resource: categoryCode,
    method: 'GET',
    status: exists ? 'SUCCESS' : 'MISS',
    httpStatus: String(getResult.status || '-'),
    notes: exists ? 'Category exists' : 'Category not found'
  });

  if (!exists) {
    const createBody = pickDefined(category, ['categoryCode', 'categoryName']);
    const createResult = await requestJson(context, 'POST', '/api/v1/categories', { body: createBody });
    appendRecord(context.records, {
      step: 'create-category',
      resource: categoryCode,
      method: 'POST',
      status: createResult.ok ? 'SUCCESS' : 'FAILED',
      httpStatus: String(createResult.status || '-'),
      notes: summarizeResult(createResult.data || createResult.text)
    });
    if (!createResult.ok) {
      throw new Error(`Failed to create category ${categoryCode}`);
    }
  }

  if (category.enableAfterEnsure !== false && context.defaults.ensureCategoryEnabled !== false) {
    const enableResult = await requestJson(context, 'PATCH', `/api/v1/categories/${encodeURIComponent(categoryCode)}/enable`, {
      allowedStatusCodes: [400, 404, 409]
    });
    appendRecord(context.records, {
      step: 'enable-category',
      resource: categoryCode,
      method: 'PATCH',
      status: enableResult.ok ? 'SUCCESS' : 'WARNING',
      httpStatus: String(enableResult.status || '-'),
      notes: summarizeResult(enableResult.data || enableResult.text || 'Enable attempt finished')
    });
  }
}

async function resolveProfileIdByCode(context, profileCode) {
  const query = encodeURIComponent(profileCode);
  const listResult = await requestJson(context, 'GET', `/api/v1/platform/proxy-profiles?keyword=${query}&page=1&size=20`, {
    allowedStatusCodes: [404]
  });
  appendRecord(context.records, {
    step: 'search-proxy-profile',
    resource: profileCode,
    method: 'GET',
    status: listResult.ok ? 'SUCCESS' : 'FAILED',
    httpStatus: String(listResult.status || '-'),
    notes: 'Search profile by keyword'
  });
  if (!listResult.ok) {
    return undefined;
  }
  const items = collectItems(listResult.data);
  const matched = items.find((item) => {
    const code = extractFirstString(item, ['profileCode', 'code']);
    return code === profileCode;
  });
  if (!matched) {
    return undefined;
  }
  return extractFirstString(matched, ['profileId', 'id', 'data.profileId', 'data.id']);
}

async function ensureProxyProfiles(context, proxyProfiles) {
  for (const profile of proxyProfiles) {
    if (context.dryRun) {
      const syntheticProfileId = profile.profileId || `dry-run-${profile.profileCode || 'profile'}`;
      if (profile.profileCode) {
        context.profileIdsByCode.set(profile.profileCode, syntheticProfileId);
      }
      appendRecord(context.records, {
        step: 'ensure-proxy-profile',
        resource: profile.profileCode || syntheticProfileId,
        method: 'POST',
        status: 'SKIPPED',
        httpStatus: '-',
        notes: `Dry-run synthetic profileId: ${syntheticProfileId}`
      });
      continue;
    }
    if (profile.profileId) {
      context.profileIdsByCode.set(profile.profileCode, profile.profileId);
      continue;
    }
    if (profile.createIfMissing === false) {
      const existingId = await resolveProfileIdByCode(context, profile.profileCode);
      if (!existingId) {
        throw new Error(`profileId for ${profile.profileCode} is required when createIfMissing is false`);
      }
      context.profileIdsByCode.set(profile.profileCode, existingId);
      continue;
    }
    const createBody = pickDefined(profile, [
      'profileCode',
      'profileName',
      'proxyType',
      'proxyHost',
      'proxyPort',
      'username',
      'password',
      'enabled'
    ]);
    const createResult = await requestJson(context, 'POST', '/api/v1/platform/proxy-profiles', {
      body: createBody,
      allowedStatusCodes: [409]
    });
    appendRecord(context.records, {
      step: 'ensure-proxy-profile',
      resource: profile.profileCode,
      method: 'POST',
      status: createResult.ok ? 'SUCCESS' : 'FAILED',
      httpStatus: String(createResult.status || '-'),
      notes: summarizeResult(createResult.data || createResult.text)
    });
    if (!createResult.ok && createResult.status !== 409) {
      throw new Error(`Failed to create proxy profile ${profile.profileCode}`);
    }
    const profileId = extractFirstString(createResult.data, ['profileId', 'id', 'data.profileId', 'data.id'])
      || await resolveProfileIdByCode(context, profile.profileCode);
    if (!profileId) {
      throw new Error(`Cannot resolve profileId for ${profile.profileCode}`);
    }
    context.profileIdsByCode.set(profile.profileCode, profileId);
  }
}

async function registerAsset(context, api) {
  const registerBody = pickDefined(api, [
    'apiCode',
    'assetType',
    'assetName',
    'requestJsonSchema',
    'responseJsonSchema'
  ]);
  const registerResult = await requestJson(context, 'POST', '/api/v1/current-user/assets', {
    body: registerBody,
    allowedStatusCodes: [409, 400]
  });
  const existingConflict = registerResult.status === 400 && isExistingAssetConflict(registerResult);
  appendRecord(context.records, {
    step: 'register-asset',
    resource: api.apiCode,
    method: 'POST',
    status: registerResult.ok ? ((registerResult.status === 409 || existingConflict) ? 'EXISTS' : 'SUCCESS') : 'FAILED',
    httpStatus: String(registerResult.status || '-'),
    notes: summarizeResult(registerResult.data || registerResult.text)
  });
  if (!registerResult.ok && !existingConflict) {
    throw new Error(`Failed to register asset ${api.apiCode}`);
  }
}

async function reviseAsset(context, api) {
  const reviseBody = pickDefined(api, [
    'assetName',
    'assetType',
    'categoryCode',
    'requestMethod',
    'upstreamUrl',
    'authScheme',
    'authConfig',
    'requestTemplate',
    'requestExample',
    'responseExample',
    'requestJsonSchema',
    'responseJsonSchema',
    'asyncTaskConfig'
  ]);
  const hasFields = Object.keys(reviseBody).length > 0;
  if (!hasFields) {
    return;
  }
  const reviseResult = await requestJson(context, 'PUT', `/api/v1/current-user/assets/${encodeURIComponent(api.apiCode)}`, {
    body: reviseBody
  });
  appendRecord(context.records, {
    step: 'revise-asset',
    resource: api.apiCode,
    method: 'PUT',
    status: reviseResult.ok ? 'SUCCESS' : 'FAILED',
    httpStatus: String(reviseResult.status || '-'),
    notes: summarizeResult(reviseResult.data || reviseResult.text)
  });
  if (!reviseResult.ok) {
    throw new Error(`Failed to revise asset ${api.apiCode}`);
  }
}

async function attachAiProfile(context, api) {
  if (!isPlainObject(api.aiProfile)) {
    return;
  }
  const attachResult = await requestJson(context, 'PUT', `/api/v1/current-user/assets/${encodeURIComponent(api.apiCode)}/ai-profile`, {
    body: api.aiProfile
  });
  appendRecord(context.records, {
    step: 'attach-ai-profile',
    resource: api.apiCode,
    method: 'PUT',
    status: attachResult.ok ? 'SUCCESS' : 'FAILED',
    httpStatus: String(attachResult.status || '-'),
    notes: summarizeResult(attachResult.data || attachResult.text)
  });
  if (!attachResult.ok) {
    throw new Error(`Failed to attach AI profile for ${api.apiCode}`);
  }
}

async function bindProxyProfile(context, api) {
  if (!isPlainObject(api.proxyBinding)) {
    return;
  }
  const profileId = api.proxyBinding.profileId || context.profileIdsByCode.get(api.proxyBinding.profileCode);
  if (!profileId) {
    throw new Error(`proxyBinding.profileId or resolvable profileCode is required for ${api.apiCode}`);
  }
  const bindResult = await requestJson(context, 'PUT', `/api/v1/platform/proxy-profiles/asset-bindings/${encodeURIComponent(api.apiCode)}`, {
    body: { profileId }
  });
  appendRecord(context.records, {
    step: 'bind-proxy-profile',
    resource: api.apiCode,
    method: 'PUT',
    status: bindResult.ok ? 'SUCCESS' : 'FAILED',
    httpStatus: String(bindResult.status || '-'),
    notes: summarizeResult(bindResult.data || bindResult.text)
  });
  if (!bindResult.ok) {
    throw new Error(`Failed to bind proxy profile for ${api.apiCode}`);
  }
}

async function publishAsset(context, api) {
  if (api.publish === false) {
    return;
  }
  const publishResult = await requestJson(context, 'PATCH', `/api/v1/current-user/assets/${encodeURIComponent(api.apiCode)}/publish`);
  appendRecord(context.records, {
    step: 'publish-asset',
    resource: api.apiCode,
    method: 'PATCH',
    status: publishResult.ok ? 'SUCCESS' : 'FAILED',
    httpStatus: String(publishResult.status || '-'),
    notes: summarizeResult(publishResult.data || publishResult.text)
  });
  if (!publishResult.ok) {
    throw new Error(`Failed to publish asset ${api.apiCode}`);
  }
}

function parseExampleBody(exampleValue) {
  if (exampleValue == null) {
    return undefined;
  }
  if (typeof exampleValue === 'string') {
    try {
      return JSON.parse(exampleValue);
    } catch {
      return exampleValue;
    }
  }
  return exampleValue;
}

function buildAccessExample(config, generatedAccessKey) {
  const firstApi = config.apis[0];
  if (!firstApi) {
    return null;
  }
  const exampleAccess = firstApi.exampleAccess || {};
  const method = exampleAccess.method || firstApi.requestMethod || 'POST';
  const body = parseExampleBody(exampleAccess.body ?? firstApi.requestExample) ?? { ping: 'pong' };
  let apiKey = generatedAccessKey;
  if (!apiKey && config.exampleAccess) {
    apiKey = config.exampleAccess.apiKey;
    if (!apiKey && config.exampleAccess.apiKeyEnv) {
      apiKey = process.env[config.exampleAccess.apiKeyEnv];
    }
  }
  const renderedKey = apiKey || '<请填入可用 X-Aether-Api-Key>';
  const contentType = config.exampleAccess?.contentType || 'application/json';
  return [
    `curl -X ${method.toUpperCase()} "${normalizeUrl(config.baseUrl, `/api/v1/access/${firstApi.apiCode}`)}" \\`,
    `  -H "X-Aether-Api-Key: ${renderedKey}" \\`,
    `  -H "Content-Type: ${contentType}" \\`,
    `  -d '${JSON.stringify(body, null, 2)}'`
  ].join('\n');
}

function buildTaskQueryExamples(config, generatedAccessKey) {
  const asyncApis = config.apis.filter((api) => isPlainObject(api.asyncTaskConfig) && api.asyncTaskConfig.enabled !== false);
  if (asyncApis.length === 0) {
    return 'No async task query examples.';
  }
  let apiKey = generatedAccessKey;
  if (!apiKey && config.exampleAccess) {
    apiKey = config.exampleAccess.apiKey;
    if (!apiKey && config.exampleAccess.apiKeyEnv) {
      apiKey = process.env[config.exampleAccess.apiKeyEnv];
    }
  }
  const renderedKey = apiKey || '<please-fill-X-Aether-Api-Key>';
  return asyncApis.map((api) => {
    const exampleTaskId = api.asyncTaskConfig.exampleTaskId || 'task-id-from-submit-response';
    return [
      `# ${api.apiCode}`,
      `curl -X GET "${normalizeUrl(config.baseUrl, `/api/v1/access/${api.apiCode}/tasks/${exampleTaskId}`)}" \\`,
      `  -H "X-Aether-Api-Key: ${renderedKey}"`
    ].join('\n');
  }).join('\n\n');
}

function buildInterfaceChecklist(usedProxy, usedAi, usedAsync) {
  const lines = [
    '| 接口 | 方法 | 用途 |',
    '| --- | --- | --- |',
    '| /api/v1/categories/{categoryCode} | GET | 检查分类是否已存在 |',
    '| /api/v1/categories | POST | 创建分类 |',
    '| /api/v1/categories/{categoryCode}/enable | PATCH | 启用分类，确保可被资产引用 |',
    '| /api/v1/current-user/assets | POST | 创建 API 资产草稿 |',
    '| /api/v1/current-user/assets/{apiCode} | PUT | 修订资产详情与上游配置 |',
    '| /api/v1/current-user/assets/{apiCode}/publish | PATCH | 发布资产 |',
    '| /api/v1/access/{apiCode} | 任意 HTTP 方法 | 统一接入示例调用 |'
  ];
  if (usedAi) {
    lines.splice(lines.length - 1, 0, '| /api/v1/current-user/assets/{apiCode}/ai-profile | PUT | 绑定 AI 能力档案 |');
  }
  if (usedProxy) {
    lines.splice(lines.length - 1, 0,
      '| /api/v1/platform/proxy-profiles | POST | 创建平台代理配置 |',
      '| /api/v1/platform/proxy-profiles?keyword=... | GET | 通过 profileCode 解析 profileId |',
      '| /api/v1/platform/proxy-profiles/asset-bindings/{apiCode} | PUT | 给资产绑定代理配置 |'
    );
  }
  if (usedAsync) {
    lines.push('| /api/v1/access/{apiCode}/tasks/{taskId} | GET | Unified Access async task query |');
  }
  return lines.join('\n');
}

function buildExecutionTable(records) {
  const lines = [
    '| 时间 | 步骤 | 资源 | 方法 | 状态 | HTTP 状态码 | 备注 |',
    '| --- | --- | --- | --- | --- | --- | --- |'
  ];
  for (const record of records) {
    lines.push(`| ${record.timestamp} | ${record.step} | ${record.resource} | ${record.method} | ${record.status} | ${record.httpStatus} | ${String(record.notes || '').replace(/\|/g, '\\|')} |`);
  }
  return lines.join('\n');
}

function buildFailures(records) {
  const failed = records.filter((record) => record.status === 'FAILED');
  if (failed.length === 0) {
    return '无失败项。';
  }
  return failed.map((record) => `- ${record.step} / ${record.resource}: ${record.notes || '请检查请求参数、鉴权 header 和接口顺序。'}`).join('\n');
}

async function writeOutputs(context, config, generatedAccessKey, errors) {
  const fileStamp = timestampForFile();
  const outputDir = context.outputDir;
  await fs.mkdir(outputDir, { recursive: true });
  const markdownPath = path.join(outputDir, `api-import-run-${fileStamp}.md`);
  const jsonPath = path.join(outputDir, `api-import-run-${fileStamp}.json`);

  const usedProxy = ensureArray(config.proxyProfiles, 'proxyProfiles').length > 0
    || config.apis.some((api) => isPlainObject(api.proxyBinding));
  const usedAi = config.apis.some((api) => isPlainObject(api.aiProfile));
  const usedAsync = config.apis.some((api) => isPlainObject(api.asyncTaskConfig) && api.asyncTaskConfig.enabled !== false);
  const successCount = new Set(context.records.filter((record) => record.step === 'publish-asset' && record.status === 'SUCCESS').map((record) => record.resource)).size
    || new Set(context.records.filter((record) => record.step === 'register-asset' && ['SUCCESS', 'EXISTS'].includes(record.status)).map((record) => record.resource)).size;
  const summary = [
    '# API 批量导入过程文档',
    '',
    '## 导入概览',
    '',
    `- 执行时间：${nowIso()}`,
    `- 后端地址：${config.baseUrl}`,
    `- 管理鉴权头：${context.managementHeaders.headerName}`,
    `- 管理鉴权值：${context.managementHeaders.maskedValue}`,
    `- 计划导入 API 数量：${config.apis.length}`,
    `- 成功处理 API 数量：${successCount}`,
    `- 失败记录数：${errors.length}`,
    '',
    '## 使用到的后端接口',
    '',
    buildInterfaceChecklist(usedProxy, usedAi, usedAsync),
    '',
    '## 接入示例',
    '',
    '```bash',
    buildAccessExample(config, generatedAccessKey) || '暂无可生成示例',
    '```',
    '',
    '## 任务执行记录',
    '',
    buildExecutionTable(context.records),
    '',
    '## 失败项与重试建议',
    '',
    buildFailures(context.records)
  ].join('\n');

  await fs.writeFile(markdownPath, `${summary}\n`, 'utf8');
  await fs.writeFile(jsonPath, JSON.stringify({
    generatedAt: nowIso(),
    baseUrl: config.baseUrl,
    managementHeaderName: context.managementHeaders.headerName,
    records: context.records,
    errors
  }, null, 2), 'utf8');
  return { markdownPath, jsonPath };
}

async function run() {
  const args = parseArgs(process.argv.slice(2));
  if (args.help || !args.config) {
    printHelp();
    return;
  }

  const config = await readConfig(path.resolve(args.config));
  const defaults = isPlainObject(config.defaults) ? config.defaults : {};
  const managementHeaders = buildManagementHeaders(config.managementAuth);
  const context = {
    baseUrl: config.baseUrl,
    dryRun: Boolean(args.dryRun),
    defaults,
    managementHeaders,
    outputDir: path.resolve(args.outputDir || path.join(process.cwd(), 'docs', 'api-import-runs')),
    profileIdsByCode: new Map(),
    records: []
  };
  const errors = [];

  try {
    for (const category of ensureArray(config.categories, 'categories')) {
      await ensureCategory(context, category);
    }
    await ensureProxyProfiles(context, ensureArray(config.proxyProfiles, 'proxyProfiles'));

    for (const originalApi of config.apis) {
      const api = mergeApiConfig(defaults, originalApi);
      try {
        if (!api.apiCode || !api.categoryCode || !api.assetType) {
          throw new Error('apiCode, categoryCode and assetType are required for each api');
        }
        await registerAsset(context, api);
        await reviseAsset(context, api);
        await attachAiProfile(context, api);
        await bindProxyProfile(context, api);
        await publishAsset(context, api);
      } catch (error) {
        errors.push({ apiCode: api.apiCode, message: error.message });
        appendRecord(context.records, {
          step: 'api-import',
          resource: api.apiCode,
          method: '-',
          status: 'FAILED',
          httpStatus: '-',
          notes: error.message
        });
        if (api.continueOnError !== true && defaults.continueOnError !== true) {
          break;
        }
      }
    }
  } finally {
    const outputs = await writeOutputs(context, config, undefined, errors);
    console.log(`Process document: ${outputs.markdownPath}`);
    console.log(`Run log JSON: ${outputs.jsonPath}`);
    if (errors.length > 0) {
      console.error(`Completed with ${errors.length} error(s).`);
      process.exitCode = 1;
    }
  }
}

run().catch((error) => {
  console.error(error.stack || error.message);
  process.exitCode = 1;
});
