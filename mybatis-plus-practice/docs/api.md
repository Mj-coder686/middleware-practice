# MyBatis-Plus 练习模块 — API 文档

> 启动端口：8081
> Swagger UI：http://localhost:8081/doc.html
> H2 Console：http://localhost:8081/h2-console

---

## 用户管理 `/user`

### 1. 新增用户

```
POST /user
Content-Type: application/json

{
    "username": "test_user",
    "password": "123456",
    "nickname": "测试用户",
    "email": "test@example.com",
    "age": 25,
    "status": 1
}

Response: { "code": 200, "msg": "success", "data": true }
```

### 2. 根据 ID 查询

```
GET /user/{id}

Response: { "code": 200, "msg": "success", "data": { "id": 1, "username": "zhangsan", ... } }
```

### 3. 查询全部用户

```
GET /user/list

Response: { "code": 200, "msg": "success", "data": [...] }
```

### 4. 更新用户

```
PUT /user
Content-Type: application/json

{
    "id": 1,
    "nickname": "新昵称",
    "age": 26
}

Response: { "code": 200, "msg": "success", "data": true }
```

### 5. 删除用户（逻辑删除）

```
DELETE /user/{id}

Response: { "code": 200, "msg": "success", "data": true }
```

### 6. 批量删除

```
DELETE /user/batch
Content-Type: application/json

[1, 2, 3]

Response: { "code": 200, "msg": "success", "data": true }
```

### 7. 批量插入演示

```
POST /user/batch-insert

Response: { "code": 200, "msg": "success", "data": true }
```

### 8. 根据用户名查询

```
GET /user/by-username?username=zhangsan

Response: { "code": 200, "msg": "success", "data": { "id": 1, "username": "zhangsan", ... } }
```

### 9. 分页查询（带条件）

```
GET /user/page?keyword=张&status=1&pageNum=1&pageSize=10

Response: {
    "code": 200,
    "msg": "success",
    "data": {
        "records": [...],
        "total": 1,
        "size": 10,
        "current": 1,
        "pages": 1
    }
}
```

---

## 商品管理 `/product`

### 1. 新增商品

```
POST /product
Content-Type: application/json

{
    "name": "iPad Pro",
    "category": "平板",
    "brand": "Apple",
    "price": 7999.00,
    "stock": 50,
    "description": "Apple iPad Pro 13英寸 M4",
    "sales": 0,
    "score": 5.0,
    "status": 1
}
```

### 2. 根据 ID 查询

```
GET /product/{id}
```

### 3. 查询全部

```
GET /product/list
```

### 4. 按分类和价格范围查询（LambdaQueryWrapper）

```
GET /product/by-condition?category=手机&minPrice=5000&maxPrice=10000
```

### 5. 复杂查询（多条件组合）

```
GET /product/complex?keyword=iPhone&brand=Apple&minPrice=5000&minStock=10
```

### 6. 按品牌统计商品数量（聚合查询）

```
GET /product/count-by-brand

Response: {
    "code": 200,
    "msg": "success",
    "data": [
        { "brand": "Apple", "count": 3 },
        { "brand": "小米", "count": 1 },
        { "brand": "联想", "count": 1 }
    ]
}
```

### 7. 乐观锁更新价格

```
PUT /product/optimistic-lock?id=1&newPrice=8499.00

# 多次调用观察 version 变化
# 第一次: version 1→2, 更新成功
# 第二次: version 2→3, 更新成功
```

### 8. 批量更新价格（按分类涨价）

```
PUT /product/batch-price?category=手机&increaseAmount=100

# 所有手机类商品涨价 100 元
```
