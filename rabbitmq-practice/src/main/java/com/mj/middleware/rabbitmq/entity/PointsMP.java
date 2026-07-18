package com.mj.middleware.rabbitmq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 积分实体 — 订单创建后通过 MQ 异步发放积分
 */
@Data
@Accessors(chain = true)
@TableName("t_points")
public class PointsMP {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 积分值（正数为获得，负数为扣减） */
    private Integer points;

    /**
     * 积分类型：1-订单获得 2-订单退还 3-手动调整
     */
    private Integer type;

    /**
     * 积分状态：0-待入账 1-已入账 2-已撤销
     */
    private Integer status;

    /** 备注 */
    private String description;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
