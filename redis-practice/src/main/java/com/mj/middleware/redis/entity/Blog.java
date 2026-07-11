package com.mj.middleware.redis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 博客 — 练习大V发博客 & 点赞 / Feed 推送
 *
 * 存储设计：
 *   hash  → blog:{blogId}            博客详情（Hash 结构）
 *   set   → blog:liked:{blogId}      点赞用户集合
 *   zset  → blog:comments:{blogId}   评论（按时间排序）
 *   list  → feed:{userId}            推送到粉丝的 Feed 流
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "博客")
public class Blog {

    @Schema(description = "博客 ID（Redis 自生成）")
    private Long id;

    @Schema(description = "作者用户 ID")
    private Long userId;

    @Schema(description = "作者昵称（大V）")
    private String nickname;

    @Schema(description = "博客标题")
    private String title;

    @Schema(description = "博客内容")
    private String content;

    @Schema(description = "图片地址（多张逗号分隔）")
    private String images;

    @Schema(description = "点赞数")
    private Long likedCount;

    @Schema(description = "当前登录用户是否已点赞")
    private Boolean isLiked;

    @Schema(description = "博客发布时间戳")
    private Long publishTime;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "发布时间")
    private LocalDateTime createTime;


    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
