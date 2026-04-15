package io.github.timemachinelab.domain.catalog.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API 分类聚合根测试。
 */
class ApiCategoryAggregateTest {

    private static final CategoryId TEST_ID = CategoryId.of("550e8400-e29b-41d4-a716-446655440000");
    private static final CategoryCode TEST_CODE = CategoryCode.of("llm");
    private static final String TEST_NAME = "大语言模型";

    @Nested
    @DisplayName("创建聚合根")
    class CreateTests {

        @Test
        @DisplayName("应该成功创建分类，默认状态为 ENABLED")
        void shouldCreateCategoryWithEnabledStatus() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            assertNotNull(aggregate);
            assertEquals(TEST_ID, aggregate.getId());
            assertEquals(TEST_CODE, aggregate.getCode());
            assertEquals(TEST_NAME, aggregate.getName());
            assertEquals(CategoryStatus.ENABLED, aggregate.getStatus());
            assertFalse(aggregate.isDeleted());
            assertEquals(0L, aggregate.getVersion());
            assertNotNull(aggregate.getCreatedAt());
            assertNotNull(aggregate.getUpdatedAt());
        }

        @Test
        @DisplayName("新建分类应该有效")
        void shouldBeValidWhenCreated() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);
            assertTrue(aggregate.isValid());
        }
    }

    @Nested
    @DisplayName("重命名分类")
    class RenameTests {

        @Test
        @DisplayName("应该成功重命名分类")
        void shouldRenameCategory() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);
            String newName = "大模型";

            aggregate.rename(newName);

            assertEquals(newName, aggregate.getName());
            assertEquals(1L, aggregate.getVersion());
            assertNotEquals(aggregate.getCreatedAt(), aggregate.getUpdatedAt());
        }

        @Test
        @DisplayName("重命名为空字符串应该抛出异常")
        void shouldThrowExceptionWhenRenamedToBlank() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            assertThrows(IllegalArgumentException.class, () -> aggregate.rename(""));
        }

        @Test
        @DisplayName("重命名为 null 应该抛出异常")
        void shouldThrowExceptionWhenRenamedToNull() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            assertThrows(IllegalArgumentException.class, () -> aggregate.rename(null));
        }

        @Test
        @DisplayName("重命名应该去除前后空格")
        void shouldTrimWhitespace() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            aggregate.rename("  大模型 ");

            assertEquals("大模型", aggregate.getName());
        }
    }

    @Nested
    @DisplayName("启用分类")
    class EnableTests {

        @Test
        @DisplayName("应该成功启用已停用的分类")
        void shouldEnableDisabledCategory() {
            ApiCategoryAggregate aggregate = createDisabledCategory();

            aggregate.enable();

            assertEquals(CategoryStatus.ENABLED, aggregate.getStatus());
            assertTrue(aggregate.isValid());
            assertEquals(1L, aggregate.getVersion());
        }

        @Test
        @DisplayName("启用已启用的分类应该抛出异常")
        void shouldThrowExceptionWhenEnablingEnabledCategory() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    aggregate::enable
            );
            assertEquals("Category is already enabled", exception.getMessage());
        }

        @Test
        @DisplayName("启用已删除的分类应该抛出异常")
        void shouldThrowExceptionWhenEnablingDeletedCategory() {
            ApiCategoryAggregate aggregate = createDeletedCategory();

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    aggregate::enable
            );
            assertEquals("Category has been deleted", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("停用分类")
    class DisableTests {

        @Test
        @DisplayName("应该成功停用已启用的分类")
        void shouldDisableEnabledCategory() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            aggregate.disable();

            assertEquals(CategoryStatus.DISABLED, aggregate.getStatus());
            assertFalse(aggregate.isValid());
            assertEquals(1L, aggregate.getVersion());
        }

        @Test
        @DisplayName("停用已停用的分类应该抛出异常")
        void shouldThrowExceptionWhenDisablingDisabledCategory() {
            ApiCategoryAggregate aggregate = createDisabledCategory();

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    aggregate::disable
            );
            assertEquals("Category is already disabled", exception.getMessage());
        }

        @Test
        @DisplayName("停用已删除的分类应该抛出异常")
        void shouldThrowExceptionWhenDisablingDeletedCategory() {
            ApiCategoryAggregate aggregate = createDeletedCategory();

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    aggregate::disable
            );
            assertEquals("Category has been deleted", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("软删除")
    class DeleteTests {

        @Test
        @DisplayName("应该成功软删除分类")
        void shouldSoftDeleteCategory() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            aggregate.markDeleted();

            assertTrue(aggregate.isDeleted());
            assertFalse(aggregate.isValid());
            assertEquals(1L, aggregate.getVersion());
        }

        @Test
        @DisplayName("已删除的分类无效")
        void deletedCategoryShouldBeInvalid() {
            ApiCategoryAggregate aggregate = createDeletedCategory();

            assertFalse(aggregate.isValid());
        }
    }

    @Nested
    @DisplayName("生命周期流转")
    class LifecycleTransitionTests {

        @Test
        @DisplayName("应该支持 ENABLED -> DISABLED -> ENABLED 循环")
        void shouldSupportEnabledDisabledEnabledCycle() {
            ApiCategoryAggregate aggregate = ApiCategoryAggregate.create(TEST_ID, TEST_CODE, TEST_NAME);

            aggregate.disable();
            assertEquals(CategoryStatus.DISABLED, aggregate.getStatus());

            aggregate.enable();
            assertEquals(CategoryStatus.ENABLED, aggregate.getStatus());
            assertTrue(aggregate.isValid());
        }

        @Test
        @DisplayName("应该支持 DISABLED -> ENABLED -> DISABLED 循环")
        void shouldSupportDisabledEnabledDisabledCycle() {
            ApiCategoryAggregate aggregate = createDisabledCategory();

            aggregate.enable();
            assertEquals(CategoryStatus.ENABLED, aggregate.getStatus());

            aggregate.disable();
            assertEquals(CategoryStatus.DISABLED, aggregate.getStatus());
        }

        @Test
        @DisplayName("重命名后应该保持状态")
        void shouldMaintainStatusAfterRename() {
            ApiCategoryAggregate aggregate = createDisabledCategory();

            aggregate.rename("新名称");

            assertEquals(CategoryStatus.DISABLED, aggregate.getStatus());
            assertEquals("新名称", aggregate.getName());
        }
    }

    @Nested
    @DisplayName("聚合根重建")
    class ReconstituteTests {

        @Test
        @DisplayName("应该正确重建聚合根")
        void shouldReconstituteAggregate() {
            Instant createdAt = Instant.parse("2026-04-15T10:00:00Z");
            Instant updatedAt = Instant.parse("2026-04-15T12:00:00Z");

            ApiCategoryAggregate aggregate = ApiCategoryAggregate.reconstitute(
                    TEST_ID, TEST_CODE, TEST_NAME, CategoryStatus.DISABLED,
                    createdAt, updatedAt, false, 5L
            );

            assertEquals(TEST_ID, aggregate.getId());
            assertEquals(TEST_CODE, aggregate.getCode());
            assertEquals(TEST_NAME, aggregate.getName());
            assertEquals(CategoryStatus.DISABLED, aggregate.getStatus());
            assertEquals(createdAt, aggregate.getCreatedAt());
            assertEquals(updatedAt, aggregate.getUpdatedAt());
            assertFalse(aggregate.isDeleted());
            assertEquals(5L, aggregate.getVersion());
        }
    }

    // -------------------- 辅助方法 --------------------

    private ApiCategoryAggregate createDisabledCategory() {
        return ApiCategoryAggregate.reconstitute(
                TEST_ID, TEST_CODE, TEST_NAME, CategoryStatus.DISABLED,
                Instant.now(), Instant.now(), false, 0L
        );
    }

    private ApiCategoryAggregate createDeletedCategory() {
        return ApiCategoryAggregate.reconstitute(
                TEST_ID, TEST_CODE, TEST_NAME, CategoryStatus.ENABLED,
                Instant.now(), Instant.now(), true, 0L
        );
    }
}
