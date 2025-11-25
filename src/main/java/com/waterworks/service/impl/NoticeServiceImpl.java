package com.waterworks.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.waterworks.common.ResultCode;
import com.waterworks.entity.Notice;
import com.waterworks.exception.BusinessException;
import com.waterworks.mapper.NoticeMapper;
import com.waterworks.service.NoticeService;
import org.springframework.stereotype.Service;

/**
 * 公告服务实现类
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public Page<Notice> getNoticePage(Integer page, Integer size, String title, Integer noticeType, Integer status) {
        Page<Notice> noticePage = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(title)) {
            wrapper.like(Notice::getTitle, title);
        }
        if (noticeType != null) {
            wrapper.eq(Notice::getNoticeType, noticeType);
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        
        wrapper.orderByDesc(Notice::getIsTop)
               .orderByDesc(Notice::getCreateTime);
        return this.page(noticePage, wrapper);
    }

    @Override
    public boolean publishNotice(Long noticeId) {
        Notice notice = this.getById(noticeId);
        if (notice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }

        notice.setStatus(1);
        return this.updateById(notice);
    }
}


