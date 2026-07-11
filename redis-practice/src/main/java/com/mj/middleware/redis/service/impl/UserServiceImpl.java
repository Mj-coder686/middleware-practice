package com.mj.middleware.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.redis.entity.User;
import com.mj.middleware.redis.mapper.UserMapper;
import com.mj.middleware.redis.service.IUserService;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User getUserById(Long userId) {
        return this.getById(userId);
    }
}