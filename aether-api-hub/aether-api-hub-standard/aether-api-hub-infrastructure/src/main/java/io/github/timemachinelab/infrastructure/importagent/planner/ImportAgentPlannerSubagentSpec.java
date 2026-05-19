package io.github.timemachinelab.infrastructure.importagent.planner;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registration metadata for planner-internal subagents.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ImportAgentPlannerSubagentSpec {

    String name();

    ImportAgentPlannerSubagentRole role();

    int order() default 0;
}