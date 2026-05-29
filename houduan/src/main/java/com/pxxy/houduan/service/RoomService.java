package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pxxy.houduan.entity.Room;

import java.util.List;
import java.util.Map;

public interface RoomService extends IService<Room> {

    /**
     * 获取房间使用情况统计
     * @param buildingId 楼栋ID，可选，如果为null则统计所有楼栋
     * @return 房间使用情况统计列表
     */
    List<Map<String, Object>> getRoomUsageStatistics(Long buildingId);

}
