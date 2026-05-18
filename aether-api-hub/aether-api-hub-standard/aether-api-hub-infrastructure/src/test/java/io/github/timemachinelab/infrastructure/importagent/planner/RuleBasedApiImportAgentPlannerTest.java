package io.github.timemachinelab.infrastructure.importagent.planner;

import io.github.timemachinelab.domain.catalog.model.AssetType;
import io.github.timemachinelab.domain.catalog.model.AuthScheme;
import io.github.timemachinelab.domain.catalog.model.RequestMethod;
import io.github.timemachinelab.service.model.ImportAgentPlanModel;
import io.github.timemachinelab.service.model.ImportAgentPlannerRequest;
import io.github.timemachinelab.service.model.ImportAgentPlannerResult;
import io.github.timemachinelab.service.model.ImportAssetPlanModel;
import io.github.timemachinelab.service.model.ImportCategoryPlanAction;
import io.github.timemachinelab.service.model.ImportCategoryPlanModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedApiImportAgentPlannerTest {

    private final RuleBasedApiImportAgentPlanner planner = new RuleBasedApiImportAgentPlanner();

    @Test
    @DisplayName("planner should request clarification when no asset plans are provided")
    void shouldRequestClarificationWhenAssetPlansMissing() {
        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                "https://docs.example.com/weather",
                "{}",
                "import weather api",
                "please import it",
                null,
                1,
                List.of()
        ));

        assertFalse(result.getPlan().isExecutable());
        assertEquals(1, result.getPlan().getVersion());
        assertEquals(List.of("Provide JSON with assetPlans or assets to continue."), result.getPlan().getClarificationQuestions());
    }

    @Test
    @DisplayName("planner should parse executable JSON asset plan and auto-fill missing category plan")
    void shouldParseExecutablePlanFromJson() {
        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
                null,
                """
                        {
                          "assetPlans": [
                            {
                              "apiCode": "weather-forecast",
                              "assetName": "Weather Forecast",
                              "assetType": "AI_API",
                              "categoryCode": "tools",
                              "requestMethod": "GET",
                              "upstreamUrl": "https://upstream.example.com/weather",
                              "authScheme": "HEADER_TOKEN",
                              "authConfig": "Authorization: Bearer upstream-token",
                              "publishAfterImport": true,
                              "aiProfile": {
                                "provider": "OpenAI",
                                "model": "gpt-4.1",
                                "streamingSupported": true,
                                "capabilityTags": ["chat"]
                              }
                            }
                          ]
                        }
                        """,
                "import weather api",
                "please continue",
                null,
                2,
                List.of()
        ));

        assertTrue(result.getPlan().isExecutable());
        assertEquals(2, result.getPlan().getVersion());
        assertEquals(1, result.getPlan().getCategoryPlans().size());
        assertEquals("tools", result.getPlan().getCategoryPlans().get(0).getCategoryCode());
        assertEquals("weather-forecast", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("OpenAI", result.getPlan().getAssetPlans().get(0).getAiProfile().getProvider());
        assertTrue(result.getAgentMessage().contains("Ready for confirmation"));
    }

      @Test
      @DisplayName("planner should infer draft asset plan from markdown API documentation")
      void shouldInferDraftPlanFromMarkdownDocumentation() {
        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
            "https://docs.example.com/video/generate",
            """
                # 视频生成

                ## 接口信息

                - 请求地址: https://api.example.com/video/generate
                - 请求方式: POST
                - 鉴权方式: Header Token

                ## 请求参数说明

                | 名称 | 必填 | 类型 | 示例值 | 说明 |
                | ---- | ---- | ---- | ---- | ---- |
                | prompt | 是 | string | 生成一段海边日落视频 | 提示词 |

                ## 返回参数说明:

                | 名称 | 类型 | 说明 |
                | ---- | ---- | ---- |
                | code | int | 状态码 |
                | msg | string | 状态信息 |
                | data | string | 请求结果数据集 |
                | data.status | int | 状态 0初始化 1进行中 2成功 3失败 |
                | data.message | string | 请求结果错误信息 |
                """,
            "导入视频生成 API 文档",
            "请继续导入",
            null,
            2,
            List.of()
        ));

        assertTrue(result.getPlan().isExecutable());
        assertEquals(1, result.getPlan().getAssetPlans().size());
        assertEquals("video-generate", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertEquals("视频生成", result.getPlan().getAssetPlans().get(0).getAssetName());
        assertEquals("https://api.example.com/video/generate", result.getPlan().getAssetPlans().get(0).getUpstreamUrl());
        assertEquals("POST", result.getPlan().getAssetPlans().get(0).getRequestMethod().name());
        assertTrue(result.getAgentMessage().contains("Ready for confirmation"));
      }

          @Test
          @DisplayName("planner should preserve current draft plans when partial JSON update omits asset arrays")
          void shouldPreserveCurrentDraftPlansWhenPartialJsonUpdateOmitsAssetArrays() {
        ImportAgentPlanModel currentPlan = new ImportAgentPlanModel(
          1,
          true,
          "Draft plan version 1 prepared with 1 category plan(s) and 1 asset plan(s).",
          List.of(),
          List.of(new ImportCategoryPlanModel("video", "Video", ImportCategoryPlanAction.CREATE_IF_MISSING)),
          List.of(new ImportAssetPlanModel(
            "video-package-submit",
            "Video Package Submit",
            AssetType.STANDARD_API,
            "video",
            RequestMethod.POST,
            "https://upstream.example.com/video/package/submit",
            AuthScheme.HEADER_TOKEN,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            null
          ))
        );

        ImportAgentPlannerResult result = planner.plan(new ImportAgentPlannerRequest(
          null,
          null,
          "import video package api",
          "{\"summary\":\"Updated draft summary\"}",
          currentPlan,
          2,
          List.of()
        ));

        assertTrue(result.getPlan().isExecutable());
        assertEquals("Updated draft summary", result.getPlan().getSummary());
        assertEquals(1, result.getPlan().getCategoryPlans().size());
        assertEquals(1, result.getPlan().getAssetPlans().size());
        assertEquals("video-package-submit", result.getPlan().getAssetPlans().get(0).getApiCode());
        assertTrue(result.getAgentMessage().contains("Ready for confirmation"));
          }
}