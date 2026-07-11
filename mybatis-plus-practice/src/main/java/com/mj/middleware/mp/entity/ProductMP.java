package com.mj.middleware.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 — 练习查询与分页
 */
@Data
@Accessors(chain = true)
@TableName("t_product")
public class ProductMP {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private String brand;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private Integer sales;

    private Double score;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
