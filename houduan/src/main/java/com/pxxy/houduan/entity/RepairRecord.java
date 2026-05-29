package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_record")
public class RepairRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long repairUserId;

    private Integer action;

    private String remark;

    private LocalDateTime createTime;

}
