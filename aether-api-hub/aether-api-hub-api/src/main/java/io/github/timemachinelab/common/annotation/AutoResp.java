package io.github.timemachinelab.common.annotation;

import io.temana.foundation.web.result.AutoResp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 响应自动包装注解。
 *
 * <p>此注解是 TML-SDK {@link io.temana.foundation.web.result.AutoResp} 的别名，
 * 统一使用项目自己的包名空间，方便后续可能的迁移。
 *
 * <p>使用方式：
 * <pre>
 * {@code @AutoResp}
 * {@code @RestController}
 * public class MyController {
 *     // 所有方法返回值自动包装为 Result
 * }
 * </pre>
 *
 * @see io.temana.foundation.web.result.AutoResp
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AutoResp
public @interface AutoResp {
}
