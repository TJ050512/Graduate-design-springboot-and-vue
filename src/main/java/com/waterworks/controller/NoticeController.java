package com.waterworks.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.waterworks.common.PageResult;
import com.waterworks.common.Result;
import com.waterworks.entity.Notice;
import com.waterworks.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 公告控制器
 */
@Tag(name = "公告管理")
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Operation(summary = "分页查询公告列表")
    @GetMapping("/page")
    public Result<PageResult<Notice>> getNoticePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status) {
        Page<Notice> noticePage = noticeService.getNoticePage(page, size, title, noticeType, status);
        PageResult<Notice> pageResult = PageResult.build(
                noticePage.getCurrent(),
                noticePage.getSize(),
                noticePage.getTotal(),
                noticePage.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "根据ID查询公告")
    @GetMapping("/{id}")
    public Result<Notice> getNoticeById(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    @Operation(summary = "添加公告")
    @PostMapping
    public Result<Boolean> addNotice(@Valid @RequestBody Notice notice) {
        boolean result = noticeService.save(notice);
        return Result.success(result);
    }

    @Operation(summary = "更新公告")
    @PutMapping
    public Result<Boolean> updateNotice(@Valid @RequestBody Notice notice) {
        boolean result = noticeService.updateById(notice);
        return Result.success(result);
    }

    @Operation(summary = "发布公告")
    @PutMapping("/publish/{id}")
    public Result<Boolean> publishNotice(@PathVariable Long id) {
        boolean result = noticeService.publishNotice(id);
        return Result.success(result);
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        boolean result = noticeService.removeById(id);
        return Result.success(result);
    }
}


