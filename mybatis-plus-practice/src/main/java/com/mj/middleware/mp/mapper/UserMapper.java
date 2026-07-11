package com.mj.middleware.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.middleware.mp.entity.UserMP;

/**
 * UserMapper — 继承 BaseMapper 即拥有全部 CRUD 方法
 *
 * 常用方法：
 *   int insert(T entity);
 *   int deleteById(Serializable id);
 *   int updateById(T entity);
 *   T selectById(Serializable id);
 *   List<T> selectBatchIds(Collection<?> ids);
 *   List<T> selectByMap(Map<String, Object> columnMap);
 *   List<T> selectList(Wrapper<T> queryWrapper);
 *   <E extends IPage<T>> E selectPage(E page, Wrapper<T> queryWrapper);
 *   Long selectCount(Wrapper<T> queryWrapper);
 */
public interface UserMapper extends BaseMapper<UserMP> {
    // BaseMapper 提供了完整的 CRUD，无需额外定义
    // 如需自定义 SQL，可在此声明方法并在 XML 中实现
}
