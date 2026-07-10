package com.mj.middleware.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 — ES 搜索/Redis 缓存 示例
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private Integer sales;       // 销量
    private Double score;        // 评分
    private Integer status;      // 0-下架 1-上架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
