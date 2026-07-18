package com.mj.middleware.rabbitmq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息日志实体 — 记录 RabbitMQ 消息的发送与消费状态
 */
@Data
@Accessors(chain = true)
@TableName("t_message_log")
public class MessageLogMP {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 消息ID（唯一标识） */
    private String messageId;

    /** 交换机名称 */
    private String exchange;

    /** 路由键 */
    private String routingKey;

    /** 消息内容 */
    private String content;

    /**
     * 消息状态：0-发送中 1-发送成功 2-发送失败 3-已消费
     */
    private Integer status;

    /** 重试次数 */
    private Integer retryCount;

    /** 下次重试时间 */
    private LocalDateTime nextRetryTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
