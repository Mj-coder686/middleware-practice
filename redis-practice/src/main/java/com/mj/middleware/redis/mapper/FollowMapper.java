package com.mj.middleware.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.middleware.redis.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 查询当前用户的关注列表（被关注人id集合）
     */
    List<Long> selectFollowedIdsByFollower(@Param("followerId") Long followerId);

    /**
     * 查询博主的所有粉丝id集合
     */
    List<Long> selectFollowerIdsByFollowed(@Param("followedId") Long followedId);

    /**
     * 判断是否已关注
     */
    Follow selectFollowRel(@Param("followerId") Long followerId, @Param("followedId") Long followedId);
}