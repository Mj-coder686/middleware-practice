package com.mj.middleware.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.middleware.mp.entity.ProductMP;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductMapper
 *
 * 简单查询用 BaseMapper / IService 提供的方法即可
 * 复杂 SQL（聚合、JOIN、动态SQL）放到 XML 里写
 */
@Mapper
public interface ProductMapper extends BaseMapper<ProductMP> {

    /**
     * 按品牌统计商品数量
     * 复杂度：聚合 + 分组 → 放在 XML 里写
     */
    List<Map<String, Object>> countByBrand();

    /**
     * 批量涨价
     * 复杂度：price = price + ? 运算 → 放在 XML 里写
     */
    int batchUpdatePrice(@Param("category") String category,
                         @Param("increaseAmount") BigDecimal increaseAmount);

    /**
     * 多条件复杂查询
     * 复杂度：嵌套 AND/OR + 多字段 → 放在 XML 里用动态 SQL 更清晰
     */
    List<ProductMP> complexQuery(@Param("keyword") String keyword,
                                 @Param("brand") String brand,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("minStock") Integer minStock);
}
