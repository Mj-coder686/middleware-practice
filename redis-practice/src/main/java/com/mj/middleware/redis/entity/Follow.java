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
@TableName("tb_follow")
@Schema(description = "用户关注关系中间表")
public class Follow {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "关注者ID（粉丝userId）")
    private Long followerId;

    @Schema(description = "被关注者ID（博主userId）")
    private Long followedId;

    @Schema(description = "关注类型：1普通关注 2特别关注")
    private Integer followType;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "关注时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}