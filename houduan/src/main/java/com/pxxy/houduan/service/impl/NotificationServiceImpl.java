package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.common.JsonUtils;
import com.pxxy.houduan.config.WebSocketHandler;
import com.pxxy.houduan.entity.Notification;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.mapper.NotificationMapper;
import com.pxxy.houduan.mapper.SysUserMapper;
import com.pxxy.houduan.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息通知服务实现类
 * 提供系统通知、个人消息、聊天记录等功能，并集成WebSocket实时推送
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final WebSocketHandler webSocketHandler;
    private final SysUserMapper sysUserMapper;

    public NotificationServiceImpl(WebSocketHandler webSocketHandler, SysUserMapper sysUserMapper) {
        this.webSocketHandler = webSocketHandler;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 发送系统通知给多个用户
     * 支持批量发送并自动通过WebSocket推送
     *
     * @param orderId 关联的报修单ID
     * @param subType 消息子类型
     * @param title 消息标题
     * @param content 消息内容
     * @param userIds 接收消息的用户ID列表
     */
    @Override
    public void sendSystemNotification(Long orderId, Integer subType, String title, String content, List<Long> userIds) {
        // 遍历用户列表，逐个创建系统消息并推送
        for (Long userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(1); // 系统消息
            notification.setSubType(subType);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setOrderId(orderId);
            notification.setIsRead(0);
            notification.setCreateTime(LocalDateTime.now());
            baseMapper.insert(notification);
            logger.info("发送系统消息给用户 {}: {}", userId, title);
            sendWebSocketMessage(userId, notification);
        }
    }

    /**
     * 发送个人消息给指定用户
     * 建立用户间的聊天通信
     *
     * @param senderId 发送者用户ID
     * @param receiverId 接收者用户ID
     * @param subType 消息子类型
     * @param title 消息标题
     * @param content 消息内容
     */
    @Override
    public void sendPersonalNotification(Long senderId, Long receiverId, Integer subType, String title, String content) {
        // 只创建一条消息记录给接收方
        // 发送方可以通过getChatMessages查询到自己发送的消息
        Notification notification = new Notification();
        notification.setUserId(receiverId);
        notification.setType(2); // 个人消息
        notification.setSubType(subType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedUserId(senderId); // 发送者ID
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        baseMapper.insert(notification);
        logger.info("发送个人消息给用户 {}: {}", receiverId, title);
        sendWebSocketMessage(receiverId, notification);
    }

    /**
     * 查询指定用户的所有消息列表
     *
     * @param userId 用户ID
     * @return 消息列表，按创建时间降序排列
     */
    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return baseMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime)
        );
    }

    @Override
    public IPage<Notification> getUserNotificationsPage(Long userId, long current, long size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.orderByDesc(Notification::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    /**
     * 获取用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    @Override
    public int getUnreadCount(Long userId) {
        return Math.toIntExact(baseMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
        ));
    }

    /**
     * 获取用户指定类型的未读消息数量
     *
     * @param userId 用户ID
     * @param type 消息类型：1-系统通知，2-个人消息
     * @return 未读消息数量
     */
    @Override
    public int getUnreadCountByType(Long userId, Integer type) {
        return Math.toIntExact(baseMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getType, type)
                .eq(Notification::getIsRead, 0)
        ));
    }

    /**
     * 标记指定消息为已读状态
     * 包含权限校验和状态检查
     *
     * @param notificationId 消息ID
     * @param userId 操作用户ID（用于校验所有权）
     * @throws IllegalArgumentException 当消息不存在或无权操作时抛出
     */
    @Override
    public void markAsRead(Long notificationId, Long userId) {
        // 验证消息存在性及操作权限
        Notification notification = baseMapper.selectById(notificationId);
        if (notification == null) {
            logger.warn("尝试标记不存在的消息: {}", notificationId);
            throw new IllegalArgumentException("消息不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            logger.warn("用户 {} 尝试标记不属于自己的消息: {}", userId, notificationId);
            throw new IllegalArgumentException("无权操作此消息");
        }
        // 避免重复更新
        if (notification.getIsRead() == 1) {
            logger.debug("消息 {} 已经是已读状态，无需再次标记", notificationId);
            return;
        }
        notification.setIsRead(1);
        baseMapper.updateById(notification);
        logger.info("用户 {} 标记消息已读: {}", userId, notificationId);
    }

    /**
     * 标记与指定用户的所有聊天消息为已读
     *
     * @param userId 当前用户ID
     * @param otherUserId 对方用户ID
     */
    @Override
    public void markAllAsRead(Long userId, Long otherUserId) {
        // 更新当前用户收到的来自对方的所有未读消息
        baseMapper.update(
            null,
            new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getRelatedUserId, otherUserId)
                .eq(Notification::getType, 2) // 个人消息
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
        );
        logger.info("用户 {} 标记与用户 {} 的所有消息为已读", userId, otherUserId);
    }

    /**
     * 获取两个用户之间的聊天记录
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 聊天记录列表，按创建时间升序排列
     */
    @Override
    public List<Notification> getChatMessages(Long userId1, Long userId2) {
        // 查询双方之间的所有个人消息
        // userId1发送给userId2的消息: userId=userId1, relatedUserId=userId2
        // userId2发送给userId1的消息: userId=userId2, relatedUserId=userId1
        return baseMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getType, 2) // 个人消息
                .and(wrapper -> wrapper
                    .eq(Notification::getUserId, userId1)
                    .eq(Notification::getRelatedUserId, userId2)
                    .or()
                    .eq(Notification::getUserId, userId2)
                    .eq(Notification::getRelatedUserId, userId1)
                )
                .orderByAsc(Notification::getCreateTime)
        );
    }

    /**
     * 通过WebSocket向指定用户实时推送消息
     * 异常捕获确保推送失败不影响主流程
     *
     * @param userId 目标用户ID
     * @param notification 消息对象
     */
    @Override
    public void sendWebSocketMessage(Long userId, Notification notification) {
        try {
            String message = JsonUtils.toJson(notification);
            webSocketHandler.sendMessage(userId, message);
        } catch (Exception e) {
            logger.error("发送WebSocket消息失败", e);
        }
    }

}
