package com.mj.middleware.rabbitmq.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mj.middleware.rabbitmq.entity.MessageLogMP;
import com.mj.middleware.rabbitmq.mapper.MessageLogMapper;
import com.mj.middleware.rabbitmq.service.IMessageLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息日志 Service 实现
 */
@Slf4j
@Service
public class MessageLogServiceImpl extends ServiceImpl<MessageLogMapper, MessageLogMP> implements IMessageLogService {

    /** 消息状态常量 */
    private static final int STATUS_SENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;
    private static final int STATUS_CONSUMED = 3;

    @Override
    public MessageLogMP get(Long id) {
        return getById(id);
    }

    @Override
    public void addLog(MessageLogMP messageLog) {
        if (messageLog.getStatus() == null) {
            messageLog.setStatus(STATUS_SENDING);
        }
        if (messageLog.getRetryCount() == null) {
            messageLog.setRetryCount(0);
        }
        boolean result = save(messageLog);
        log.info("记录消息日志: messageId={}, 结果: {}", messageLog.getMessageId(), result);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        MessageLogMP exist = getById(id);
        if (exist == null) {
            throw new RuntimeException("消息日志不存在: id=" + id);
        }
        exist.setStatus(status);
        updateById(exist);
    }

    @Override
    public MessageLogMP getByMessageId(String messageId) {
        return lambdaQuery().eq(MessageLogMP::getMessageId, messageId).one();
    }

    @Override
    public IPage<MessageLogMP> listLog(Integer pageNum, Integer pageSize) {
        Page<MessageLogMP> page = new Page<>(pageNum, pageSize);
        return lambdaQuery().orderByDesc(MessageLogMP::getCreateTime).page(page);
    }

    @Override
    public List<MessageLogMP> listRetryable() {
        // 查询发送失败且未超过最大重试次数的消息
        return lambdaQuery()
                .eq(MessageLogMP::getStatus, STATUS_FAILED)
                .lt(MessageLogMP::getRetryCount, 3)
                .le(MessageLogMP::getNextRetryTime, LocalDateTime.now())
                .list();
    }
}
