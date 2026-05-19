package io.github.timemachinelab.infrastructure.importagent.planner;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registration metadata for an import-agent planning tool.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ImportAgentToolSpec {

    String name();

    PlannerStage stage();

    int order() default 0;
}