package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.SysUser;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    SysUser login(String username, String password);

    boolean register(SysUser user);

    List<SysUser> getRepairmanList();

    List<SysUser> getStudentList();

    List<SysUser> getOnlineRepairmanList();

    List<SysUser> getOnlineStudentList();

    boolean isOnline(Long userId);

}
