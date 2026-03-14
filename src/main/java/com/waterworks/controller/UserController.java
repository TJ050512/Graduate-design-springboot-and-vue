package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.annotation.RequireRole;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.User;
import com.waterworks.exception.BusinessException;
import com.waterworks.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        String token = userService.login(username, password);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 从token中解析用户名
        String username = com.waterworks.utils.JwtUtil.getUsernameFromToken(token);
        // 根据用户名查询用户信息
        User user = userService.getUserByUsername(username);
        sanitizeUser(user);
        return Result.success(user);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    @RequireRole(roles = {1, 3, 4}, description = "管理员、抄表员和维修人员可查询用户列表")
    public Result<PageResult<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status) {
        Page<User> userPage = userService.getUserPage(page, size, username, userType, status);
        userPage.getRecords().forEach(this::sanitizeUser);
        PageResult<User> pageResult = PageResult.build(
                userPage.getCurrent(),
                userPage.getSize(),
                userPage.getTotal(),
                userPage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{id}")
    @RequireRole(roles = {1, 2, 3, 4}, description = "登录用户可查询用户信息，非管理员仅可查询本人")
    public Result<User> getUserById(@PathVariable Long id, HttpServletRequest request) {
        requireSelfOrAdmin(id, request);
        User user = userService.getById(id);
        sanitizeUser(user);
        return Result.success(user);
    }

    @Operation(summary = "添加用户")
    @PostMapping
    @RequireRole(roles = {1}, description = "仅管理员可添加用户")
    public Result<Boolean> addUser(@Valid @RequestBody User user) {
        boolean result = userService.addUser(user);
        return Result.success(result);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    @RequireRole(roles = {1}, description = "仅管理员可更新用户")
    public Result<Boolean> updateUser(@Valid @RequestBody User user) {
        boolean result = userService.updateUser(user);
        return Result.success(result);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @RequireRole(roles = {1}, description = "仅管理员可删除用户")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.removeById(id);
        return Result.success(result);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/changePassword")
    @RequireRole(roles = {1, 2, 3, 4}, description = "登录用户可修改自己的密码")
    public Result<Boolean> changePassword(@RequestBody Map<String, String> passwordInfo, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");
        boolean result = userService.changePassword(userId, oldPassword, newPassword);
        return Result.success(result);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/resetPassword/{id}")
    @RequireRole(roles = {1}, description = "仅管理员可重置密码")
    public Result<Boolean> resetPassword(@PathVariable Long id) {
        boolean result = userService.resetPassword(id);
        return Result.success(result);
    }

    private void sanitizeUser(User user) {
        if (user == null) {
            return;
        }
        user.setPassword(null);
        user.setSalt(null);
    }

    private void requireSelfOrAdmin(Long targetUserId, HttpServletRequest request) {
        Integer userType = getCurrentUserType(request);
        Long userId = getCurrentUserId(request);
        if (userType != 1 && !userId.equals(targetUserId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return Long.valueOf(userIdObj.toString());
    }

    private Integer getCurrentUserType(HttpServletRequest request) {
        Object userTypeObj = request.getAttribute("userType");
        if (userTypeObj == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return Integer.valueOf(userTypeObj.toString());
    }
}

