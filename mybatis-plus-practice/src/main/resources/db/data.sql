-- 初始测试数据
INSERT INTO t_user (username, password, nickname, email, age, status) VALUES
('zhangsan', '123456', '张三', 'zhangsan@example.com', 25, 1),
('lisi', '123456', '李四', 'lisi@example.com', 30, 1),
('wangwu', '123456', '王五', 'wangwu@example.com', 28, 1),
('zhaoliu', '123456', '赵六', 'zhaoliu@example.com', 35, 0),
('sunqi', '123456', '孙七', 'sunqi@example.com', 22, 1);

INSERT INTO t_product (name, category, brand, price, stock, description, sales, score, status) VALUES
('iPhone 15 Pro', '手机', 'Apple', 8999.00, 100, 'Apple iPhone 15 Pro 256GB', 1500, 4.8, 1),
('MacBook Pro 14', '笔记本', 'Apple', 14999.00, 50, 'Apple MacBook Pro 14英寸 M3 Pro', 800, 4.9, 1),
('Redmi Note 13', '手机', '小米', 1299.00, 500, '小米 Redmi Note 13 Pro', 3000, 4.5, 1),
('ThinkPad X1 Carbon', '笔记本', '联想', 9999.00, 30, '联想 ThinkPad X1 Carbon Gen 11', 200, 4.7, 1),
('Galaxy S24 Ultra', '手机', '三星', 9699.00, 80, '三星 Galaxy S24 Ultra 512GB', 600, 4.6, 1),
('AirPods Pro 2', '耳机', 'Apple', 1899.00, 200, 'Apple AirPods Pro 第二代', 5000, 4.7, 1),
('WH-1000XM5', '耳机', '索尼', 2499.00, 60, '索尼 WH-1000XM5 降噪耳机', 1200, 4.8, 1),
('Mate 60 Pro', '手机', '华为', 6999.00, 150, '华为 Mate 60 Pro 512GB', 2000, 4.6, 1);
