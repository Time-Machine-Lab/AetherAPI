import { http } from '@/api/http'
import type {
  CreateCurrentUserApiKeyBody,
  CurrentUserApiKeyDto,
  CurrentUserApiKeyPageDto,
  IssuedCurrentUserApiKeyDto,
  LastUsedSnapshotDto,
} from './credential.dto'
import type {
  ApiKey,
  CredentialStatus,
  IssuedApiKey,
  LastUsedSnapshot,
} from './credential.types'
import type { PageResult } from '@/api/catalog/catalog.types'

function mapLastUsedSnapshot(dto: LastUsedSnapshotDto | null): LastUsedSnapshot | null {
  if (!dto) return null
  return {
    lastUsedAt: dto.lastUsedAt,
    lastUsedChannel: dto.lastUsedChannel,
    lastUsedResult: dto.lastUsedResult,
  }
}

function mapApiKey(dto: CurrentUserApiKeyDto): ApiKey {
  return {
    credentialId: dto.credentialId,
    credentialCode: dto.credentialCode,
    credentialName: dto.credentialName,
    credentialDescription: dto.credentialDescription,
    maskedKey: dto.maskedKey,
    keyPrefix: dto.keyPrefix,
    status: dto.status,
    expireAt: dto.expireAt,
    revokedAt: dto.revokedAt,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
    lastUsedSnapshot: mapLastUsedSnapshot(dto.lastUsedSnapshot),
  }
}

function mapIssuedApiKey(dto: IssuedCurrentUserApiKeyDto): IssuedApiKey {
  return {
    ...mapApiKey(dto),
    plaintextKey: dto.plaintextKey,
  }
}

export async function listCurrentUserApiKeys(params?: {
  status?: CredentialStatus
  page?: number
  size?: number
}): Promise<PageResult<ApiKey>> {
  const { data } = await http.get<CurrentUserApiKeyPageDto>('v1/current-user/api-keys', { params })
  return {
    items: data.items.map(mapApiKey),
    total: data.total,
    page: data.page,
    pageSize: data.size,
  }
}

export async function getCurrentUserApiKeyDetail(credentialId: string): Promise<ApiKey> {
  const { data } = await http.get<CurrentUserApiKeyDto>(
    `v1/current-user/api-keys/${encodeURIComponent(credentialId)}`,
  )
  return mapApiKey(data)
}

export async function createCurrentUserApiKey(
  body: CreateCurrentUserApiKeyBody,
): Promise<IssuedApiKey> {
  const { data } = await http.post<IssuedCurrentUserApiKeyDto>('v1/current-user/api-keys', body)
  return mapIssuedApiKey(data)
}

export async function enableCurrentUserApiKey(credentialId: string): Promise<ApiKey> {
  const { data } = await http.patch<CurrentUserApiKeyDto>(
    `v1/current-user/api-keys/${encodeURIComponent(credentialId)}/enable`,
  )
  return mapApiKey(data)
}

export async function disableCurrentUserApiKey(credentialId: string): Promise<ApiKey> {
  const { data } = await http.patch<CurrentUserApiKeyDto>(
    `v1/current-user/api-keys/${encodeURIComponent(credentialId)}/disable`,
  )
  return mapApiKey(data)
}

export async function revokeCurrentUserApiKey(credentialId: string): Promise<ApiKey> {
  const { data } = await http.patch<CurrentUserApiKeyDto>(
    `v1/current-user/api-keys/${encodeURIComponent(credentialId)}/revoke`,
  )
  return mapApiKey(data)
}
