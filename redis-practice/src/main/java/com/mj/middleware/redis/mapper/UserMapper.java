package com.mj.middleware.redis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.middleware.redis.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}