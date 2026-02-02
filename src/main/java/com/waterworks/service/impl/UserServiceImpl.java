package com.waterworks.service.impl;

import cn.hutool.core.util.IdUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 生成随机盐值
     */
    private String generateSalt() {
        return IdUtil.fastSimpleUUID().substring(0, 16);
    }

    /**
     * 使用盐值加密密码
     * 加密算法：MD5(MD5(password) + salt)
     */
    private String encryptPassword(String password, String salt) {
        return DigestUtil.md5Hex(DigestUtil.md5Hex(password) + salt);
    }

    /**
     * 验证密码（兼容旧的无盐密码）
     */
    private boolean verifyPassword(String inputPassword, User user) {
        if (StrUtil.isNotBlank(user.getSalt())) {
            // 新版加盐密码验证
            String encryptedInput = encryptPassword(inputPassword, user.getSalt());
            return encryptedInput.equals(user.getPassword());
        } else {
            // 兼容旧版无盐密码（纯MD5）
            String oldEncrypt = DigestUtil.md5Hex(inputPassword);
            return oldEncrypt.equals(user.getPassword());
        }
    }

    @Override
    public String login(String username, String password) {
        // 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证密码（兼容新旧两种加密方式）
        if (!verifyPassword(password, user)) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_FORBIDDEN);
        }

        // 如果是旧密码（无盐），自动升级为加盐密码
        if (StrUtil.isBlank(user.getSalt())) {
            log.info("用户 {} 密码升级为加盐存储", username);
            String salt = generateSalt();
            String newPassword = encryptPassword(password, salt);
            user.setSalt(salt);
            user.setPassword(newPassword);
            this.updateById(user);
        }

        // 生成token（包含用户类型，用于权限控制）
        return JwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getUserType());
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

        // 生成盐值并加密密码
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(encryptPassword(user.getPassword(), salt));
        
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

        // 验证旧密码（兼容新旧两种加密方式）
        if (!verifyPassword(oldPassword, user)) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 生成新盐值并加密新密码
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(encryptPassword(newPassword, salt));
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 生成新盐值并重置为默认密码123456
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(encryptPassword("123456", salt));
        return this.updateById(user);
    }
}


