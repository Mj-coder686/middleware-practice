package com.mj.middleware.redis.controller;

import com.mj.middleware.common.result.Result;
import com.mj.middleware.redis.entity.Blog;
import com.mj.middleware.redis.service.IBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 博客接口 — 大V发博客 / 点赞 / Feed 推送
 */
@Tag(name = "博客管理", description = "大V博客 & 点赞 & Feed推送")
@RestController
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogController {

    private final IBlogService blogService;

    @Operation(summary = "发布博客（自动推送到粉丝 Feed）")
    @PostMapping
    public Result<Long> publishBlog(@RequestBody Blog blog) {
        return Result.success(blogService.publish(blog));
    }

    @Operation(summary = "根据 ID 查询博客")
    @GetMapping("/{id}")
    public Result<Blog> getBlog(@PathVariable Long id) {
        return Result.success(blogService.getById(id));
    }

    @Operation(summary = "点赞博客")
    @PostMapping("/{id}/like/{userId}")
    public Result<Boolean> likeBlog(@PathVariable Long id, @PathVariable Long userId) {
        return Result.success(blogService.like(id, userId));
    }

    @Operation(summary = "取消点赞")
    @DeleteMapping("/{id}/like/{userId}")
    public Result<Boolean> unlikeBlog(@PathVariable Long id, @PathVariable Long userId) {
        return Result.success(blogService.unlike(id, userId));
    }

    @Operation(summary = "查询博客点赞数")
    @GetMapping("/{id}/like-count")
    public Result<Long> getLikeCount(@PathVariable Long id) {
        return Result.success(blogService.getLikeCount(id));
    }

    @Operation(summary = "查询博客点赞的用户列表（Top-N）")
    @GetMapping("/{id}/liked-users")
    public Result<List<Long>> getLikedUsers(
            @PathVariable Long id,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "5") int count) {
        return Result.success(blogService.getLikedUsers(id, count));
    }

    @Operation(summary = "查询博主的博客列表（按时间倒序）")
    @GetMapping("/user/{userId}")
    public Result<List<Blog>> getBlogsByUser(
            @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(blogService.getBlogsByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "查询推送过来的博客")
    @GetMapping("/feed")
    public Map<String, Object> getFeed(
            @RequestParam("lastId") Long max,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset
    )
        {
        return blogService.getFeed(max, offset);
    }
}
