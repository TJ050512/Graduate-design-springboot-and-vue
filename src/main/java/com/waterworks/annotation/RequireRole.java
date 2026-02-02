package com.waterworks.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 
 * @description 用于接口级别的角色权限控制
 * 
 * 使用示例：
 * - @RequireRole(roles = {1})          仅管理员可访问
 * - @RequireRole(roles = {1, 3})       管理员和抄表员可访问
 * - @RequireRole(roles = {1, 2, 3})    所有角色可访问
 * 
 * 角色定义：
 * - 1: 管理员
 * - 2: 普通用户
 * - 3: 抄表员
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /**
     * 允许访问的角色数组
     * 用户类型: 1-管理员 2-普通用户 3-抄表员
     */
    int[] roles() default {};

    /**
     * 权限描述（用于日志记录）
     */
    String description() default "";
}
