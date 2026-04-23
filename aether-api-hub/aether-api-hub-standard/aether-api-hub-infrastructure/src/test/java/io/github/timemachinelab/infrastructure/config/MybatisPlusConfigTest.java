package io.github.timemachinelab.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * MyBatis-Plus plugin configuration tests.
 */
class MybatisPlusConfigTest {

    @Test
    @DisplayName("should register optimistic-lock plugin before pagination")
    void shouldRegisterOptimisticLockPluginBeforePagination() {
        MybatisPlusInterceptor interceptor = new MybatisPlusConfig().mybatisPlusInterceptor();

        assertEquals(2, interceptor.getInterceptors().size());
        assertInstanceOf(OptimisticLockerInnerInterceptor.class, interceptor.getInterceptors().get(0));
        PaginationInnerInterceptor pagination = assertInstanceOf(
                PaginationInnerInterceptor.class,
                interceptor.getInterceptors().get(1)
        );
        assertEquals(DbType.MYSQL, pagination.getDbType());
    }
}
