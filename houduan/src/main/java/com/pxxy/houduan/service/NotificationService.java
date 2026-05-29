package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    void sendSystemNotification(Long orderId, Integer subType, String title, String content, List<Long> userIds);

    void sendPersonalNotification(Long senderId, Long receiverId, Integer subType, String title, String content);

    List<Notification> getUserNotifications(Long userId);

    IPage<Notification> getUserNotificationsPage(Long userId, long current, long size);

    int getUnreadCount(Long userId);

    int getUnreadCountByType(Long userId, Integer type);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId, Long otherUserId);

    List<Notification> getChatMessages(Long userId1, Long userId2);

    void sendWebSocketMessage(Long userId, Notification notification);

}
