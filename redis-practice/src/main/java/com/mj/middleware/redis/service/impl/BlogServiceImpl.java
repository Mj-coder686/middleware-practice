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
import org.springframework.data.redis.core.ZSetOperations;
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
        List<Object> pipelined = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fanId : fanIdList) {
                String fanskey = FEED_PREFIX + fanId;
                connection.zAdd(fanskey.getBytes(StandardCharsets.UTF_8), publishTs, blogId.toString().getBytes(StandardCharsets.UTF_8));
            }
            return null;
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

        // 1. 修复分页区间 Bug，计算出正确的 start 和 end
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;

        Set<String> range = stringRedisTemplate.opsForZSet().reverseRange(authorkey, start, end);
        if (range == null || range.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> keys = range.stream()
                .map(id -> BLOG_FULL_PREFIX + id)
                .toList();

        // 2. 使用强转 Lambda 表达式，既不报红，又省去了匿名内部类的臃肿
        // 同时用 opsForHash().entries 替代底层 connection，彻底跟 byte[] 说再见！
        List<Object> pipeResult = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : keys) {
                stringRedisTemplate.opsForHash().entries(key);
            }
            return null;
        });

        // 3. 干净纯粹的 Stream 流转换，出来的直接就是 String 类型的 Map
        return pipeResult.stream()
                .filter(Objects::nonNull)
                .map(obj -> (Map<Object, Object>) obj) // 强转为普通 Map
                .filter(map -> !map.isEmpty() && map.containsKey("id")) // 过滤空数据，确保核心字段存在
                .map(map -> Blog.builder()
                        .id(Long.parseLong((String) map.get("id")))
                        .userId(Long.parseLong((String) map.get("userId")))
                        .nickname((String) map.get("nickname"))
                        .title((String) map.get("title"))
                        .content((String) map.get("content"))
                        .images((String) map.get("images"))
                        .likedCount(map.get("likedCount") == null ? 0L : Long.parseLong((String) map.get("likedCount")))
                        .build())
                .toList();
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

    @Override
    public Map<String, Object> getFeed(Long max, Integer offset) {
        Long currentUser = UserContext.getCurrentUser();
        String userKey = FEED_PREFIX + currentUser;

        // 1. ZSet 滚动分页查询
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate
                .opsForZSet()
                .reverseRangeByScoreWithScores(userKey, 0, max, offset, 5);

        if (typedTuples == null || typedTuples.isEmpty()){
            return Collections.emptyMap();
        }

        // 2. 解析 ZSet 数据（获取 ID、计算 minTime 和 os）
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 0;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            ids.add(Long.parseLong(Objects.requireNonNull(typedTuple.getValue())));
            long time = Objects.requireNonNull(typedTuple.getScore()).longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }

        // 3. 管道批量查询 Hash（用 RedisTemplate 封装的方法，避开底层的 connection 和 byte[] 转换）
        List<Object> pipeResult = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long id : ids) {
                // stringRedisTemplate 的序列化器会自动帮你处理 String 到 byte[] 的转换
                stringRedisTemplate.opsForHash().entries(BLOG_LITE_PREFIX + id);
            }
            return null;
        });

        // 4. 解析管道结果并组装成 Blog 对象
        List<Blog> blogList = pipeResult.stream()
                .filter(Objects::nonNull)
                .map(obj -> (Map<Object, Object>) obj) // 转换成普通 Map
                .filter(map -> !map.isEmpty() && map.containsKey("id")) // 过滤掉空 Hash
                .map(this::mapToBlog) // 抽离出来的转换方法，让主流程变干净！
                .toList();

        // 5. 封装最终的滚动分页结果
        Map<String, Object> result = new HashMap<>(3);
        result.put("blogs", blogList);
        result.put("minTime", minTime);
        result.put("offset", os);

        return result;
    }

    /**
     * 职责单一：将 Redis 读出的 Map 转换为 Blog 对象
     */
    private Blog mapToBlog(Map<Object, Object> map) {
        // 既然是用 stringRedisTemplate，这里取出来的 Key 和 Value 都是 String
        return Blog.builder()
                .id(Long.parseLong((String) map.get("id")))
                .userId(Long.parseLong((String) map.get("userId")))
                .nickname((String) map.get("nickname"))
                .title((String) map.get("title"))
                .content((String) map.get("content"))
                .images((String) map.get("images"))
                .likedCount(map.get("likedCount") == null ? 0L : Long.parseLong((String) map.get("likedCount")))
                .build();
    }
}
