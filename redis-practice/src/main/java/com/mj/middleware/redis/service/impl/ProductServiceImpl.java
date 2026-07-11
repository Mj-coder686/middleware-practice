package com.mj.middleware.redis.service.impl;

import com.mj.middleware.redis.entity.Product;
import com.mj.middleware.redis.mapper.ProductMapper;
import com.mj.middleware.redis.service.IFeedService;
import com.mj.middleware.redis.service.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品业务实现 — TODO: 使用 Redis Hash / ZSet / Set 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductMapper productMapper;
    private final IFeedService feedService;

    @Override
    public Long publish(Product product) {
        // TODO: 1. INCR global:productId → productId
        // TODO: 2. HSET product:{productId} 存储商品详情
        // TODO: 3. ZADD product:of:{merchantId} timestamp productId
        // TODO: 4. ZADD product:category:{category} sales productId
        // TODO: 5. 推送到关注者的 Feed 流：feedService.pushToFollowers(merchantId, productId)
        return null;
    }

    @Override
    public Product getById(Long id) {
        // TODO: HGETALL product:{id} → 映射为 Product
        return null;
    }

    @Override
    public void putOnSale(Long productId) {
        // TODO: HSET product:{id} status 1
    }

    @Override
    public void pullOffSale(Long productId) {
        // TODO: HSET product:{id} status 0
    }

    @Override
    public boolean like(Long productId, Long userId) {
        // TODO: SADD product:liked:{productId} userId
        // TODO: HINCRBY product:{id} likedCount 1
        return false;
    }

    @Override
    public boolean unlike(Long productId, Long userId) {
        // TODO: SREM product:liked:{productId} userId
        // TODO: HINCRBY product:{id} likedCount -1
        return false;
    }

    @Override
    public List<Product> getByMerchant(Long merchantId, int pageNum, int pageSize) {
        // TODO: ZREVRANGE product:of:{merchantId} start end → 逐个 HGETALL
        return null;
    }

    @Override
    public List<Product> getByCategory(String category, int pageNum, int pageSize) {
        // TODO: ZREVRANGE product:category:{category} start end → 逐个 HGETALL
        return null;
    }
}
