package com.mj.middleware.redis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.redis.entity.Blog;
import com.mj.middleware.redis.entity.Follow;
import com.mj.middleware.redis.entity.User;
import com.mj.middleware.redis.mapper.BlogMapper;
import com.mj.middleware.redis.service.IBlogService;
import com.mj.middleware.redis.service.IFeedService;
import com.mj.middleware.redis.until.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 博客业务实现 — TODO: 使用 Redis Hash / Set / SortedSet 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    private final IFeedService feedService;
    private final FollowServiceImpl followService;
    private final UserServiceImpl userService;
    private final StringRedisTemplate stringRedisTemplate;
    // 博客ID自增计数器
    private static final String COUNTER_BLOG = "counter:blog";
    // Feed收件箱 zset key前缀 feed:userId
    private static final String FEED_PREFIX = "feed:";
    // 博客轻量缓存 blog:lite:blogId
    private static final String BLOG_LITE_PREFIX = "blog:lite:";
    // 博客完整详情缓存 blog:full:blogId
    private static final String BLOG_FULL_PREFIX = "blog:full:";
//    点赞
    private static final String BLOG_LIKE_PREFIX = "blog:like:";
//
    private static final String BLOG_AUTHOR_PREFIX = "blog:author:";


    // Feed最大保留条数
    private static final int MAX_FEED_SIZE = 500;


    @Transactional(rollbackFor = Exception.class)
    public Long publish(Blog blog) {
        // 1. 参数校验
        if (blog == null) {
            throw new RuntimeException("参数错误");
        }
        // 获取当前登录博主ID
        Long authorId = UserContext.getCurrentUser();
        User user = userService.getUserById(authorId);
        // 2. Redis自增生成全局博客ID
        Long blogId = stringRedisTemplate.opsForValue().increment(COUNTER_BLOG);
        if (blogId == null) {
            throw new RuntimeException("博客ID生成失败");
        }
        // 填充博客基础信息
        blog.setId(blogId);
        blog.setUserId(authorId);
        blog.setNickname(user.getNickname());
        long publishTs = System.currentTimeMillis();
        blog.setCreateTime(LocalDateTime.now());

        // 3. 数据库持久化（DB为权威数据源，先入库）
        boolean saveSuccess = save(blog);
        if (!saveSuccess) {
            throw new RuntimeException("博客数据库保存失败");
        }
        // ========== 新增：写入两层Redis博客缓存 ==========
        String liteKey = BLOG_LITE_PREFIX + blogId;
        String fullKey = BLOG_FULL_PREFIX + blogId;
        // 轻量缓存（Feed列表用）
        Map<String, Object> liteMap = new HashMap<>();
        liteMap.put("blogId", blogId);
        liteMap.put("userId", authorId);
        liteMap.put("nickname", blog.getNickname());
        liteMap.put("title", blog.getTitle());
        liteMap.put("coverImg", blog.getImages() == null ? "" : blog.getImages().split(",")[0]);
        liteMap.put("likedCount", 0L);
        liteMap.put("publishTs", publishTs);
        stringRedisTemplate.opsForHash().putAll(liteKey, liteMap);
        // 完整详情缓存（点击进入详情页使用）
        // Blog完整对象直接转Map
        Map<String, Object> fullMap = BeanUtil.beanToMap(blog, false, true);
        stringRedisTemplate.opsForHash().putAll(fullKey, fullMap);
        // 设置7天过期兜底一致性
        stringRedisTemplate.expire(liteKey, 7, TimeUnit.DAYS);
        stringRedisTemplate.expire(fullKey, 7, TimeUnit.DAYS);
        // 4. Stream获取所有粉丝ID列表
        List<Long> fanIdList = followService.lambdaQuery()
                .eq(Follow::getFollowedId, authorId)
                .list()
                .stream()
                .map(Follow::getFollowerId)
                .toList();
        // 无粉丝直接返回博客ID，不要return null
        if (CollectionUtils.isEmpty(fanIdList)) {
            return blogId;
        }
        // 5. Redis Pipeline批量推送Feed ZSet，stream遍历粉丝
        stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (Long fanId : fanIdList){
                    if (blogId != null) {
                        connection.zAdd(FEED_PREFIX.getBytes(StandardCharsets.UTF_8), publishTs, blogId.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                return null;
            }
        });
        try {
            String authorkey = BLOG_AUTHOR_PREFIX + authorId;
            stringRedisTemplate.opsForZSet().add(authorkey, blogId.toString(), publishTs);
        } catch (Exception e) {
            throw new RuntimeException("保存失败");
        }
        return blogId;
    }

    @Override
    public Blog getById(Long id) {
        if (id == null){
            return null;
        }
        String fullKey = BLOG_FULL_PREFIX + id;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(fullKey);
        if (CollectionUtils.isEmpty(entries)) {
            return null;
        }
        return BeanUtil.fillBeanWithMap(entries, new Blog(), false);
    }

    @Override
    public boolean like(Long blogId, Long userId) {
        if (blogId == null || userId == null){
            return false;
        }
        String fullKey = BLOG_FULL_PREFIX + blogId;
        String likekey = BLOG_LIKE_PREFIX + blogId;
        String liteKey = BLOG_LITE_PREFIX + blogId;
        checkBlogCacheExist(blogId, liteKey, fullKey);
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likekey, userId);
        if (Boolean.TRUE.equals(isLiked)) {
            throw new RuntimeException("已点赞");
        }
       try{
           stringRedisTemplate.executePipelined(new RedisCallback<Object>() {
               @Override
               public Object doInRedis(RedisConnection connection) throws DataAccessException {
                   connection.hashCommands().hIncrBy(liteKey.getBytes(StandardCharsets.UTF_8), "likedCount".getBytes(StandardCharsets.UTF_8), 1);
                   connection.hashCommands().hIncrBy(fullKey.getBytes(StandardCharsets.UTF_8), "likedCount".getBytes(StandardCharsets.UTF_8), 1);
                   connection.zSetCommands().zAdd(likekey.getBytes(StandardCharsets.UTF_8),System.currentTimeMillis(), userId.toString().getBytes(StandardCharsets.UTF_8));
                   return null;
               }
           });
       }catch (Exception e){
           log.error("点赞失败", e);
           return false;
       }
        return true;
    }

    @Override
    public boolean unlike(Long blogId, Long userId) {
        // 1. 参数校验
        if (blogId == null || userId == null) {
            return false;
        }

        String liteKey = BLOG_LITE_PREFIX + blogId;
        String fullKey = BLOG_FULL_PREFIX + blogId;
        String likeSetKey = BLOG_LIKE_PREFIX + blogId;

        // 你保留的redis key存在校验（练习使用，生产建议替换为查库）
        checkBlogCacheExist(blogId, liteKey, fullKey);

        // 判断是否点赞
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(likeSetKey, userId);
        if (!Boolean.TRUE.equals(isLiked)) {
            throw new RuntimeException("未点赞，无法取消");
        }

        // 提前统一转字节，减少重复代码
        byte[] liteBytes = liteKey.getBytes(StandardCharsets.UTF_8);
        byte[] fullBytes = fullKey.getBytes(StandardCharsets.UTF_8);
        byte[] likeKeyBytes = likeSetKey.getBytes(StandardCharsets.UTF_8);
        byte[] countField = "likedCount".getBytes(StandardCharsets.UTF_8);
        byte[] uidBytes = userId.toString().getBytes(StandardCharsets.UTF_8);

        try {
            stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                // 两层缓存点赞数-1
                connection.hashCommands().hIncrBy(liteBytes, countField, -1);
                connection.hashCommands().hIncrBy(fullBytes, countField, -1);
                // 移除点赞用户
                connection.zSetCommands().zRem(likeKeyBytes, uidBytes);
                return null;
            });
        } catch (Exception e) {
            log.error("取消点赞Redis操作异常 blogId:{},userId:{}", blogId, userId, e);
            return false;
        }

        // 点赞成功返回true
        return true;
    }

    /**
     * 校验博客两层缓存是否都存在（练习专用，生产建议查数据库）
     */
    private void checkBlogCacheExist(Long blogId, String liteKey, String fullKey) {
        Boolean hasLite = stringRedisTemplate.hasKey(liteKey);
        Boolean hasFull = stringRedisTemplate.hasKey(fullKey);
        // 避免空指针，用常量比对
        if (!Boolean.TRUE.equals(hasLite) || !Boolean.TRUE.equals(hasFull)) {
            log.warn("博客缓存缺失，blogId={}, liteKey={}, fullKey={}", blogId, liteKey, fullKey);
            throw new RuntimeException("博客不存在");
        }
    }

    /**
     * 获取博客点赞总数
     */
    @Override
    public long getLikeCount(Long blogId) {
        if (blogId == null) {
            return 0;
        }
        String liteKey = BLOG_LITE_PREFIX + blogId;
        String fullKey = BLOG_FULL_PREFIX + blogId;
        // 先校验缓存
        checkBlogCacheExist(blogId, liteKey, fullKey);

        // 优先读取轻量缓存（Feed列表高频读取）
        Object countObj = stringRedisTemplate.opsForHash().get(liteKey, "likedCount");
        if (countObj != null) {
            return Long.parseLong(countObj.toString());
        }
        // 兜底读完整缓存
        Object fullCount = stringRedisTemplate.opsForHash().get(fullKey, "likedCount");
        return fullCount == null ? 0 : Long.parseLong(fullCount.toString());
    }

    public List<Long> getLikedUsers(Long blogId, int count) {
        // 参数校验
        if (blogId == null || count <= 0) {
            return Collections.emptyList();
        }
        // 限制最大条数，防止一次性查几万条
        int limit = Math.min(count, 20);

        String liteKey = BLOG_LITE_PREFIX + blogId;
        String fullKey = BLOG_FULL_PREFIX + blogId;
        String likeZSetKey = BLOG_LIKE_PREFIX + blogId;

        // 校验博客缓存存在
        checkBlogCacheExist(blogId, liteKey, fullKey);

        // 倒序取最新 N 条：0 ~ limit-1
        Set<String> userIdObjSet = stringRedisTemplate.opsForZSet()
                .reverseRange(likeZSetKey, 0, limit - 1);

        if (userIdObjSet != null && userIdObjSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 转Long集合返回
        if (userIdObjSet != null) {
            return userIdObjSet.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Blog> getBlogsByUser(Long userId, int pageNum, int pageSize) {
        String authorkey = BLOG_AUTHOR_PREFIX + userId;
        Set<String> range = stringRedisTemplate.opsForZSet().reverseRange(authorkey, pageNum - 1, pageNum + pageSize - 1);
        if (range == null) {
            return Collections.emptyList();
        }
        List<String> keys = range.stream()
                .map(id -> BLOG_FULL_PREFIX + id)
                .toList();

        // 使用原生的 RedisConnection 确保拿到的是未经序列化的原始 byte[]
        List<Object> pipeResult = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        List<Blog> blogList = pipeResult.stream()
                // 安全起见，先过滤掉 null，防止 Redis 中有些 key 不存在导致管道返回 null
                .filter(Objects::nonNull)
                // 转换
                .map(o -> {
                    // 确保类型正确再转，如果 stringRedisTemplate 自动转成了 Map<String, String>，就直接用
                    if (o instanceof Map<?, ?> rawMap) {
                        if (rawMap.isEmpty()) return Collections.<String, String>emptyMap();
                        // 检查判断：如果是 byte[] 则调用你的 convertByteMap
                        Map.Entry<?, ?> entry = rawMap.entrySet().iterator().next();
                        if (entry.getKey() instanceof byte[]) {
                            return convertByteMap((Map<byte[], byte[]>) rawMap);
                        }
                        // 如果已经是 String 类型的 Map，直接强转返回
                        return (Map<String, String>) rawMap;
                    }
                    return Collections.<String, String>emptyMap();
                })
                .filter(m -> !m.isEmpty() && m.containsKey("id")) // 过滤空数据，且确保核心字段存在
                .map(m -> Blog.builder()
                        .id(Long.parseLong(m.get("id")))
                        .userId(Long.parseLong(m.get("userId")))
                        .nickname(m.get("nickname"))
                        .title(m.get("title"))
                        .content(m.get("content"))
                        .images(m.get("images"))
                        .likedCount(m.get("likedCount") == null ? 0L : Long.parseLong(m.get("likedCount")))
                        .build())
                .toList();
        return blogList;
    }
    public static Map<String, String> convertByteMap(Map<byte[], byte[]> byteMap) {
        Map<String, String> map = new HashMap<>(byteMap.size());
        byteMap.forEach((k, v) -> {
            String field = new String(k, StandardCharsets.UTF_8);
            String val = v == null ? "" : new String(v, StandardCharsets.UTF_8);
            map.put(field, val);
        });
        return map;
    }
}
