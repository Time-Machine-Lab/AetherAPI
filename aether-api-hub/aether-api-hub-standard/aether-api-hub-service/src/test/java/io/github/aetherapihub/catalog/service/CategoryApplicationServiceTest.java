package io.github.aetherapihub.catalog.service;

import io.github.aetherapihub.catalog.domain.model.*;
import io.github.aetherapihub.catalog.service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 分类应用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private ApiCategoryRepository repository;

    private CategoryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CategoryApplicationService(repository);
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        when(repository.existsByCode(any(CategoryCode.class))).thenReturn(false);
        when(repository.save(any(ApiCategoryAggregate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryModel result = service.createCategory(
                new CreateCategoryCommand("llm", "大语言模型"));

        assertNotNull(result);
        assertEquals("llm", result.getCode().getValue());
        assertEquals("大语言模型", result.getName());
        assertEquals(CategoryStatus.ENABLED, result.getStatus());
        verify(repository).save(any(ApiCategoryAggregate.class));
    }

    @Test
    void shouldRejectDuplicateCategoryCode() {
        when(repository.existsByCode(any(CategoryCode.class))).thenReturn(true);

        CategoryDomainException ex = assertThrows(CategoryDomainException.class,
                () -> service.createCategory(new CreateCategoryCommand("llm", "大语言模型")));

        assertEquals("CATEGORY_CODE_DUPLICATE", ex.getErrorCode());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldGetCategoryByCode() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型");
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));

        Optional<CategoryModel> result = service.getByCode("llm");

        assertTrue(result.isPresent());
        assertEquals("llm", result.get().getCode().getValue());
    }

    @Test
    void shouldReturnEmptyForDeletedCategory() {
        ApiCategoryAggregate deleted = newAggregate("llm", "大语言模型");
        deleted.disable();
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(deleted));

        Optional<CategoryModel> result = service.getByCode("llm");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRenameCategory() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型");
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));
        when(repository.save(any(ApiCategoryAggregate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryModel result = service.renameCategory(
                new RenameCategoryCommand("llm", "大模型"));

        assertEquals("大模型", result.getName());
    }

    @Test
    void shouldEnableCategory() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型", CategoryStatus.DISABLED);
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));
        when(repository.save(any(ApiCategoryAggregate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryModel result = service.enableCategory("llm");

        assertEquals(CategoryStatus.ENABLED, result.getStatus());
    }

    @Test
    void shouldDisableCategory() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型");
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));
        when(repository.save(any(ApiCategoryAggregate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CategoryModel result = service.disableCategory("llm");

        assertEquals(CategoryStatus.DISABLED, result.getStatus());
    }

    @Test
    void shouldReturnNotFoundForMissingCategoryOnRename() {
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

        CategoryDomainException ex = assertThrows(CategoryDomainException.class,
                () -> service.renameCategory(new RenameCategoryCommand("llm", "大模型")));

        assertEquals("CATEGORY_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void shouldCheckValidityAsValid() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型");
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));

        CategoryValidityResult result = service.checkValidity("llm");

        assertTrue(result.isValid());
        assertNull(result.getReason());
    }

    @Test
    void shouldCheckValidityAsInvalidWhenNotFound() {
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.empty());

        CategoryValidityResult result = service.checkValidity("llm");

        assertFalse(result.isValid());
        assertEquals("分类不存在或已删除", result.getReason());
    }

    @Test
    void shouldCheckValidityAsInvalidWhenDisabled() {
        ApiCategoryAggregate category = newAggregate("llm", "大语言模型", CategoryStatus.DISABLED);
        when(repository.findByCode(any(CategoryCode.class))).thenReturn(Optional.of(category));

        CategoryValidityResult result = service.checkValidity("llm");

        assertFalse(result.isValid());
        assertEquals("分类已停用，不得被新资产引用", result.getReason());
    }

    private ApiCategoryAggregate newAggregate(String code, String name) {
        return newAggregate(code, name, CategoryStatus.ENABLED);
    }

    private ApiCategoryAggregate newAggregate(String code, String name, CategoryStatus status) {
        return new ApiCategoryAggregate(
                new CategoryId(UUID.randomUUID().toString()),
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
