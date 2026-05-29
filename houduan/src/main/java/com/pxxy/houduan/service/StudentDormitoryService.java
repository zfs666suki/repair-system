package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.StudentDormitory;

import java.util.Map;

public interface StudentDormitoryService extends IService<StudentDormitory> {

    StudentDormitory getActiveDormitoryByStudentId(Long studentId);

    Map<String, Object> getStudentDormitoryDetails(Long studentId);

}
