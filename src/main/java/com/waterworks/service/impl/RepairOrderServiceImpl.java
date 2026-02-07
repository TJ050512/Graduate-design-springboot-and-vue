package com.waterworks.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.RepairOrder;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.RepairOrderMapper;
import com.waterworks.entity.User;
import com.waterworks.mapper.UserMapper;
import com.waterworks.service.RepairOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 报修工单服务实现类
 */
@Slf4j
@Service
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder> implements RepairOrderService {

    @Autowired
    private UserMapper userMapper;

    private String getUserName(Long userId) {
        if (userId == null) return "未知";
        User user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : "未知";
    }

    @Override
    public Page<RepairOrder> getRepairOrderPage(Integer page, Integer size, Long userId, String orderNo,
                                                 Integer repairType, Integer status, Long handlerId) {
        Page<RepairOrder> repairOrderPage = new Page<>(page, size);
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(RepairOrder::getUserId, userId);
        }
        if (StrUtil.isNotBlank(orderNo)) {
            wrapper.like(RepairOrder::getOrderNo, orderNo);
        }
        if (repairType != null) {
            wrapper.eq(RepairOrder::getRepairType, repairType);
        }
        if (status != null) {
            wrapper.eq(RepairOrder::getStatus, status);
        }
        if (handlerId != null) {
            wrapper.eq(RepairOrder::getHandlerId, handlerId);
        }
        
        // 先按状态排序（待处理和处理中的排前面），再按优先级排序（紧急的排前面），最后按创建时间倒序
        wrapper.orderByAsc(RepairOrder::getStatus);
        wrapper.orderByAsc(RepairOrder::getPriority);
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        return this.page(repairOrderPage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRepairOrder(RepairOrder repairOrder) {
        // 生成工单编号
        repairOrder.setOrderNo("RO" + IdUtil.getSnowflakeNextIdStr());
        // 默认状态为待处理
        repairOrder.setStatus(0);
        // 默认优先级为普通
        if (repairOrder.getPriority() == null) {
            repairOrder.setPriority(2);
        }
        
        log.info("创建报修工单: {}", repairOrder.getOrderNo());
        return this.save(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRepairOrder(RepairOrder repairOrder) {
        RepairOrder existOrder = this.getById(repairOrder.getOrderId());
        if (existOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        // 已完成或已取消的工单不能修改
        if (existOrder.getStatus() == 2 || existOrder.getStatus() == 3) {
            throw new BusinessException("已完成或已取消的工单不能修改");
        }
        
        return this.updateById(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleOrder(Long orderId, Long handlerId) {
        RepairOrder repairOrder = this.getById(orderId);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        if (repairOrder.getStatus() != 0) {
            throw new BusinessException("只有待处理的工单才能开始处理");
        }
        
        repairOrder.setStatus(1);
        repairOrder.setHandlerId(handlerId);
        repairOrder.setHandleTime(LocalDateTime.now());
        
        log.info("开始处理工单: {}, 处理人: {}", repairOrder.getOrderNo(), handlerId);
        return this.updateById(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, String handleResult) {
        RepairOrder repairOrder = this.getById(orderId);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        if (repairOrder.getStatus() != 1) {
            throw new BusinessException("只有处理中的工单才能完成");
        }
        
        // 拼接处理人和时间到处理结果
        String handlerName = getUserName(repairOrder.getHandlerId());
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String previousResult = repairOrder.getHandleResult();
        String newResult = "【处理完成 - " + handlerName + " " + time + "】" + handleResult;
        if (StrUtil.isNotBlank(previousResult)) {
            newResult = previousResult + "\n" + newResult;
        }
        
        repairOrder.setStatus(2);
        repairOrder.setHandleResult(newResult);
        repairOrder.setCompleteTime(LocalDateTime.now());
        
        log.info("完成工单: {}", repairOrder.getOrderNo());
        return this.updateById(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean failOrder(Long orderId, String failReason) {
        RepairOrder repairOrder = this.getById(orderId);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        if (repairOrder.getStatus() != 1) {
            throw new BusinessException("只有处理中的工单才能标记为处理失败");
        }
        
        // 记录失败原因，附上处理人和时间
        String handlerName = getUserName(repairOrder.getHandlerId());
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String previousResult = repairOrder.getHandleResult();
        String newResult = "【处理失败 - " + handlerName + " " + time + "】" + failReason;
        if (StrUtil.isNotBlank(previousResult)) {
            newResult = previousResult + "\n" + newResult;
        }
        
        // 工单回到待处理状态，等待其他维修人员处理
        repairOrder.setStatus(0);
        repairOrder.setHandleResult(newResult);
        // 清除当前处理人，允许其他人接单
        repairOrder.setHandlerId(null);
        repairOrder.setHandleTime(null);
        
        log.info("工单处理失败，转派: {}, 原因: {}", repairOrder.getOrderNo(), failReason);
        return this.updateById(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        RepairOrder repairOrder = this.getById(orderId);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        // 已完成的工单不能取消
        if (repairOrder.getStatus() == 2) {
            throw new BusinessException("已完成的工单不能取消");
        }
        
        repairOrder.setStatus(3);
        
        log.info("取消工单: {}", repairOrder.getOrderNo());
        return this.updateById(repairOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean feedbackOrder(Long orderId, String feedback, Integer rating) {
        RepairOrder repairOrder = this.getById(orderId);
        if (repairOrder == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "工单不存在");
        }
        
        if (repairOrder.getStatus() != 2) {
            throw new BusinessException("只有已完成的工单才能评价");
        }
        
        if (repairOrder.getRating() != null) {
            throw new BusinessException("该工单已评价，不能重复评价");
        }
        
        repairOrder.setFeedback(feedback);
        repairOrder.setRating(rating);
        
        log.info("工单评价: {}, 评分: {}", repairOrder.getOrderNo(), rating);
        return this.updateById(repairOrder);
    }
}
