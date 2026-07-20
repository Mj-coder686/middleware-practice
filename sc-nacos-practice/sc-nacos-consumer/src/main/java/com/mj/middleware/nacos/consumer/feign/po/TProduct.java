package com.mj.middleware.nacos.consumer.feign.po;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品 PO — 仅用于 Feign 调用的 JSON 反序列化
 * 不需要 MyBatis-Plus 注解（Consumer 没有 MyBatis 依赖）
 */
@Data
public class TProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private Integer sales;
    private Double score;
    /** 0-下架 1-上架 */
    private Integer status;
    private Integer deleted;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
