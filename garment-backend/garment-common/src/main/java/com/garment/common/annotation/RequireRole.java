package com.garment.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色检查注解
 *
 * @author garment
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 需要的角色代码
     */
    String[] value();
    
    /**
     * 权限关系：AND（需要所有角色）或 OR（需要任一角色）
     */
    LogicalOperator logical() default LogicalOperator.OR;
    
    /**
     * 逻辑操作符
     */
    enum LogicalOperator {
        AND, OR
    }
}