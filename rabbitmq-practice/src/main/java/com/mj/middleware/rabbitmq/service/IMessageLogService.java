package com.mj.middleware.rabbitmq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mj.middleware.rabbitmq.entity.MessageLogMP;

import java.util.List;

/**
 * 消息日志 Service 接口 — 继承 IService 拥有批量 CRUD
 */
public interface IMessageLogService extends IService<MessageLogMP> {

    /** 根据 ID 查询消息日志 */
    MessageLogMP get(Long id);

    /** 记录消息日志 */
    void addLog(MessageLogMP messageLog);

    /** 更新消息状态 */
    void updateStatus(Long id, Integer status);

    /** 根据消息ID查询 */
    MessageLogMP getByMessageId(String messageId);

    /** 分页查询消息日志 */
    IPage<MessageLogMP> listLog(Integer pageNum, Integer pageSize);

    /** 查询待重试的消息列表 */
    List<MessageLogMP> listRetryable();
}
