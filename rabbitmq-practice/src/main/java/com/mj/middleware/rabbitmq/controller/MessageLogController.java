package com.mj.middleware.rabbitmq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mj.middleware.common.result.Result;
import com.mj.middleware.rabbitmq.entity.MessageLogMP;
import com.mj.middleware.rabbitmq.service.IMessageLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息日志接口 — 记录 RabbitMQ 消息发送与消费状态
 */
@Tag(name = "消息日志管理", description = "消息日志 CRUD 接口")
@RestController
@RequestMapping("/message-log")
@RequiredArgsConstructor
public class MessageLogController {

    private final IMessageLogService messageLogService;

    @Operation(summary = "记录消息日志")
    @PostMapping
    public Result<Void> addLog(@RequestBody MessageLogMP messageLog) {
        messageLogService.addLog(messageLog);
        return Result.success();
    }

    @Operation(summary = "根据 ID 查询消息日志")
    @GetMapping("/{id}")
    public Result<MessageLogMP> getLog(@PathVariable Long id) {
        return Result.success(messageLogService.get(id));
    }

    @Operation(summary = "根据消息ID查询")
    @GetMapping("/by-message-id/{messageId}")
    public Result<MessageLogMP> getByMessageId(@PathVariable String messageId) {
        return Result.success(messageLogService.getByMessageId(messageId));
    }

    @Operation(summary = "更新消息状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        messageLogService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "分页查询消息日志")
    @GetMapping("/page")
    public Result<IPage<MessageLogMP>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageLogService.listLog(pageNum, pageSize));
    }

    @Operation(summary = "查询待重试的消息")
    @GetMapping("/retryable")
    public Result<List<MessageLogMP>> listRetryable() {
        return Result.success(messageLogService.listRetryable());
    }
}
