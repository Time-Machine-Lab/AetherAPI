package io.github.timemachinelab.domain.catalog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CategoryCode 值对象测试。
 */
class CategoryCodeTest {

    @Nested
    @DisplayName("创建 CategoryCode")
    class CreateTests {

        @Test
        @DisplayName("应该成功创建有效的 CategoryCode")
        void shouldCreateValidCategoryCode() {
            CategoryCode code = CategoryCode.of("llm");

            assertNotNull(code);
            assertEquals("llm", code.getValue());
        }

        @Test
        @DisplayName("应该将大写字母转换为小写")
        void shouldConvertToLowercase() {
            CategoryCode code = CategoryCode.of("LLM");

            assertEquals("llm", code.getValue());
        }

        @Test
        @DisplayName("应该去除前后空格")
        void shouldTrimWhitespace() {
            CategoryCode code = CategoryCode.of("  llm  ");

            assertEquals("llm", code.getValue());
        }

        @Test
        @DisplayName("应该支持带连字符的编码")
        void shouldSupportHyphenatedCode() {
            CategoryCode code = CategoryCode.of("llm-api");

            assertEquals("llm-api", code.getValue());
        }

        @Test
        @DisplayName("应该支持下划线编码")
        void shouldSupportUnderscoredCode() {
            CategoryCode code = CategoryCode.of("llm_api");

            assertEquals("llm_api", code.getValue());
        }

        @Test
        @DisplayName("应该支持单个字符编码")
        void shouldSupportSingleCharCode() {
            CategoryCode code = CategoryCode.of("a");

            assertEquals("a", code.getValue());
        }

        @Test
        @DisplayName("应该支持数字编码")
        void shouldSupportNumericCode() {
            CategoryCode code = CategoryCode.of("123");

            assertEquals("123", code.getValue());
        }
    }

    @Nested
    @DisplayName("无效 CategoryCode 校验")
    class InvalidCodeTests {

        @Test
        @DisplayName("空字符串应该抛出异常")
        void shouldThrowExceptionForEmptyString() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of(""));
        }

        @Test
        @DisplayName("仅空白字符应该抛出异常")
        void shouldThrowExceptionForBlankString() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of("   "));
        }

        @Test
        @DisplayName("null 应该抛出异常")
        void shouldThrowExceptionForNull() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of(null));
        }

        @Test
        @DisplayName("超过 64 字符应该抛出异常")
        void shouldThrowExceptionForTooLongCode() {
            String longCode = "a".repeat(65);
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of(longCode));
        }

        @Test
        @DisplayName("正好 64 字符应该成功")
        void shouldSucceedFor64CharCode() {
            String code64 = "a".repeat(64);
            CategoryCode code = CategoryCode.of(code64);
            assertEquals(code64, code.getValue());
        }

        @ParameterizedTest
        @ValueSource(strings = {"llm api", "llm.api", "llm@api", "llm#api"})
        @DisplayName("包含非法字符应该抛出异常")
        void shouldThrowExceptionForInvalidCharacters(String invalidCode) {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of(invalidCode));
        }

        @Test
        @DisplayName("以连字符开头应该抛出异常")
        void shouldThrowExceptionForLeadingHyphen() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of("-llm"));
        }

        @Test
        @DisplayName("以连字符结尾应该抛出异常")
        void shouldThrowExceptionForTrailingHyphen() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of("llm-"));
        }

        @Test
        @DisplayName("以连续连字符应该抛出异常")
        void shouldThrowExceptionForConsecutiveHyphens() {
            assertThrows(IllegalArgumentException.class, () -> CategoryCode.of("llm--api"));
        }
    }

    @Nested
    @DisplayName("相等性")
    class EqualityTests {

        @Test
        @DisplayName("相同值的 CategoryCode 应该相等")
        void sameValueShouldBeEqual() {
            CategoryCode code1 = CategoryCode.of("llm");
            CategoryCode code2 = CategoryCode.of("llm");

            assertEquals(code1, code2);
            assertEquals(code1.hashCode(), code2.hashCode());
        }

        @Test
        @DisplayName("不同值的 CategoryCode 应该不相等")
        void differentValueShouldNotBeEqual() {
            CategoryCode code1 = CategoryCode.of("llm");
            CategoryCode code2 = CategoryCode.of("api");

            assertNotEquals(code1, code2);
        }

        @Test
        @DisplayName("toString 应该返回值")
        void toStringShouldReturnValue() {
            CategoryCode code = CategoryCode.of("llm");

            assertEquals("llm", code.toString());
        }
    }
}
