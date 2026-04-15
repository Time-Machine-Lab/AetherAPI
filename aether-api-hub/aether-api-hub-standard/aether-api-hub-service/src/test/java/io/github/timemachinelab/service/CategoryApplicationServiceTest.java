package io.github.timemachinelab.service;

import io.github.timemachinelab.domain.catalog.model.ApiCategoryAggregate;
import io.github.timemachinelab.domain.catalog.model.CategoryCode;
import io.github.timemachinelab.domain.catalog.model.CategoryDomainException;
import io.github.timemachinelab.domain.catalog.model.CategoryId;
import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.service.application.CategoryApplicationService;
import io.github.timemachinelab.service.model.CategoryModel;
import io.github.timemachinelab.service.model.CategoryPageResult;
import io.github.timemachinelab.service.model.CategoryValidityResult;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.RenameCategoryCommand;
import io.github.timemachinelab.service.port.out.CategoryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 分类应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private CategoryRepositoryPort repositoryPort;

    private CategoryApplicationService service;

    private static final String TEST_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_CODE = "llm";
    private static final String TEST_NAME = "大语言模型";

    @BeforeEach
    void setUp() {
        service = new CategoryApplicationService(repositoryPort);
    }

    @Nested
    @DisplayName("创建分类")
    class CreateCategoryTests {

        @Test
        @DisplayName("应该成功创建新分类")
        void shouldCreateCategorySuccessfully() {
            when(repositoryPort.existsByCode(any(CategoryCode.class))).thenReturn(false);
            doNothing().when(repositoryPort).save(any(ApiCategoryAggregate.class));

            CreateCategoryCommand command = new CreateCategoryCommand(TEST_CODE, TEST_NAME);
            CategoryModel result = service.createCategory(command);

            assertNotNull(result);
            assertEquals(TEST_CODE, result.getCode());
            assertEquals(TEST_NAME, result.getName());
            assertEquals("ENABLED", result.getStatus());

            verify(repositoryPort).existsByCode(any(CategoryCode.class));
            verify(repositoryPort).save(any(ApiCategoryAggregate.class));
        }

        @Test
        @DisplayName("当编码已存在时应该抛出异常")
        void shouldThrowExceptionWhenCodeExists() {
            when(repositoryPort.existsByCode(any(CategoryCode.class))).thenReturn(true);

            CreateCategoryCommand command = new CreateCategoryCommand(TEST_CODE, TEST_NAME);

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    () -> service.createCategory(command)
            );
            assertTrue(exception.getMessage().contains("already exists"));

            verify(repositoryPort).existsByCode(any(CategoryCode.class));
            verify(repositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("重命名分类")
    class RenameCategoryTests {

        @Test
        @DisplayName("应该成功重命名分类")
        void shouldRenameCategorySuccessfully() {
            ApiCategoryAggregate aggregate = createTestAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));
            doNothing().when(repositoryPort).save(any());

            RenameCategoryCommand command = new RenameCategoryCommand(TEST_CODE, "新名称");
            CategoryModel result = service.renameCategory(command);

            assertNotNull(result);
            assertEquals("新名称", result.getName());
            verify(repositoryPort).save(any());
        }

        @Test
        @DisplayName("当分类不存在时应该抛出异常")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

            RenameCategoryCommand command = new RenameCategoryCommand(TEST_CODE, "新名称");

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    () -> service.renameCategory(command)
            );
            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    @Nested
    @DisplayName("启用分类")
    class EnableCategoryTests {

        @Test
        @DisplayName("应该成功启用已停用的分类")
        void shouldEnableCategorySuccessfully() {
            ApiCategoryAggregate aggregate = createDisabledAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));
            doNothing().when(repositoryPort).save(any());

            CategoryModel result = service.enableCategory(TEST_CODE);

            assertNotNull(result);
            assertEquals("ENABLED", result.getStatus());
            verify(repositoryPort).save(any());
        }

        @Test
        @DisplayName("当分类不存在时应该抛出异常")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    () -> service.enableCategory(TEST_CODE)
            );
            assertTrue(exception.getMessage().contains("not found"));
        }
    }

    @Nested
    @DisplayName("停用分类")
    class DisableCategoryTests {

        @Test
        @DisplayName("应该成功停用已启用的分类")
        void shouldDisableCategorySuccessfully() {
            ApiCategoryAggregate aggregate = createTestAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));
            doNothing().when(repositoryPort).save(any());

            CategoryModel result = service.disableCategory(TEST_CODE);

            assertNotNull(result);
            assertEquals("DISABLED", result.getStatus());
            verify(repositoryPort).save(any());
        }
    }

    @Nested
    @DisplayName("查询分类")
    class QueryCategoryTests {

        @Test
        @DisplayName("应该成功根据编码查询分类")
        void shouldGetCategoryByCode() {
            ApiCategoryAggregate aggregate = createTestAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));

            CategoryModel result = service.getCategoryByCode(TEST_CODE);

            assertNotNull(result);
            assertEquals(TEST_CODE, result.getCode());
            assertEquals(TEST_NAME, result.getName());
        }

        @Test
        @DisplayName("当分类不存在时应该抛出异常")
        void shouldThrowExceptionWhenCategoryNotFound() {
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

            CategoryDomainException exception = assertThrows(
                    CategoryDomainException.class,
                    () -> service.getCategoryByCode(TEST_CODE)
            );
            assertTrue(exception.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("应该成功分页查询分类列表")
        void shouldListCategoriesWithPagination() {
            List<ApiCategoryAggregate> aggregates = Arrays.asList(
                    createTestAggregate(),
                    createDisabledAggregate()
            );
            when(repositoryPort.findAll(any(), eq(0), eq(20))).thenReturn(aggregates);
            when(repositoryPort.count(any())).thenReturn(2L);

            CategoryPageResult result = service.listCategories(null, 1, 20);

            assertNotNull(result);
            assertEquals(2, result.getItems().size());
            assertEquals(1, result.getPage());
            assertEquals(20, result.getSize());
            assertEquals(2, result.getTotal());
        }
    }

    @Nested
    @DisplayName("校验分类有效性")
    class ValidateCategoryTests {

        @Test
        @DisplayName("有效分类应该返回 valid=true")
        void shouldReturnValidForEnabledCategory() {
            ApiCategoryAggregate aggregate = createTestAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));

            CategoryValidityResult result = service.validateCategory(TEST_CODE);

            assertTrue(result.isValid());
            assertEquals(TEST_CODE, result.getCategoryCode());
            assertNull(result.getReason());
        }

        @Test
        @DisplayName("已停用分类应该返回 valid=false")
        void shouldReturnInvalidForDisabledCategory() {
            ApiCategoryAggregate aggregate = createDisabledAggregate();
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(aggregate));

            CategoryValidityResult result = service.validateCategory(TEST_CODE);

            assertFalse(result.isValid());
            assertEquals(TEST_CODE, result.getCategoryCode());
            assertNotNull(result.getReason());
            assertTrue(result.getReason().contains("disabled"));
        }

        @Test
        @DisplayName("不存在的分类应该返回 valid=false")
        void shouldReturnInvalidForNonExistentCategory() {
            when(repositoryPort.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

            CategoryValidityResult result = service.validateCategory(TEST_CODE);

            assertFalse(result.isValid());
            assertEquals(TEST_CODE, result.getCategoryCode());
            assertNotNull(result.getReason());
            assertTrue(result.getReason().contains("not found"));
        }
    }

    // -------------------- 辅助方法 --------------------

    private ApiCategoryAggregate createTestAggregate() {
        return ApiCategoryAggregate.reconstitute(
                CategoryId.of(TEST_ID),
                CategoryCode.of(TEST_CODE),
                TEST_NAME,
                CategoryStatus.ENABLED,
                Instant.now(),
                Instant.now(),
                false,
                0L
        );
    }

    private ApiCategoryAggregate createDisabledAggregate() {
        return ApiCategoryAggregate.reconstitute(
                CategoryId.of(TEST_ID),
                CategoryCode.of(TEST_CODE),
                TEST_NAME,
                CategoryStatus.DISABLED,
                Instant.now(),
                Instant.now(),
                false,
                0L
        );
    }
}
