package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.User;
import com.waterworks.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        if (user != null) {
            // 清除密码敏感信息
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageResult<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status) {
        Page<User> userPage = userService.getUserPage(page, size, username, userType, status);
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
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "添加用户")
    @PostMapping
    public Result<Boolean> addUser(@Valid @RequestBody User user) {
        boolean result = userService.addUser(user);
        return Result.success(result);
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Boolean> updateUser(@Valid @RequestBody User user) {
        boolean result = userService.updateUser(user);
        return Result.success(result);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        boolean result = userService.removeById(id);
        return Result.success(result);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/changePassword")
    public Result<Boolean> changePassword(@RequestBody Map<String, String> passwordInfo) {
        Long userId = Long.valueOf(passwordInfo.get("userId"));
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");
        boolean result = userService.changePassword(userId, oldPassword, newPassword);
        return Result.success(result);
    }

    @Operation(summary = "重置密码")
    @PutMapping("/resetPassword/{id}")
    public Result<Boolean> resetPassword(@PathVariable Long id) {
        boolean result = userService.resetPassword(id);
        return Result.success(result);
    }
}


