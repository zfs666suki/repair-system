package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.Building;
import com.pxxy.houduan.mapper.BuildingMapper;
import com.pxxy.houduan.service.BuildingService;
import org.springframework.stereotype.Service;

/**
 * 楼栋服务实现类
 * 提供楼栋管理的基础业务逻辑，继承MyBatis-Plus通用服务
 */
@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

}
