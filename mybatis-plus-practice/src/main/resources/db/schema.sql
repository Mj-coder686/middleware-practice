-- MyBatis-Plus 练习用表

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50),
    email       VARCHAR(100),
    age         INT,
    status      INT DEFAULT 1 COMMENT '0-禁用 1-正常',
    deleted     INT DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    version     INT DEFAULT 1 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    category    VARCHAR(50),
    brand       VARCHAR(50),
    price       DECIMAL(10, 2),
    stock       INT DEFAULT 0,
    description TEXT,
    sales       INT DEFAULT 0,
    score       DOUBLE DEFAULT 5.0,
    status      INT DEFAULT 1 COMMENT '0-下架 1-上架',
    deleted     INT DEFAULT 0,
    version     INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
