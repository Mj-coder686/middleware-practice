package com.mj.middleware.redis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user")
@Schema(description = "用户信息（博主/普通用户）")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "用户主键ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像地址")
    private String avatar;

    @Schema(description = "账号用户名")
    private String username;

    @Schema(description = "加密密码")
    private String password;

    @Schema(description = "个人简介")
    private String intro;

    @Schema(description = "粉丝总数（冗余，方便首页展示）")
    private Long fanCount;

    @Schema(description = "关注总数（冗余）")
    private Long followCount;

    @Schema(description = "发布博客总数（冗余）")
    private Long blogCount;

    @Schema(description = "账号状态：0禁用 1正常")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // 非数据库字段：页面交互临时字段
    @TableField(exist = false)
    @Schema(description = "当前登录用户是否已关注该用户")
    private Boolean isFollow;
}