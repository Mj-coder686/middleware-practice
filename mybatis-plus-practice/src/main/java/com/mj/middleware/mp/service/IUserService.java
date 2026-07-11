package com.mj.middleware.mp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.mp.entity.UserMP;

import java.util.List;

/**
 * UserService 接口 — 继承 IService 拥有批量 CRUD
 *
 * IService 常用方法：
 *   save / saveBatch / saveOrUpdate
 *   removeById / removeBatchByIds
 *   updateById / updateBatchById
 *   getById / listByIds
 *   page / page(Wrapper)
 */
public interface IUserService extends IService<UserMP> {

    /** 根据 ID 查询 */
    UserMP get(Long id);

    /** 新增用户 */
    void add(UserMP user);

    /** 更新用户（只更新非 null 字段） */
    void updateUser(Long id, UserMP user);

    /** 删除用户（逻辑删除） */
    void deleteUser(Long id);

    /** 分页查询 */
    IPage<UserMP> listUser(Integer pageNum, Integer pageSize);

    /** 批量插入 */
    void addList(List<UserMP> userList);
}
