package com.waterworks.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.User;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.UserMapper;
import com.waterworks.service.UserService;
import com.waterworks.utils.JwtUtil;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public String login(String username, String password) {
        // 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证密码
        String encryptPassword = DigestUtil.md5Hex(password);
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_FORBIDDEN);
        }

        // 生成token
        return JwtUtil.generateToken(user.getUserId(), user.getUsername());
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public Page<User> getUserPage(Integer page, Integer size, String username, Integer userType, Integer status) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(User::getUsername, username)
                   .or()
                   .like(User::getRealName, username);
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(userPage, wrapper);
    }

    @Override
    public boolean addUser(User user) {
        // 检查用户名是否存在
        User existUser = getUserByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_HAS_EXISTED);
        }

        // 密码加密
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        
        return this.save(user);
    }

    @Override
    public boolean updateUser(User user) {
        User existUser = this.getById(user.getUserId());
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 如果修改了用户名，检查是否重复
        if (!existUser.getUsername().equals(user.getUsername())) {
            User checkUser = getUserByUsername(user.getUsername());
            if (checkUser != null) {
                throw new BusinessException(ResultCode.USER_HAS_EXISTED);
            }
        }

        return this.updateById(user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证旧密码
        String encryptOldPassword = DigestUtil.md5Hex(oldPassword);
        if (!encryptOldPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(DigestUtil.md5Hex(newPassword));
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 重置为默认密码123456
        user.setPassword(DigestUtil.md5Hex("123456"));
        return this.updateById(user);
    }
}


