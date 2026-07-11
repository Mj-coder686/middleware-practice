package com.mj.middleware.redis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 — 练习 Feed 推拉流
 *
 * 存储设计：
 *   hash    → product:{productId}               商品详情
 *   zset    → product:of:{merchantId}            商家商品列表（按时间排序）
 *   zset    → product:category:{category}        分类商品列表（按销量排序）
 *   set     → product:liked:{productId}          商品收藏用户集合
 *   string  → global:productId                   商品 ID 自增器
 *   list    → feed:{userId}                      推送到用户 Feed 流
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品 ID（Redis 自生成）")
    private Long id;

    @Schema(description = "商家 / 卖家用户 ID")
    private Long merchantId;

    @Schema(description = "商家昵称")
    private String merchantName;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品分类：food/clothing/electronics/...")
    private String category;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "商品图片（多张逗号分隔）")
    private String images;

    @Schema(description = "商品状态：0-下架 1-上架")
    private Integer status;

    @Schema(description = "收藏数")
    private Long likedCount;

    @Schema(description = "是否被当前用户收藏")
    private Boolean isLiked;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
