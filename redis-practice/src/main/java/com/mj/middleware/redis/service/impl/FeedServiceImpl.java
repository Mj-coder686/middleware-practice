package com.mj.middleware.redis.service.impl;

import com.mj.middleware.redis.entity.Feed;
import com.mj.middleware.redis.mapper.FeedMapper;
import com.mj.middleware.redis.service.IFeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Feed 流业务实现 — TODO: 使用 Redis Set + List / SortedSet 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private final FeedMapper feedMapper;

    @Override
    public void follow(Long followerId, Long followeeId) {
        // TODO: SADD follow:{followerId} followeeId
        // TODO: SADD fans:{followeeId} followerId
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        // TODO: SREM follow:{followerId} followeeId
        // TODO: SREM fans:{followeeId} followerId
    }

    @Override
    public List<Long> getFollowing(Long userId) {
        // TODO: SMEMBERS follow:{userId}
        return null;
    }

    @Override
    public List<Long> getFollowers(Long userId) {
        // TODO: SMEMBERS fans:{userId}
        return null;
    }

    @Override
    public List<Long> getCommonFollowing(Long userId1, Long userId2) {
        // TODO: SINTER follow:{userId1} follow:{userId2}
        return null;
    }

    @Override
    public List<Feed> getFeed(Long userId, Long lastFeedId, int pageSize) {
        // TODO: 推模型 → LRANGE feed:{userId} start end
        // TODO: 拉模型 → 拉取关注人博客 + ZUNIONSTORE 合并排序
        return null;
    }

    @Override
    public void pushToFollowers(Long authorId, Long blogId) {
        // TODO: SMEMBERS fans:{authorId} → LPUSH feed:{粉丝id}
        // TODO: 大V（粉丝>阈值）不推，走拉模型
    }
}
