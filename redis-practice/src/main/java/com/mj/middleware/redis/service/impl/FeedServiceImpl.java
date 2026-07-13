package com.mj.middleware.redis.service.impl;

import com.mj.middleware.redis.entity.Feed;
import com.mj.middleware.redis.entity.Follow;
import com.mj.middleware.redis.mapper.FeedMapper;
import com.mj.middleware.redis.service.IFeedService;
import com.mj.middleware.redis.service.IFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Feed 流业务实现 — TODO: 使用 Redis Set + List / SortedSet 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private final FeedMapper feedMapper;
    private final StringRedisTemplate  redisTemplate;
    private final IFollowService followService;

    @Override

    public void follow(Long followerId, Long followeeId) {

        if (followerId == null || followeeId == null) {
            throw new RuntimeException("参数错误");
        }

        String followKey = "follow:" + followerId;
        String fansKey = "fans:" + followeeId;


        Boolean isFollow = redisTemplate
                .opsForSet()
                .isMember(followKey, followeeId.toString());

        if (Boolean.TRUE.equals(isFollow)) {
            throw new RuntimeException("已经关注");
        }


        redisTemplate.multi();

        redisTemplate.opsForSet()
                .add("follow:" + followerId, followeeId.toString());

        redisTemplate.opsForSet()
                .add("fans:" + followeeId, followerId.toString());

        redisTemplate.exec();
        followService.follow(followerId, followeeId);
    }

    @Override
    public void unfollow(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            throw new RuntimeException("参数错误");
        }

        String followKey = "follow:" + followerId;
        String fansKey = "fans:" + followeeId;


        Boolean isFollow = redisTemplate
                .opsForSet()
                .isMember(followKey, followeeId.toString());

        if (!Boolean.TRUE.equals(isFollow)) {
            throw new RuntimeException("未取消关注");
        }
        redisTemplate.execute(new SessionCallback<>() {

            @Override
            public Object execute(RedisOperations operations) {

                operations.multi();

                operations.opsForSet()
                        .remove(followKey, followeeId.toString());

                operations.opsForSet()
                        .remove(fansKey, followerId.toString());

                return operations.exec();
            }
        });
        followService.unfollow(followerId, followeeId);
    }



    @Override
    public List<Long> getFollowing(Long userId) {
        if (userId == null){
            throw new RuntimeException("参数错误");
        }
        String fansKey = "fans:" + userId;
        Boolean aBoolean = redisTemplate.hasKey(fansKey);
       List<Long> list = new ArrayList<>();
        if (!aBoolean) {
            List<Follow> flist = followService.lambdaQuery()
                    .eq(Follow::getFollowedId, userId)
                    .list();
            list = flist.stream()
                    .map(Follow::getFollowerId)
                    .toList();
            return list;
        }
        list = Objects.requireNonNull(redisTemplate.opsForSet()
                        .members(fansKey))
                .stream()
                .map(Long::parseLong)
                .toList();
        return list;
    }

    @Override
    public List<Long> getFollowers(Long userId) {
        if (userId == null){
            throw new RuntimeException("参数错误");
        }
        String followKey = "follow:" + userId;
        Boolean aBoolean = redisTemplate.hasKey(followKey);
        List<Long> list = new ArrayList<>();
        if (!aBoolean) {
            List<Follow> flist = followService.lambdaQuery()
                    .eq(Follow::getFollowerId, userId)
                    .list();
            list = flist.stream()
                    .map(Follow::getFollowedId)
                    .toList();
            return list;
        }
        list = Objects.requireNonNull(redisTemplate.opsForSet()
                        .members(followKey))
                .stream()
                .map(Long::parseLong)
                .toList();
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
