package com.waterworks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.WaterUsage;

/**
 * 用水记录服务接口
 */
public interface WaterUsageService extends IService<WaterUsage> {

    /**
     * 分页查询用水记录列表
     *
     * @param page      页码
     * @param size      每页大小
     * @param userId    用户ID
     * @param meterId   水表ID
     * @param readMonth 抄表月份
     * @param status    状态
     * @return 用水记录列表
     */
    Page<WaterUsage> getUsagePage(Integer page, Integer size, Long userId, Long meterId, String readMonth, Integer status);

    /**
     * 添加用水记录
     *
     * @param waterUsage 用水记录
     * @return 是否成功
     */
    boolean addUsage(WaterUsage waterUsage);

    /**
     * 更新用水记录
     *
     * @param waterUsage 用水记录
     * @return 是否成功
     */
    boolean updateUsage(WaterUsage waterUsage);

    /**
     * 确认用水记录
     *
     * @param usageId 记录ID
     * @return 是否成功
     */
    boolean confirmUsage(Long usageId);
}


