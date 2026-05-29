package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息通知实体类
 * 对应数据库表 notification，存储系统通知和个人聊天消息
 */
@Data
@TableName("notification")
public class Notification {

    /**
     * 消息ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收消息的用户ID
     */
    private Long userId;

    /**
     * 消息类型：1-系统消息，2-个人消息
     */
    private Integer type;

    /**
     * 消息子类型：用于区分不同业务场景（如报修单状态变更、维修进度更新等）
     */
    private Integer subType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联的报修单ID（系统消息使用）
     */
    private Long orderId;

    /**
     * 关联的用户ID（个人消息中表示发送者ID）
     */
    private Long relatedUserId;

    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;

}
