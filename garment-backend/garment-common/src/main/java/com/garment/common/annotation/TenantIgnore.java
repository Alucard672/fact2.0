package com.garment.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 忽略租户隔离注解
 * 标记了此注解的方法或类将跳过租户数据隔离
 *
 * @author garment
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantIgnore {
    
    /**
     * 忽略原因说明
     */
    String value() default "";
}