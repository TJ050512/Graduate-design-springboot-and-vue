package com.waterworks.interceptor;

import cn.hutool.core.util.StrUtil;
import com.waterworks.common.ResultCode;
import com.waterworks.exception.BusinessException;
import com.waterworks.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取token
        String token = request.getHeader("Authorization");
        
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }

        // 将用户信息存入request中（用于权限校验）
        Long userId = JwtUtil.getUserIdFromToken(token);
        String username = JwtUtil.getUsernameFromToken(token);
        Integer userType = JwtUtil.getUserTypeFromToken(token);
        
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("userType", userType);

        log.debug("JWT拦截器：用户{}({})通过验证，角色类型：{}", username, userId, userType);

        return true;
    }
}


