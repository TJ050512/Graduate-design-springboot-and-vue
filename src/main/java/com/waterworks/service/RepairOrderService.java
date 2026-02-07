package com.waterworks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.RepairOrder;

/**
 * 报修工单服务接口
 */
public interface RepairOrderService extends IService<RepairOrder> {

    /**
     * 分页查询报修工单列表
     *
     * @param page       页码
     * @param size       每页大小
     * @param userId     用户ID
     * @param orderNo    工单编号
     * @param repairType 报修类型
     * @param status     状态
     * @param handlerId  处理人ID
     * @return 报修工单列表
     */
    Page<RepairOrder> getRepairOrderPage(Integer page, Integer size, Long userId, String orderNo, 
                                          Integer repairType, Integer status, Long handlerId);

    /**
     * 创建报修工单
     *
     * @param repairOrder 报修工单信息
     * @return 是否成功
     */
    boolean createRepairOrder(RepairOrder repairOrder);

    /**
     * 更新报修工单
     *
     * @param repairOrder 报修工单信息
     * @return 是否成功
     */
    boolean updateRepairOrder(RepairOrder repairOrder);

    /**
     * 处理工单（开始处理）
     *
     * @param orderId   工单ID
     * @param handlerId 处理人ID
     * @return 是否成功
     */
    boolean handleOrder(Long orderId, Long handlerId);

    /**
     * 完成工单
     *
     * @param orderId      工单ID
     * @param handleResult 处理结果
     * @return 是否成功
     */
    boolean completeOrder(Long orderId, String handleResult);

    /**
     * 处理失败，转派工单
     *
     * @param orderId    工单ID
     * @param failReason 失败原因
     * @return 是否成功
     */
    boolean failOrder(Long orderId, String failReason);

    /**
     * 取消工单
     *
     * @param orderId 工单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId);

    /**
     * 用户反馈评价
     *
     * @param orderId  工单ID
     * @param feedback 反馈内容
     * @param rating   评分
     * @return 是否成功
     */
    boolean feedbackOrder(Long orderId, String feedback, Integer rating);
}
