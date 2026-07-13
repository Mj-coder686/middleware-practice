package com.mj.middleware.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.redis.entity.Blog;

import java.util.List;
import java.util.Map;

/**
 * 博客业务接口
 */
public interface IBlogService extends IService<Blog> {

    /** 发布博客（含 Feed 推送） */
    Long publish(Blog blog);

    /** 根据 ID 查询 */
    Blog getById(Long id);

    /** 点赞 */
    boolean like(Long blogId, Long userId);

    /** 取消点赞 */
    boolean unlike(Long blogId, Long userId);

    /** 查询点赞数 */
    long getLikeCount(Long blogId);

    /** 查询点赞用户列表（Top-N） */
    List<Long> getLikedUsers(Long blogId, int count);

    /** 查询博主的博客列表 */
    List<Blog> getBlogsByUser(Long userId, int pageNum, int pageSize);

    Map<String, Object> getFeed(Long max, Integer offset);
}
