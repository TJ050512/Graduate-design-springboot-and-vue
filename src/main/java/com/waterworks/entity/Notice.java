package com.waterworks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    private Long noticeId;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型(1:系统公告 2:停水通知 3:价格调整)
     */
    private Integer noticeType;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 状态(0:草稿 1:已发布 2:已撤回)
     */
    private Integer status;

    /**
     * 是否置顶(0:否 1:是)
     */
    private Integer isTop;
}


