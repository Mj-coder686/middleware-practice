package com.mj.middleware.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.mp.entity.ProductMP;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductService — 查询构造器全面演示
 */
public interface IProductService extends IService<ProductMP> {

    /** 按分类和价格范围查询 — LambdaQueryWrapper 链式条件 */
    List<ProductMP> queryByCondition(String category, BigDecimal minPrice, BigDecimal maxPrice);

    /** 按品牌统计商品数量 — QueryWrapper + select + groupBy */
    List<Map<String, Object>> countByBrand();

    /** 多条件复杂查询 — 嵌套 AND/OR 条件 */
    List<ProductMP> complexQuery(String keyword, String brand, BigDecimal minPrice, Integer minStock);

    /** 乐观锁更新 — @Version 机制 */
    boolean updateWithOptimisticLock(Long id, BigDecimal newPrice);

    /** 条件批量更新 — LambdaUpdateWrapper + setSql */
    boolean batchUpdatePrice(String category, BigDecimal increaseAmount);
}
