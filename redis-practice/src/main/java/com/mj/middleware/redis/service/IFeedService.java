package com.mj.middleware.redis.service;

import com.mj.middleware.redis.entity.Feed;

import java.util.List;

/**
 * Feed 流业务接口
 */
public interface IFeedService {

    /** 关注 */
    void follow(Long followerId, Long followeeId);

    /** 取消关注 */
    void unfollow(Long followerId, Long followeeId);

    /** 查询关注列表 */
    List<Long> getFollowing(Long userId);

    /** 查询粉丝列表 */
    List<Long> getFollowers(Long userId);

    /** 查询共同关注 */
    List<Long> getCommonFollowing(Long userId1, Long userId2);

    /** 获取 Feed 流（滚动分页） */
    List<Feed> getFeed(Long userId, Long lastFeedId, int pageSize);

    /** 推送博客到粉丝 Feed（大V发博客时调用） */
    void pushToFollowers(Long authorId, Long blogId);
}
