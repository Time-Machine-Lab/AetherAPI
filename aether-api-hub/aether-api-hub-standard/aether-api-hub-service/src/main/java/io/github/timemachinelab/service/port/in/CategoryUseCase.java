package io.github.timemachinelab.service.port.in;

import io.github.timemachinelab.domain.catalog.model.CategoryStatus;
import io.github.timemachinelab.service.model.CategoryModel;
import io.github.timemachinelab.service.model.CategoryPageResult;
import io.github.timemachinelab.service.model.CategoryValidityResult;
import io.github.timemachinelab.service.model.CreateCategoryCommand;
import io.github.timemachinelab.service.model.RenameCategoryCommand;

/**
 * 分类管理入站端口（用例接口）。
 */
public interface CategoryUseCase {

    /**
     * 创建新分类。
     *
     * @param command 创建命令
     * @return 创建成功的分类模型
     */
    CategoryModel createCategory(CreateCategoryCommand command);

    /**
     * 重命名分类。
     *
     * @param command 重命名命令
     * @return 重命名后的分类模型
     */
    CategoryModel renameCategory(RenameCategoryCommand command);

    /**
     * 启用分类。
     *
     * @param categoryCode 分类编码
     * @return 启用后的分类模型
     */
    CategoryModel enableCategory(String categoryCode);

    /**
     * 停用分类。
     *
     * @param categoryCode 分类编码
     * @return 停用后的分类模型
     */
    CategoryModel disableCategory(String categoryCode);

    /**
     * 根据编码查询分类详情。
     *
     * @param categoryCode 分类编码
     * @return 分类模型（如果存在）
     */
    CategoryModel getCategoryByCode(String categoryCode);

    /**
     * 分页查询分类列表。
     *
     * @param status 按状态过滤（可选）
     * @param page   页码（从 1 开始）
     * @param size   每页记录数
     * @return 分类分页结果
     */
    CategoryPageResult listCategories(CategoryStatus status, int page, int size);

    /**
     * 校验分类有效性（供下游资产模块调用）。
     *
     * @param categoryCode 分类编码
     * @return 有效性校验结果
     */
    CategoryValidityResult validateCategory(String categoryCode);
}
