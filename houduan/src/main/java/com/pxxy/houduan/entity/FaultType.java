package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("fault_type")
public class FaultType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String typeName;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

}
