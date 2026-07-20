package com.mj.middleware.nacos.provider.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.nacos.provider.entity.UserEntity;
import com.mj.middleware.nacos.provider.mapper.UserMapper;
import com.mj.middleware.nacos.provider.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements IUserService {
}
