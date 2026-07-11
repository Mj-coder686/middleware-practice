package com.mj.middleware.mp.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.mp.entity.UserMP;
import com.mj.middleware.mp.mapper.UserMapper;
import com.mj.middleware.mp.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserMP> implements IUserService {

    @Override
    public UserMP get(Long id) {
        // IService 已内置 getById，直接用
        return getById(id);
    }

    @Override
    public void add(UserMP user) {
        boolean result = save(user);
        log.info("新增用户: {}, 结果: {}", user.getUsername(), result);
    }

    @Override
    public void updateUser(Long id, UserMP user) {
        // 先查出来，保证是已有记录
        UserMP exist = getById(id);
        if (exist == null) {
            throw new RuntimeException("用户不存在: id=" + id);
        }
        // 只更新非 null 字段，避免覆盖已有数据
        if (user.getUsername() != null) exist.setUsername(user.getUsername());
        if (user.getPassword() != null) exist.setPassword(user.getPassword());
        if (user.getNickname() != null) exist.setNickname(user.getNickname());
        if (user.getEmail() != null) exist.setEmail(user.getEmail());
        if (user.getAge() != null) exist.setAge(user.getAge());
        if (user.getStatus() != null) exist.setStatus(user.getStatus());
        updateById(exist);
    }

    @Override
    public void deleteUser(Long id) {
        // 逻辑删除（实体类已配置 @TableLogic，deleteById 实际执行 UPDATE SET deleted=1）
        removeById(id);
    }

    @Override
    public IPage<UserMP> listUser(Integer pageNum, Integer pageSize) {
        Page<UserMP> page = new Page<>(pageNum, pageSize);
        // 无条件分页查询
        return lambdaQuery().page(page);
    }

    @Override
    public void addList(List<UserMP> userList) {
        if (userList == null || userList.isEmpty()) {
            return;
        }
        // saveBatch 默认每 1000 条一批，失败会抛异常
        saveBatch(userList);
        log.info("批量插入 {} 条用户", userList.size());
    }
}
