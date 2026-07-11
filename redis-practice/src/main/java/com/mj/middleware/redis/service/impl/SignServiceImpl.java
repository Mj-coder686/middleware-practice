package com.mj.middleware.redis.service.impl;

import com.mj.middleware.redis.entity.SignRecord;
import com.mj.middleware.redis.mapper.SignMapper;
import com.mj.middleware.redis.service.ISignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 签到业务实现 — TODO: 使用 Redis Bitmap / HyperLogLog 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements ISignService {

    private final SignMapper signMapper;
    private final StringRedisTemplate stringRedisTemplate;
    // 档位积分
    private static final int POINT_L7 = 40;
    private static final int POINT_L14 = 20;
    private static final int POINT_L28 = 80;
    // 档位标识
    private static final String L7 = "level7";
    private static final String L14 = "level14";
    private static final String L28 = "level28";

    @Override
    public void sign(Long userId) {
        String monthKey = "sign:" + "_all" + YearMonth.now();
        String key = "sign:" + userId + ":" + YearMonth.now();
        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();
        Boolean aBoolean = stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        if (Boolean.FALSE.equals(aBoolean)){
            stringRedisTemplate.opsForSet().add(monthKey, String.valueOf(userId));
        }
    }

    @Override
    public SignRecord getSignRecord(Long userId, String month) {
        // TODO: Redis BITFIELD sign:{userId}:{month} GET u8 0 — 读取本月签到状态
        String yearstr = month.substring(0, 4);
        String monthstr = month.substring(4);
        int now_year = Integer.parseInt(yearstr);
        int now_month = Integer.parseInt(monthstr);
        if (now_month <= 0 || now_month > 12){
            throw new RuntimeException("无效的月份");
        }
        YearMonth yearMonth = YearMonth.of(now_year, now_month);
        int dayCount = yearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        int dayOfMonth = today.getDayOfMonth();
        if (dayOfMonth > dayCount){
            throw new RuntimeException("无效的日期");
        }
        String key = "sign:" + userId + ":" + month ;
        int consecutiveDays = 0;
        for (int day = 1 ; day <= dayOfMonth; day++){
            int offset = day - 1;
            Boolean bit = stringRedisTemplate.opsForValue().getBit(key, offset);
            if ( bit){
                consecutiveDays++;
            }
        }
        SignRecord signRecord = new SignRecord(userId, LocalDate.now(), consecutiveDays, dayOfMonth, false);
        return null;
    }

    @Override
    public int getConsecutiveDays(Long userId) {
        int consecutiveDays = 0;
        int maxDays = 0;
        String key = "sign:" + userId + ":" + YearMonth.now();
        LocalDate now = LocalDate.now();
        int dayCount = now.getDayOfMonth() ;
        for (int day = dayCount; day > 0; day--){
            long offset = day - 1;
            Boolean bit = stringRedisTemplate.opsForValue().getBit(key, offset);
            if (Boolean.TRUE.equals(bit)){
                consecutiveDays++;
            }
            else {
                maxDays = Math.max(maxDays, consecutiveDays);
                consecutiveDays = 0;
            }
        }
        maxDays = Math.max(maxDays, consecutiveDays);
        return maxDays;
    }

    @Override
    public void claimReward(Long userId) {
        int consecutiveDays = getCurrentDays(userId);
        String key = "sign:" + userId + ":" + YearMonth.now() ;
        if (consecutiveDays == 7  ){
            // 积分奖励 第一档
            stringRedisTemplate.opsForSet().add(key, String.valueOf(POINT_L7));
            log.info("用户 {} 签到 {} 天，发放第一档积分奖励", userId, consecutiveDays);
        } else if (consecutiveDays == 14) {
            // 积分奖励 第二档
            stringRedisTemplate.opsForSet().add(key , String.valueOf(POINT_L14));
            log.info("用户 {} 签到 {} 天，发放第二档积分奖励", userId, consecutiveDays);
        }
        else if (consecutiveDays == 28) {
            // 积分奖励 第三档
            stringRedisTemplate.opsForSet().add(key , String.valueOf(POINT_L28));
            log.info("用户 {} 签到 {} 天，发放第三档积分奖励", userId, consecutiveDays);
        }
    }

    @Override
    public long countUniqueSignUsers(String month) {
        String monthKey = "sign:" + "_all" + YearMonth.now();
        Long count = stringRedisTemplate.opsForSet().size(monthKey);
        return count == null ? 0 : count;
    }

    public int getCurrentDays(Long userId) {
        String key = "sign:" + userId + ":" + YearMonth.now();
        LocalDate now = LocalDate.now();
        int dayOfMonth = now.getDayOfMonth();

        // 优化：使用 BITFIELD 一次性获取本月位图，避免循环调用 Redis
        List<Long> bits = stringRedisTemplate.opsForValue()
                .bitField(key, BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));

        if (bits == null || bits.isEmpty() || bits.getFirst() == null) {
            return 0;
        }

        long bitmap = bits.getFirst();
        int consecutive = 0;

        // 从【今天】开始向前检查，遇到0立即中断
        for (int day = dayOfMonth - 1; day >= 0; day--) {
            if (((bitmap >> day) & 1L) == 1L) {
                consecutive++;
            } else {
                break;
            }
        }
        return consecutive;
    }
}
