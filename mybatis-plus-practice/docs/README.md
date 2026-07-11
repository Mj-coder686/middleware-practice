# MyBatis-Plus 学习指南

## 一、概述

**MyBatis-Plus (MP)** 是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

**核心特性：**
- 内置通用 Mapper、Service，无需编写基础 CRUD SQL
- 强大的条件构造器（QueryWrapper / LambdaQueryWrapper）
- 内置分页插件、乐观锁插件、逻辑删除、自动填充
- 代码生成器（AutoGenerator）

## 二、环境搭建

### 依赖配置

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
```

### application.yml 核心配置

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # SQL 日志
    map-underscore-to-camel-case: true                      # 下划线转驼峰
  global-config:
    db-config:
      id-type: auto           # 主键策略：AUTO(自增) / ASSIGN_ID(雪花) / NONE
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath:mapper/*.xml
```

## 三、核心知识点

### 🟢 必须掌握

#### 1. 实体注解

| 注解 | 作用 | 示例代码 |
|------|------|----------|
| `@TableName` | 映射表名 | `@TableName("t_user")` |
| `@TableId` | 主键配置 | `@TableId(type = IdType.AUTO)` |
| `@TableField` | 字段映射 | `@TableField(fill = FieldFill.INSERT)` |
| `@TableLogic` | 逻辑删除 | `@TableLogic` |
| `@Version` | 乐观锁 | `@Version` |

**对应代码：** `entity/UserMP.java`, `entity/ProductMP.java`

#### 2. BaseMapper CRUD 方法

| 方法 | 说明 |
|------|------|
| `insert(T entity)` | 插入一条记录 |
| `deleteById(id)` | 根据 ID 删除 |
| `deleteBatchIds(ids)` | 批量删除 |
| `updateById(entity)` | 根据 ID 更新（null 字段不更新） |
| `selectById(id)` | 根据 ID 查询 |
| `selectList(wrapper)` | 条件查询列表 |
| `selectPage(page, wrapper)` | 分页查询 |
| `selectCount(wrapper)` | 条件计数 |

**对应代码：** `mapper/UserMapper.java`, `mapper/ProductMapper.java`

#### 3. IService 常用方法

| 方法 | 说明 |
|------|------|
| `save(entity)` | 新增 |
| `saveBatch(list)` | 批量新增 |
| `saveOrUpdate(entity)` | 新增或更新（ID 有值则更新） |
| `removeById(id)` | 删除 |
| `removeBatchByIds(ids)` | 批量删除 |
| `updateById(entity)` | 更新 |
| `getById(id)` | 查询单个 |
| `list()` | 查询全部 |
| `listByIds(ids)` | 批量查询 |
| `page(page, wrapper)` | 分页查询 |
| `count(wrapper)` | 条件计数 |
| `getOne(wrapper)` | 查询一条（多条会报错） |

**对应代码：** `service/IUserService.java`, `service/impl/UserServiceImpl.java`

#### 4. 条件构造器

**QueryWrapper** — 字符串形式：
```java
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.like("nickname", "张")
       .eq("status", 1)
       .between("age", 20, 30)
       .orderByDesc("create_time");
```

**LambdaQueryWrapper** — 类型安全（推荐）：
```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.like(User::getNickname, "张")
       .eq(User::getStatus, 1)
       .between(User::getAge, 20, 30)
       .orderByDesc(User::getCreateTime);
```

**UpdateWrapper** — 条件更新：
```java
LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(Product::getCategory, "手机")
       .setSql("price = price + 100");
```

**对应代码：** `service/impl/ProductServiceImpl.java`

#### 5. 分页查询

```java
// 1. 配置分页插件（见 config/MyBatisPlusConfig.java）
// 2. 使用
Page<User> page = new Page<>(1, 10); // 第1页，每页10条
IPage<User> result = userMapper.selectPage(page, wrapper);
// result.getRecords() — 当前页数据
// result.getTotal()    — 总记录数
// result.getPages()    — 总页数
```

**对应代码：** `service/impl/UserServiceImpl.java#pageQuery`

---

### 🟡 进阶掌握

#### 6. 逻辑删除

配置 `@TableLogic` 注解后：
- `deleteById()` → 实际执行 `UPDATE SET deleted=1 WHERE id=?`
- `selectList()` → 自动追加 `WHERE deleted=0`

```java
@TableLogic
private Integer deleted;
```

#### 7. 乐观锁

```java
// 1. 注册 OptimisticLockerInnerInterceptor
// 2. 实体加 @Version 字段
// 3. 更新时先查后改
Product product = productService.getById(1);  // version=1
product.setPrice(99.0);
productService.updateById(product);
// 执行: UPDATE SET price=99.0, version=2 WHERE id=1 AND version=1
// 如果 version 已被其他线程改为 2，更新影响行数为 0
```

**对应代码：** `service/impl/ProductServiceImpl.java#updateWithOptimisticLock`

#### 8. 自动填充

```java
// 1. 实体标注 @TableField(fill = FieldFill.INSERT)
// 2. 实现 MetaObjectHandler
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
}
```

**对应代码：** `config/MyBatisPlusConfig.java#AutoFillHandler`

#### 9. 自定义 SQL（XML）

```java
// Mapper 接口
List<UserMP> selectByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
```

```xml
<!-- mapper/UserMapper.xml -->
<select id="selectByAgeRange" resultType="UserMP">
    SELECT * FROM t_user WHERE age BETWEEN #{minAge} AND #{maxAge} AND deleted = 0
</select>
```

---

### 🔴 高级/面试常问

#### 10. 条件构造器执行流程

```
Controller → Service(lambdaQuery().eq(...).list())
                 ↓
           ServiceImpl → getBaseMapper().selectList(wrapper)
                 ↓
           Mapper → MyBatis 执行 → SQL 拼接 → 数据库
```

#### 11. MP 与 MyBatis 的关系

| 维度 | MyBatis | MyBatis-Plus |
|------|---------|--------------|
| CRUD | 手写 XML/注解 | 内置通用 CRUD |
| 分页 | 需要 PageHelper | 内置分页插件 |
| 条件查询 | 手写 `<where>` | Wrapper 链式构造 |
| 代码生成 | 无 | AutoGenerator |

#### 12. 常见面试题

**Q: updateById 对 null 字段怎么处理？**
A: 默认策略是 `NOT_NULL`，null 字段不更新。可通过 `@TableField(updateStrategy = FieldStrategy.ALWAYS)` 改为始终更新。

**Q: 乐观锁失效场景？**
A: 当使用 `update(entity, wrapper)` 且 wrapper 中没有包含 version 条件时，乐观锁不生效。必须用 `updateById(entity)` 或在 wrapper 中显式指定 version。

**Q: 逻辑删除后唯一索引冲突？**
A: 逻辑删除只是标记，记录还在表中。如果 username 有唯一索引，删除后再新增同名用户会冲突。解决方案：唯一索引加 deleted 字段组合，或使用 `delete_id` 前缀。

**Q: 分页 count 查询优化？**
A: 当不需要总记录数时，`page.setSearchCount(false)` 可跳过 count 查询，提升性能。

## 四、最佳实践 & 踩坑记录

1. **优先使用 LambdaQueryWrapper** — 类型安全，重构时不会遗漏
2. **批量操作用 saveBatch** — 比循环 save 快很多（内部按 1000 条一批拼接 SQL）
3. **谨慎使用 selectOne** — 查不到返回 null，查到多条抛异常，建议用 `one()` 或限制条数
4. **排序字段不要用 `orderByDesc(createTime)`** — 字符串形式容易写错字段名，用 Lambda 写法
5. **H2 数据库注意兼容性** — 部分 MySQL 函数在 H2 中不支持，开发时注意

## 五、参考链接

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)
- [条件构造器文档](https://baomidou.com/guides/wrapper/)
