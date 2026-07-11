package com.mj.middleware.redis.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.redis.entity.Feed;
import com.mj.middleware.redis.service.IFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feed 流接口 — 推拉模型练习
 */
@Tag(name = "Feed 流", description = "推拉模型 & 关注 Feed")
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final IFeedService feedService;

    @Operation(summary = "关注用户（拉模型：拉取对方博客到自己 Feed）")
    @PostMapping("/follow/{followerId}/{followeeId}")
    public Result<Void> follow(@PathVariable Long followerId, @PathVariable Long followeeId) {
        feedService.follow(followerId, followeeId);
        return Result.success();
    }

    @Operation(summary = "取消关注")
    @DeleteMapping("/follow/{followerId}/{followeeId}")
    public Result<Void> unfollow(@PathVariable Long followerId, @PathVariable Long followeeId) {
        feedService.unfollow(followerId, followeeId);
        return Result.success();
    }

    @Operation(summary = "查询关注列表")
    @GetMapping("/following/{userId}")
    public Result<List<Long>> getFollowing(@PathVariable Long userId) {
        return Result.success(feedService.getFollowing(userId));
    }

    @Operation(summary = "查询粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result<List<Long>> getFollowers(@PathVariable Long userId) {
        return Result.success(feedService.getFollowers(userId));
    }

    @Operation(summary = "查询共同关注")
    @GetMapping("/common-follow/{userId1}/{userId2}")
    public Result<List<Long>> getCommonFollowing(
            @PathVariable Long userId1, @PathVariable Long userId2) {
        return Result.success(feedService.getCommonFollowing(userId1, userId2));
    }

    @Operation(summary = "获取我的 Feed 流（推模型，分页）")
    @GetMapping("/{userId}")
    public Result<List<Feed>> getFeed(
            @PathVariable Long userId,
            @Parameter(description = "最后一条 Feed 的 ID（用于滚动分页）")
            @RequestParam(required = false) Long lastFeedId,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(feedService.getFeed(userId, lastFeedId, pageSize));
    }
}
