package com.mj.middleware.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.mp.entity.ProductMP;
import com.mj.middleware.mp.mapper.ProductMapper;
import com.mj.middleware.mp.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductServiceImpl
 *
 * 分层原则：
 * - Service 负责业务编排（参数校验、组合调用、事务控制）
 * - Mapper 负责数据访问（SQL 放在 XML 里，Java 代码不出现 SQL 片段）
 *
 * 简单查询直接用 IService 内置方法（lambdaQuery / lambdaUpdate）
 * 复杂查询/聚合/动态SQL → 调用 Mapper 自定义方法（SQL 写在 XML 里）
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductMP> implements IProductService {

    /**
     * 按分类和价格范围查询
     * 简单条件 → 用 lambdaQuery 即可（不涉及 SQL 字符串）
     */
    @Override
    public List<ProductMP> queryByCondition(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        return lambdaQuery()
                .eq(category != null && !category.isEmpty(), ProductMP::getCategory, category)
                .ge(minPrice != null, ProductMP::getPrice, minPrice)
                .le(maxPrice != null, ProductMP::getPrice, maxPrice)
                .eq(ProductMP::getStatus, 1)
                .orderByDesc(ProductMP::getSales)
                .list();
    }

    /**
     * 按品牌统计商品数量
     * 聚合 + GROUP BY → Mapper XML
     */
    @Override
    public List<Map<String, Object>> countByBrand() {
        return baseMapper.countByBrand();
    }

    /**
     * 多条件复杂查询
     * 嵌套 AND/OR + 动态条件 → Mapper XML
     */
    @Override
    public List<ProductMP> complexQuery(String keyword, String brand, BigDecimal minPrice, Integer minStock) {
        return baseMapper.complexQuery(keyword, brand, minPrice, minStock);
    }

    /**
     * 乐观锁更新
     * 先查 → 改字段 → updateById（MP 内置，自动带 WHERE version = ?）
     */
    @Override
    public boolean updateWithOptimisticLock(Long id, BigDecimal newPrice) {
        ProductMP product = getById(id);
        if (product == null) {
            log.warn("商品不存在: id={}", id);
            return false;
        }
        product.setPrice(newPrice);
        boolean result = updateById(product);
        log.info("乐观锁更新 [{}]: price={}, version={}, result={}",
                product.getName(), newPrice, product.getVersion(), result);
        return result;
    }

    /**
     * 条件批量更新
     * price = price + ? 运算 → Mapper XML
     */
    @Override
    public boolean batchUpdatePrice(String category, BigDecimal increaseAmount) {
        int rows = baseMapper.batchUpdatePrice(category, increaseAmount);
        log.info("批量涨价: category={}, amount={}, 影响行数={}", category, increaseAmount, rows);
        return rows > 0;
    }
}
