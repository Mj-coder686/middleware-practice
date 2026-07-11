package com.mj.middleware.redis.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Feed 流数据访问 — Set（关注/粉丝）+ List（Feed 流）底层操作
 *
 * key 设计：
 *   follow:{userId}         → Set（关注列表）
 *   fans:{userId}           → Set（粉丝列表）
 *   feed:{userId}           → List（推模型 Feed 流，LPUSH + LTRIM）
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FeedMapper {

    private final StringRedisTemplate stringRedisTemplate;

    // TODO: void addFollow(Long followerId, Long followeeId)
    // TODO: void removeFollow(Long followerId, Long followeeId)
    // TODO: Set<Long> getFollowing(Long userId)
    // TODO: Set<Long> getFollowers(Long userId)
    // TODO: Set<Long> getCommonFollowing(Long userId1, Long userId2)
    // TODO: void pushFeed(Long userId, String feedData)
    // TODO: List<String> getFeed(Long userId, int start, int end)
}
