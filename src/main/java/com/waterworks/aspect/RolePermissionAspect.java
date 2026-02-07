package com.waterworks.aspect;

import com.waterworks.annotation.RequireRole;
import com.waterworks.common.ResultCode;
import com.waterworks.exception.BusinessException;
import com.waterworks.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 角色权限检查切面
 * 
 * @description 拦截带有 @RequireRole 注解的方法，进行角色权限校验
 */
@Slf4j
@Aspect
@Component
public class RolePermissionAspect {

    /**
     * 环绕通知：拦截带有 @RequireRole 注解的方法
     */
    @Around("@annotation(com.waterworks.annotation.RequireRole)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 获取允许的角色列表
        int[] allowedRoles = requireRole.roles();
        
        // 如果没有配置角色限制，直接放行
        if (allowedRoles.length == 0) {
            return joinPoint.proceed();
        }

        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "无法获取请求信息");
        }

        HttpServletRequest request = attributes.getRequest();
        
        // 从请求属性中获取用户类型（在 JwtInterceptor 中已设置）
        Object userTypeObj = request.getAttribute("userType");
        
        // 如果属性中没有，尝试从 token 中获取
        if (userTypeObj == null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    userTypeObj = JwtUtil.getUserTypeFromToken(token);
                } catch (Exception e) {
                    log.error("解析token获取用户类型失败", e);
                    throw new BusinessException(ResultCode.TOKEN_INVALID);
                }
            }
        }

        if (userTypeObj == null) {
            log.warn("权限校验失败：无法获取用户类型，接口: {}", method.getName());
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        int userType = Integer.parseInt(userTypeObj.toString());

        // 检查用户角色是否在允许列表中
        boolean hasPermission = Arrays.stream(allowedRoles).anyMatch(role -> role == userType);

        if (!hasPermission) {
            String roleName = getRoleName(userType);
            String allowedRoleNames = getRoleNames(allowedRoles);
            log.warn("权限校验失败：用户角色[{}]不在允许列表[{}]中，接口: {}", 
                    roleName, allowedRoleNames, method.getName());
            throw new BusinessException(ResultCode.NO_PERMISSION.getCode(), 
                    "权限不足，该功能仅限" + allowedRoleNames + "访问");
        }

        log.debug("权限校验通过：用户角色[{}]，接口: {}", getRoleName(userType), method.getName());
        
        return joinPoint.proceed();
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(int userType) {
        switch (userType) {
            case 1: return "管理员";
            case 2: return "普通用户";
            case 3: return "抄表员";
            case 4: return "维修人员";
            default: return "未知角色";
        }
    }

    /**
     * 获取角色名称列表
     */
    private String getRoleNames(int[] roles) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roles.length; i++) {
            if (i > 0) {
                sb.append("、");
            }
            sb.append(getRoleName(roles[i]));
        }
        return sb.toString();
    }
}
