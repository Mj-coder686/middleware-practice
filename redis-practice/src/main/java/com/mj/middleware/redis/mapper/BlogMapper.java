package com.mj.middleware.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.middleware.redis.entity.Blog;
import jdk.jfr.MemoryAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 博客数据访问 — Hash / Set / SortedSet / List 底层操作
 *
 * key 设计：
 *   blog:{blogId}              → Hash（博客详情）
 *   blog:liked:{blogId}        → Set（点赞用户集合）
 *   blog:of:{userId}           → List（博主博客列表，按时间倒序）
 *   blog:comments:{blogId}     → SortedSet（评论，按时间排序）
 *   global:blogId              → String（全局博客 ID 自增器）
 */
@Mapper
public interface BlogMapper extends BaseMapper<Blog> {


}
