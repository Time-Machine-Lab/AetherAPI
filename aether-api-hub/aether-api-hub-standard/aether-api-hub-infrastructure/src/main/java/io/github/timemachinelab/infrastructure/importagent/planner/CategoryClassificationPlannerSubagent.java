package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.timemachinelab.service.model.ImportAgentCategoryCandidateModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@ImportAgentPlannerSubagentSpec(name = "category_classification", role = ImportAgentPlannerSubagentRole.CATEGORY_CLASSIFICATION, order = 37)
public class CategoryClassificationPlannerSubagent implements ImportAgentPlannerSubagent {

    private static final int HIGH_CONFIDENCE_SCORE = 6;
    private static final int MIN_SCORE_GAP = 2;

    @Override
    public void contribute(ImportAgentPlannerSubagentContext context, ObjectNode candidatePlan) {
        JsonNode assetPlansNode = candidatePlan.path("assetPlans");
        if (!assetPlansNode.isArray()) {
            return;
        }
        Map<String, CategoryCandidate> candidates = enabledCandidates(context.getRequest());
        if (candidates.isEmpty()) {
            addMissingCandidateQuestions(context, (ArrayNode) assetPlansNode);
            return;
        }

        LinkedHashSet<String> usedCategoryCodes = new LinkedHashSet<>();
        for (JsonNode assetNode : (ArrayNode) assetPlansNode) {
            if (!assetNode.isObject()) {
                continue;
            }
            ObjectNode assetObject = (ObjectNode) assetNode;
            String currentCode = ImportAgentPlannerSubagentSupport.textValue(assetObject, "categoryCode");
            CategoryCandidate currentCandidate = candidates.get(normalizeKey(currentCode));
            if (currentCandidate != null) {
                assetObject.put("categoryCode", currentCandidate.code());
                usedCategoryCodes.add(currentCandidate.code());
                continue;
            }

            List<CategoryScore> scores = rankCandidates(assetObject, candidates);
            CategoryScore best = scores.isEmpty() ? null : scores.get(0);
            CategoryScore second = scores.size() < 2 ? null : scores.get(1);
            if (isHighConfidence(best, second)) {
                assetObject.put("categoryCode", best.candidate().code());
                usedCategoryCodes.add(best.candidate().code());
                continue;
            }

            if (currentCode == null || !currentCode.isBlank()) {
                context.addClarificationQuestion(buildClarificationQuestion(assetObject, currentCode, scores));
            }
        }
        syncCategoryPlans(candidatePlan, candidates, usedCategoryCodes);
    }

    private Map<String, CategoryCandidate> enabledCandidates(ImportAgentPlannerRequest request) {
        Map<String, CategoryCandidate> values = new LinkedHashMap<>();
        for (ImportAgentCategoryCandidateModel candidate : request.getAvailableCategories()) {
            if (candidate == null || candidate.getCategoryCode() == null || candidate.getCategoryCode().isBlank()) {
                continue;
            }
            String status = candidate.getStatus();
            if (status != null && !status.isBlank() && !"ENABLED".equalsIgnoreCase(status.trim())) {
                continue;
            }
            String key = normalizeKey(candidate.getCategoryCode());
            if (key == null || values.containsKey(key)) {
                continue;
            }
            values.put(key, new CategoryCandidate(
                    candidate.getCategoryCode().trim(),
                    candidate.getCategoryName() == null ? candidate.getCategoryCode().trim() : candidate.getCategoryName().trim()));
        }
        return values;
    }

    private void addMissingCandidateQuestions(ImportAgentPlannerSubagentContext context, ArrayNode assetPlans) {
        for (JsonNode assetNode : assetPlans) {
            if (!assetNode.isObject()) {
                continue;
            }
            String categoryCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "categoryCode");
            if (categoryCode == null) {
                context.addClarificationQuestion("当前没有可用的启用分类，无法为资产计划 "
                        + displayApiCode(assetNode) + " 自动填写分类编码；请先创建或启用分类，或告知应使用的分类。");
            }
        }
    }

    private List<CategoryScore> rankCandidates(ObjectNode assetNode, Map<String, CategoryCandidate> candidates) {
        String evidence = evidenceText(assetNode);
        Set<String> evidenceTags = domainTags(evidence);
        List<CategoryScore> scores = new ArrayList<>();
        for (CategoryCandidate candidate : candidates.values()) {
            int score = scoreCandidate(candidate, evidence, evidenceTags, assetNode);
            if (score > 0) {
                scores.add(new CategoryScore(candidate, score));
            }
        }
        scores.sort(Comparator.comparingInt(CategoryScore::score).reversed()
                .thenComparing(score -> score.candidate().code()));
        return List.copyOf(scores);
    }

    private int scoreCandidate(
            CategoryCandidate candidate,
            String evidence,
            Set<String> evidenceTags,
            ObjectNode assetNode) {
        int score = 0;
        String categoryCode = normalizeSearchable(candidate.code());
        String categoryName = normalizeSearchable(candidate.name());
        if (containsToken(evidence, categoryCode)) {
            score += 8;
        }
        if (!categoryName.equals(categoryCode) && containsToken(evidence, categoryName)) {
            score += 8;
        }
        for (String token : categoryTokens(candidate)) {
            if (token.length() >= 2 && containsToken(evidence, token)) {
                score += 3;
            }
        }
        Set<String> candidateTags = domainTags(categoryCode + " " + categoryName);
        for (String tag : candidateTags) {
            if (evidenceTags.contains(tag)) {
                score += 6;
            }
        }
        if ("AI_API".equalsIgnoreCase(ImportAgentPlannerSubagentSupport.textValue(assetNode, "assetType"))
                && (candidateTags.contains("ai") || candidateTags.contains("llm"))) {
            score += 3;
        }
        return score;
    }

    private boolean isHighConfidence(CategoryScore best, CategoryScore second) {
        if (best == null || best.score() < HIGH_CONFIDENCE_SCORE) {
            return false;
        }
        return second == null || best.score() - second.score() >= MIN_SCORE_GAP;
    }

    private void syncCategoryPlans(
            ObjectNode candidatePlan,
            Map<String, CategoryCandidate> candidates,
            LinkedHashSet<String> usedCategoryCodes) {
        if (usedCategoryCodes.isEmpty()) {
            return;
        }
        ArrayNode existing = ImportAgentPlannerSubagentSupport.ensureArray(candidatePlan, "categoryPlans");
        ArrayNode rebuilt = existing.arrayNode();
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        for (JsonNode categoryPlan : existing) {
            if (!categoryPlan.isObject()) {
                rebuilt.add(categoryPlan.deepCopy());
                continue;
            }
            String code = ImportAgentPlannerSubagentSupport.textValue(categoryPlan, "categoryCode");
            String key = normalizeKey(code);
            if (key != null && seen.contains(key)) {
                continue;
            }
            if (key != null && usedCategoryCodes.stream().anyMatch(value -> normalizeKey(value).equals(key))) {
                CategoryCandidate candidate = candidates.get(key);
                addUseExistingCategoryPlan(rebuilt, candidate);
                seen.add(key);
                continue;
            }
            rebuilt.add(categoryPlan.deepCopy());
            if (key != null) {
                seen.add(key);
            }
        }
        for (String usedCategoryCode : usedCategoryCodes) {
            String key = normalizeKey(usedCategoryCode);
            if (key == null || seen.contains(key)) {
                continue;
            }
            addUseExistingCategoryPlan(rebuilt, candidates.get(key));
            seen.add(key);
        }
        candidatePlan.set("categoryPlans", rebuilt);
    }

    private void addUseExistingCategoryPlan(ArrayNode categoryPlans, CategoryCandidate candidate) {
        if (candidate == null) {
            return;
        }
        ObjectNode categoryPlan = categoryPlans.addObject();
        categoryPlan.put("categoryCode", candidate.code());
        categoryPlan.put("categoryName", candidate.name());
        categoryPlan.put("action", "USE_EXISTING");
    }

    private String buildClarificationQuestion(ObjectNode assetNode, String invalidCategoryCode, List<CategoryScore> scores) {
        String prefix = invalidCategoryCode == null
                ? "资产计划 " + displayApiCode(assetNode) + " 需要确认分类编码"
                : "资产计划 " + displayApiCode(assetNode) + " 的分类编码 " + invalidCategoryCode + " 不在当前启用分类中";
        String suggestions = topSuggestions(scores);
        if (suggestions == null) {
            return prefix + "，请从已启用分类中选择一个合适的分类。";
        }
        return prefix + "，请确认应使用哪个分类；候选：" + suggestions + "。";
    }

    private String topSuggestions(List<CategoryScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return null;
        }
        return scores.stream()
                .limit(3)
                .map(score -> score.candidate().code() + "（" + score.candidate().name() + "）")
                .reduce((left, right) -> left + "、" + right)
                .orElse(null);
    }

    private String evidenceText(ObjectNode assetNode) {
        List<String> values = new ArrayList<>();
        addEvidence(values, assetNode, "apiCode");
        addEvidence(values, assetNode, "assetName");
        addEvidence(values, assetNode, "assetType");
        addEvidence(values, assetNode, "upstreamUrl");
        addEvidence(values, assetNode, "requestExample");
        addEvidence(values, assetNode, "responseExample");
        addEvidence(values, assetNode, "requestJsonSchema");
        addEvidence(values, assetNode, "responseJsonSchema");
        JsonNode aiProfile = assetNode.path("aiProfile");
        addEvidence(values, aiProfile, "provider");
        addEvidence(values, aiProfile, "model");
        for (JsonNode tagNode : aiProfile.path("capabilityTags")) {
            values.add(tagNode.asText(""));
        }
        JsonNode asyncTaskConfig = assetNode.path("asyncTaskConfig");
        addEvidence(values, asyncTaskConfig, "queryUrlTemplate");
        addEvidence(values, asyncTaskConfig, "statusPath");
        addEvidence(values, asyncTaskConfig, "resultPath");
        return normalizeSearchable(String.join(" ", values));
    }

    private void addEvidence(List<String> values, JsonNode node, String fieldName) {
        String value = ImportAgentPlannerSubagentSupport.textValue(node, fieldName);
        if (value != null) {
            values.add(value);
        }
    }

    private Set<String> domainTags(String text) {
        String normalized = normalizeSearchable(text);
        Set<String> tags = new LinkedHashSet<>();
        if (containsAny(normalized, "ai", "aigc", "artificial intelligence", "大模型", "模型")) {
            tags.add("ai");
        }
        if (containsAny(normalized, "llm", "chat", "gpt", "qwen", "deepseek", "claude", "gemini", "conversation", "对话", "聊天", "通义")) {
            tags.add("llm");
            tags.add("chat");
        }
        if (containsAny(normalized, "video", "t2v", "i2v", "text to video", "image to video", "视频")) {
            tags.add("video");
        }
        if (containsAny(normalized, "image", "img", "picture", "photo", "vision", "图像", "图片", "视觉")) {
            tags.add("image");
        }
        if (containsAny(normalized, "audio", "speech", "voice", "tts", "asr", "语音", "音频")) {
            tags.add("audio");
            tags.add("speech");
        }
        if (containsAny(normalized, "embedding", "embed", "vector", "向量")) {
            tags.add("embedding");
        }
        if (containsAny(normalized, "rerank", "rank", "重排")) {
            tags.add("rerank");
        }
        if (containsAny(normalized, "ocr", "vision", "视觉")) {
            tags.add("vision");
        }
        return tags;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> categoryTokens(CategoryCandidate candidate) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        addTokens(tokens, candidate.code());
        addTokens(tokens, candidate.name());
        return tokens;
    }

    private void addTokens(Set<String> tokens, String value) {
        for (String token : normalizeSearchable(value).split(" ")) {
            if (!token.isBlank()) {
                tokens.add(token);
                if (token.endsWith("s") && token.length() > 3) {
                    tokens.add(token.substring(0, token.length() - 1));
                }
            }
        }
    }

    private boolean containsToken(String text, String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String normalized = normalizeSearchable(token);
        return (" " + text + " ").contains(" " + normalized + " ");
    }

    private String normalizeSearchable(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsHan}a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String displayApiCode(JsonNode assetNode) {
        String apiCode = ImportAgentPlannerSubagentSupport.textValue(assetNode, "apiCode");
        return apiCode == null || apiCode.isBlank() ? "<unknown>" : apiCode;
    }

    private record CategoryCandidate(String code, String name) {
    }

    private record CategoryScore(CategoryCandidate candidate, int score) {
    }
}
