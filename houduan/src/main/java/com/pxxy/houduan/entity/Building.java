package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("building")
public class Building {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String buildingName;

    private Long repairUserId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
