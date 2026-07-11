package com.mj.middleware.redis.service;

import com.mj.middleware.redis.entity.Product;

import java.util.List;

/**
 * 商品业务接口
 */
public interface IProductService {

    /** 发布商品（推送到关注者 Feed） */
    Long publish(Product product);

    /** 查询商品详情 */
    Product getById(Long id);

    /** 上架 */
    void putOnSale(Long productId);

    /** 下架 */
    void pullOffSale(Long productId);

    /** 收藏商品 */
    boolean like(Long productId, Long userId);

    /** 取消收藏 */
    boolean unlike(Long productId, Long userId);

    /** 查询商家的商品列表 */
    List<Product> getByMerchant(Long merchantId, int pageNum, int pageSize);

    /** 按分类查询商品（按销量排序） */
    List<Product> getByCategory(String category, int pageNum, int pageSize);
}
