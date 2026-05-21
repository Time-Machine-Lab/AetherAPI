package io.github.timemachinelab.infrastructure.importagent.planner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportAgentExampleNormalizerTest {

    @Test
    @DisplayName("normalizeRequestBodyExample should extract JSON body from curl data")
    void shouldExtractJsonBodyFromCurlData() {
        String curl = """
                curl -X POST 'https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis' \\
                  -H 'Content-Type: application/json' \\
                  --data-raw '{"model":"happyhorse-1.0-t2v","input":{"prompt":"一匹马在草地奔跑"}}'
                """;

        assertEquals(
                "{\"model\":\"happyhorse-1.0-t2v\",\"input\":{\"prompt\":\"一匹马在草地奔跑\"}}",
                ImportAgentExampleNormalizer.normalizeRequestBodyExample(curl));
    }

    @Test
    @DisplayName("normalizeRequestBodyExample should drop curl without request body")
    void shouldDropCurlWithoutRequestBody() {
        String curl = "curl -X GET 'https://provider.example.com/tasks/123'";

        assertNull(ImportAgentExampleNormalizer.normalizeRequestBodyExample(curl));
    }

    @Test
    @DisplayName("normalizeRequestBodyExample should keep non-curl body example")
    void shouldKeepNonCurlBodyExample() {
        assertEquals(
                "{\"query\":\"bike\",\"limit\":10}",
                ImportAgentExampleNormalizer.normalizeRequestBodyExample(" { \"query\" : \"bike\", \"limit\" : 10 } "));
    }
}
