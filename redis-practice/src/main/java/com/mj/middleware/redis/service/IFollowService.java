package com.mj.middleware.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.redis.entity.Follow;
import java.util.List;

public interface IFollowService extends IService<Follow> {

    /**
     * 关注博主
     * @param followerId 粉丝id
     * @param followedId 博主id
     */
    void follow(Long followerId, Long followedId);

    /**
     * 取消关注
     */
    void unfollow(Long followerId, Long followedId);

    /**
     * 判断是否关注
     */
    boolean isFollow(Long followerId, Long followedId);

    /**
     * 获取当前用户所有关注的博主id
     */
    List<Long> getFollowedUserIdList(Long followerId);

    /**
     * 获取博主所有粉丝id
     */
    List<Long> getFanUserIdList(Long followedId);
}