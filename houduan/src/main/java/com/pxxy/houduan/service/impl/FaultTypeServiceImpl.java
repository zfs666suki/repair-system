package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.FaultType;
import com.pxxy.houduan.mapper.FaultTypeMapper;
import com.pxxy.houduan.service.FaultTypeService;
import org.springframework.stereotype.Service;

/**
 * 故障类型服务实现类
 * 提供故障类型管理的基础业务逻辑，继承MyBatis-Plus通用服务
 */
@Service
public class FaultTypeServiceImpl extends ServiceImpl<FaultTypeMapper, FaultType> implements FaultTypeService {

}
