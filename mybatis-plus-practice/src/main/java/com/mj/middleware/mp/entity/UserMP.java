package com.mj.middleware.mp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户实体 — MyBatis-Plus 注解演示
 *
 * 注解说明：
 * @TableName    — 映射数据库表名
 * @TableId      — 主键配置（type=AUTO 数据库自增）
 * @TableField   — 字段映射（exist=false 表示非数据库字段）
 * @TableLogic   — 逻辑删除标记
 * @Version      — 乐观锁
 * @TableField(fill=INSERT) — 自动填充（创建时间）
 */
@Data
@Accessors(chain = true)
@TableName("t_user")
public class UserMP {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private Integer age;

    private Integer status;

    /** 逻辑删除字段：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
