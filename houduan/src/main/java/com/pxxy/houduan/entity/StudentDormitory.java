package com.pxxy.houduan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_dormitory")
public class StudentDormitory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long buildingId;

    private Long roomId;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime endTime;

}