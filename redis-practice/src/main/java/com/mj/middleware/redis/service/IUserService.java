package com.mj.middleware.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.redis.entity.User;

public interface IUserService extends IService<User> {

    /**
     * 根据id获取用户信息
     */
    User getUserById(Long userId);
}