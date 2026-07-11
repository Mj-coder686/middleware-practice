package com.mj.middleware.redis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Feed 流条目 — 练习推拉模型
 *
 * 推模型（写扩散）：大V发博客时，写入所有粉丝的 Feed List
 *   key = feed:{粉丝userId}   → List（保留最近 N 条）
 *
 * 拉模型（读扩散）：用户刷 Feed 时，从关注列表拉取博客 ID
 *   key = blog:of:{博主userId}  → List
 *
 * 推拉结合：大V用拉，普通用户用推
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Feed 流条目")
public class Feed implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Feed ID（自生成唯一标识）")
    private Long feedId;

    @Schema(description = "博客 ID")
    private Long blogId;

    @Schema(description = "作者用户 ID")
    private Long authorId;

    @Schema(description = "作者昵称")
    private String authorNickname;

    @Schema(description = "博客标题（冗余，减少回查）")
    private String blogTitle;

    @Schema(description = "博客内容摘要")
    private String blogSummary;

    @Schema(description = "Feed 类型：PUSH-推模型 / PULL-拉模型")
    private String feedType;

    @Schema(description = "推送到 Feed 流的时间")
    private LocalDateTime pushTime;
}
