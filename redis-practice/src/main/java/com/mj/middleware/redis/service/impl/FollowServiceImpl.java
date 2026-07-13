package com.mj.middleware.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.redis.entity.Follow;
import com.mj.middleware.redis.mapper.FollowMapper;
import com.mj.middleware.redis.service.IFollowService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String KEY_FOLLOW = "follow:follows:";
    private static final String KEY_FAN = "follow:fans:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void follow(Long followerId, Long followedId) {
        // 1. 判断是否已关注
        Follow rel = baseMapper.selectFollowRel(followerId, followedId);
        if (rel != null) {
            throw new RuntimeException("已关注该博主");
        }
        // 2. 插入数据库
        Follow follow = Follow.builder()
                .followerId(followerId)
                .followedId(followedId)
                .followType(1)
                .build();
        this.save(follow);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void unfollow(Long followerId, Long followedId) {
        // 1. 删除数据库记录
        this.lambdaUpdate()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFollowedId, followedId)
                .remove();

        // 2. 删除Redis缓存
        String followKey = KEY_FOLLOW + followerId;
        String fanKey = KEY_FAN + followedId;
        redisTemplate.opsForSet().remove(followKey, followedId);
        redisTemplate.opsForSet().remove(fanKey, followerId);
    }

    @Override
    public boolean isFollow(Long followerId, Long followedId) {
        String key = KEY_FOLLOW + followerId;
        // 先查缓存
        Boolean exist = redisTemplate.opsForSet().isMember(key, followedId);
        if (Boolean.TRUE.equals(exist)) {
            return true;
        }
        // 缓存不存在查库（兜底）
        Follow rel = baseMapper.selectFollowRel(followerId, followedId);
        return rel != null;
    }

    @Override
    public List<Long> getFollowedUserIdList(Long followerId) {
        return baseMapper.selectFollowedIdsByFollower(followerId);
    }

    @Override
    public List<Long> getFanUserIdList(Long followedId) {
        return baseMapper.selectFollowerIdsByFollowed(followedId);
    }
}