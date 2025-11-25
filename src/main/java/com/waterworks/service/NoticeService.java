package com.waterworks.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.waterworks.entity.Notice;

/**
 * 公告服务接口
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 分页查询公告列表
     *
     * @param page       页码
     * @param size       每页大小
     * @param title      标题
     * @param noticeType 公告类型
     * @param status     状态
     * @return 公告列表
     */
    Page<Notice> getNoticePage(Integer page, Integer size, String title, Integer noticeType, Integer status);

    /**
     * 发布公告
     *
     * @param noticeId 公告ID
     * @return 是否成功
     */
    boolean publishNotice(Long noticeId);
}


