package io.github.timemachinelab.infrastructure.importagent.planner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportAgentSchemaNormalizerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("normalize should serialize JSON object schema nodes")
    void shouldSerializeJsonObjectSchemaNodes() {
        ObjectNode schema = OBJECT_MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties").putObject("name").put("type", "string");

        assertEquals("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}",
                ImportAgentSchemaNormalizer.normalize(schema));
    }

    @Test
    @DisplayName("normalize should accept JSON object schema strings")
    void shouldAcceptJsonObjectSchemaStrings() {
        assertEquals("{\"type\":\"object\"}",
                ImportAgentSchemaNormalizer.normalize(" { \"type\" : \"object\" } "));
    }

    @Test
    @DisplayName("normalize should reject malformed or non-object schema values")
    void shouldRejectMalformedOrNonObjectSchemaValues() {
        assertNull(ImportAgentSchemaNormalizer.normalize("不是 JSON Schema"));
        assertNull(ImportAgentSchemaNormalizer.normalize("[{\"type\":\"object\"}]"));
        assertNull(ImportAgentSchemaNormalizer.normalize("{\"type\":\"unsupported\"}"));
        assertNull(ImportAgentSchemaNormalizer.normalize(""));
    }

    @Test
    @DisplayName("inferFromExample should build a basic schema from JSON object examples")
    void shouldInferBasicSchemaFromJsonObjectExamples() {
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"age\":{\"type\":\"integer\"},\"enabled\":{\"type\":\"boolean\"}},\"required\":[\"name\",\"age\",\"enabled\"]}",
                ImportAgentSchemaNormalizer.inferFromExample("{\"name\":\"Alice\",\"age\":18,\"enabled\":true}"));
    }

    @Test
    @DisplayName("inferExampleFromSchema should build a JSON object example from schema properties")
    void shouldInferExampleFromSchemaProperties() {
        assertEquals(
                "{\"prompt\":\"example-prompt\",\"size\":1,\"stream\":true,\"items\":[{\"id\":\"example-id\"}]}",
                ImportAgentSchemaNormalizer.inferExampleFromSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "prompt": { "type": "string" },
                            "size": { "type": "integer" },
                            "stream": { "type": "boolean" },
                            "items": {
                              "type": "array",
                              "items": {
                                "type": "object",
                                "properties": {
                                  "id": { "type": "string" }
                                }
                              }
                            }
                          }
                        }
                        """));
    }
}
