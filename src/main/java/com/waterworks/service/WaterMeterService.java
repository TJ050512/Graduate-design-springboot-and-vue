package com.waterworks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.WaterMeter;

import java.util.List;

/**
 * 水表服务接口
 */
public interface WaterMeterService extends IService<WaterMeter> {

    /**
     * 分页查询水表列表
     *
     * @param page      页码
     * @param size      每页大小
     * @param meterNo   水表编号
     * @param userId    用户ID
     * @param meterType 水表类型
     * @param status    状态
     * @return 水表列表
     */
    Page<WaterMeter> getMeterPage(Integer page, Integer size, String meterNo, Long userId, Integer meterType, Integer status);

    /**
     * 根据水表编号查询水表
     *
     * @param meterNo 水表编号
     * @return 水表信息
     */
    WaterMeter getMeterByNo(String meterNo);

    /**
     * 添加水表
     *
     * @param waterMeter 水表信息
     * @return 是否成功
     */
    boolean addMeter(WaterMeter waterMeter);

    /**
     * 更新水表
     *
     * @param waterMeter 水表信息
     * @return 是否成功
     */
    boolean updateMeter(WaterMeter waterMeter);

    /**
     * 根据用户ID查询水表列表
     *
     * @param userId 用户ID
     * @return 水表列表
     */
    List<WaterMeter> getMetersByUserId(Long userId);
}


