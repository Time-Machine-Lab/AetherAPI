package io.github.timemachinelab;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SpringBootTest(classes = AetherApiHubApplication.class)
public class YmlEncryptionTool {

    @Autowired
    StringEncryptor encryptor;

    // 定义需要加密的字段路径
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
        "oss.qiniuyun.access-key-id",
        "oss.qiniuyun.access-key-secret",
        "oss.qiniuyun.bucket-name",
        "oss.qiniuyun.domain",
        "oss.qiniuyun.prefix"
    );

    @Test
    public void generateEncryptedYml() {
        try {
            // 读取原始yml文件
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream("src/main/resources/application-dev.yml");
            Map<String, Object> data = yaml.load(inputStream);
            inputStream.close();

            // 加密敏感字段
            Map<String, Object> encryptedData = encryptSensitiveFields(data, "");

            // 输出加密后的yml内容
            System.out.println("=== 加密后的application.yml内容 ===");
            printYamlContent(encryptedData, 0);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> encryptSensitiveFields(Map<String, Object> data, String prefix) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullPath = prefix.isEmpty() ? key : prefix + "." + key;
            
            if (value instanceof Map) {
                // 递归处理嵌套的Map
                result.put(key, encryptSensitiveFields((Map<String, Object>) value, fullPath));
            } else if (value instanceof String) {
                String stringValue = (String) value;
                
                // 检查是否是需要加密的敏感字段
                if (SENSITIVE_FIELDS.contains(fullPath)) {
                    // 提取默认值（如果存在）
                    String defaultValue = extractDefaultValue(stringValue);
                    if (defaultValue != null && !defaultValue.startsWith("ENC(")) {
                        // 加密默认值
                        String encrypted = encryptor.encrypt(defaultValue);
                        String encryptedValue = stringValue.replace(defaultValue, "ENC(" + encrypted + ")");
                        result.put(key, encryptedValue);
                        System.out.println("加密字段: " + fullPath + " -> " + defaultValue + " -> ENC(" + encrypted + ")");
                    } else {
                        result.put(key, stringValue);
                    }
                } else {
                    result.put(key, stringValue);
                }
            } else {
                result.put(key, value);
            }
        }
        
        return result;
    }

    /**
     * 从${VAR_NAME:defaultValue}格式中提取默认值
     */
    private String extractDefaultValue(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            String content = value.substring(2, value.length() - 1);
            int colonIndex = content.indexOf(":");
            if (colonIndex > 0 && colonIndex < content.length() - 1) {
                return content.substring(colonIndex + 1);
            }
        }
        return null;
    }

    /**
     * 打印YAML格式的内容
     */
    @SuppressWarnings("unchecked")
    private void printYamlContent(Map<String, Object> data, int indent) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            String indentStr = "  ".repeat(indent);
            
            if (value instanceof Map) {
                System.out.println(indentStr + key + ":");
                printYamlContent((Map<String, Object>) value, indent + 1);
            } else if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue.contains("\n")) {
                    // 多行字符串处理
                    System.out.println(indentStr + key + ": |");
                    String[] lines = stringValue.split("\n");
                    for (String line : lines) {
                        System.out.println(indentStr + "  " + line);
                    }
                } else {
                    System.out.println(indentStr + key + ": " + stringValue);
                }
            } else {
                System.out.println(indentStr + key + ": " + value);
            }
        }
    }

    /**
     * 单独加密指定的字符串值
     */
    @Test
    public void encryptSingleValue() {
        // 示例：加密单个值
        String plainText = "your-plain-text-here";
        String encrypted = encryptor.encrypt(plainText);
        System.out.println("原文: " + plainText);
        System.out.println("加密后: ENC(" + encrypted + ")");
    }
}