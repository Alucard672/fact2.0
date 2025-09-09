package com.garment.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 *
 * @author garment
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 需要的权限代码
     */
    String[] value();
    
    /**
     * 权限关系：AND（需要所有权限）或 OR（需要任一权限）
     */
    LogicalOperator logical() default LogicalOperator.AND;
    
    /**
     * 逻辑操作符
     */
    enum LogicalOperator {
        AND, OR
    }
}