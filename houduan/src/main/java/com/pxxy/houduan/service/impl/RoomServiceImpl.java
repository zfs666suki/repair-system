package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.Room;
import com.pxxy.houduan.entity.StudentDormitory;
import com.pxxy.houduan.entity.Building;
import com.pxxy.houduan.mapper.RoomMapper;
import com.pxxy.houduan.service.RoomService;
import com.pxxy.houduan.service.StudentDormitoryService;
import com.pxxy.houduan.service.BuildingService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 房间服务实现类
 * 提供房间使用情况统计等功能
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final StudentDormitoryService studentDormitoryService;
    private final BuildingService buildingService;

    public RoomServiceImpl(StudentDormitoryService studentDormitoryService, BuildingService buildingService) {
        this.studentDormitoryService = studentDormitoryService;
        this.buildingService = buildingService;
    }

    /**
     * 获取房间使用情况统计信息
     * 支持按楼栋筛选或统计所有房间
     *
     * @param buildingId 楼栋ID（可选，传入则只统计该楼栋，null则统计所有）
     * @return 房间使用统计列表，包含房间号、楼栋、入住人数等信息
     */
    @Override
    public List<Map<String, Object>> getRoomUsageStatistics(Long buildingId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        // 根据条件查询房间列表
        List<Room> rooms;
        if (buildingId != null) {
            rooms = this.lambdaQuery().eq(Room::getBuildingId, buildingId).list();
        } else {
            rooms = this.list();
        }
        
        // 查询所有状态为正常(1)的宿舍分配记录
        List<StudentDormitory> activeDormitories = studentDormitoryService.lambdaQuery()
                .eq(StudentDormitory::getStatus, 1)
                .list();
        
        // 构建房间ID到入住人数的映射关系
        Map<Long, Integer> roomOccupancyMap = new HashMap<>();
        for (StudentDormitory dormitory : activeDormitories) {
            roomOccupancyMap.put(dormitory.getRoomId(), 
                roomOccupancyMap.getOrDefault(dormitory.getRoomId(), 0) + 1);
        }
        
        // 遍历房间列表，组装每个房间的统计数据
        for (Room room : rooms) {
            Map<String, Object> roomStats = new HashMap<>();
            roomStats.put("roomId", room.getId());
            roomStats.put("roomNumber", room.getRoomNumber());
            roomStats.put("buildingId", room.getBuildingId());
            roomStats.put("status", room.getStatus());
            
            // 关联查询楼栋名称
            if (room.getBuildingId() != null) {
                Building building = buildingService.getById(room.getBuildingId());
                if (building != null) {
                    roomStats.put("buildingName", building.getBuildingName());
                }
            }
            
            // 计算房间入住人数及占用状态
            int occupancy = roomOccupancyMap.getOrDefault(room.getId(), 0);
            roomStats.put("occupancy", occupancy);
            roomStats.put("isOccupied", occupancy > 0);
            
            resultList.add(roomStats);
        }
        
        return resultList;
    }

}
