package io.github.aetherapihub.catalog.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分类聚合根行为测试。
 */
class ApiCategoryAggregateTest {

    @Test
    void shouldCreateCategoryAsEnabled() {
        ApiCategoryAggregate category = new ApiCategoryAggregate(
                new CategoryId("id-1"),
                new CategoryCode("llm"),
                "大语言模型",
                Instant.now()
        );

        assertEquals(CategoryStatus.ENABLED, category.getStatus());
        assertEquals("llm", category.getCode().getValue());
        assertEquals("大语言模型", category.getName());
        assertFalse(category.isDeleted());
        assertTrue(category.isValidForAssignment());
    }

    @Test
    void shouldRenameCategory() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型");

        category.rename("大模型");

        assertEquals("大模型", category.getName());
        assertEquals("llm", category.getCode().getValue());
    }

    @Test
    void shouldRejectEmptyNameOnRename() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型");

        assertThrows(IllegalArgumentException.class, () -> category.rename(""));
        assertThrows(IllegalArgumentException.class, () -> category.rename(null));
        assertThrows(IllegalArgumentException.class, () -> category.rename("   "));
    }

    @Test
    void shouldRejectTooLongName() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型");
        String tooLong = "a".repeat(129);

        assertThrows(IllegalArgumentException.class, () -> category.rename(tooLong));
    }

    @Test
    void shouldEnableCategory() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型", CategoryStatus.DISABLED);

        category.enable();

        assertEquals(CategoryStatus.ENABLED, category.getStatus());
        assertTrue(category.isValidForAssignment());
    }

    @Test
    void shouldDisableCategory() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型");

        category.disable();

        assertEquals(CategoryStatus.DISABLED, category.getStatus());
        assertFalse(category.isValidForAssignment());
    }

    @Test
    void shouldIgnoreRedundantEnable() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型");
        long versionBefore = category.getVersion();

        category.enable();

        assertEquals(versionBefore, category.getVersion());
    }

    @Test
    void shouldIgnoreRedundantDisable() {
        ApiCategoryAggregate category = newCategory("llm", "大语言模型", CategoryStatus.DISABLED);
        long versionBefore = category.getVersion();

        category.disable();

        assertEquals(versionBefore, category.getVersion());
    }

    @Test
    void shouldBeInvalidWhenDeleted() {
        ApiCategoryAggregate category = new ApiCategoryAggregate(
                new CategoryId("id-1"),
                new CategoryCode("llm"),
                "大语言模型",
                CategoryStatus.ENABLED,
                Instant.now(),
                Instant.now(),
                true,
                0L
        );

        assertFalse(category.isValidForAssignment());
    }

    @Test
    void categoryCodeShouldBeImmutable() {
        CategoryCode code = new CategoryCode("llm");

        assertEquals("llm", code.getValue());
        assertEquals(new CategoryCode("llm"), code);
        assertEquals(new CategoryCode("llm").hashCode(), code.hashCode());
    }

    @Test
    void categoryCodeShouldRejectInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> new CategoryCode(null));
        assertThrows(IllegalArgumentException.class, () -> new CategoryCode(""));
        assertThrows(IllegalArgumentException.class, () -> new CategoryCode("  "));
        assertThrows(IllegalArgumentException.class, () -> new CategoryCode("a".repeat(65)));
    }

    private ApiCategoryAggregate newCategory(String code, String name) {
        return newCategory(code, name, CategoryStatus.ENABLED);
    }

    private ApiCategoryAggregate newCategory(String code, String name, CategoryStatus status) {
        return new ApiCategoryAggregate(
                new CategoryId("id-" + System.nanoTime()),
                new CategoryCode(code),
                name,
                status,
                Instant.now(),
                Instant.now(),
                false,
                0L
        );
    }
}
