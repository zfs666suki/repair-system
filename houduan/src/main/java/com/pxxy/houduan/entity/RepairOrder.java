package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long studentId;

    private Long buildingId;

    private Long roomId;

    private Long faultTypeId;

    private String description;

    private String images;

    private Long repairUserId;

    private Integer status;

    private String rejectReason;

    private String repairResult;

    private String repairImages;

    private LocalDateTime createTime;

    private LocalDateTime acceptTime;

    private LocalDateTime completeTime;

    private LocalDateTime updateTime;

}
