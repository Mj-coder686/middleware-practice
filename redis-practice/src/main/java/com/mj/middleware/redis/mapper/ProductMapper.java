package com.mj.middleware.redis.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 商品数据访问 — Hash / ZSet / Set / String 底层操作
 *
 * key 设计：
 *   product:{productId}              → Hash（商品详情）
 *   product:of:{merchantId}          → ZSet（商家商品列表，score=时间戳）
 *   product:category:{category}      → ZSet（分类商品，score=销量）
 *   product:liked:{productId}        → Set（收藏用户集合）
 *   global:productId                 → String（商品 ID 自增器）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductMapper {

    private final StringRedisTemplate stringRedisTemplate;

    // TODO: Long nextProductId()
    // TODO: void saveProduct(Long productId, Map<String, String> fields)
    // TODO: Map<Object, Object> getProduct(Long productId)
    // TODO: void addToMerchantList(Long merchantId, Long productId, long timestamp)
    // TODO: List<Long> getMerchantProducts(Long merchantId, int start, int end)
    // TODO: void addToCategoryList(String category, Long productId, int score)
    // TODO: List<Long> getCategoryProducts(String category, int start, int end)
    // TODO: boolean likeProduct(Long productId, Long userId)
    // TODO: boolean unlikeProduct(Long productId, Long userId)
    // TODO: long getProductLikeCount(Long productId)
    // TODO: void incrProductField(Long productId, String field, long delta)
}
