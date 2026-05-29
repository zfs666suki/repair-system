package com.pxxy.houduan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pxxy.houduan.entity.Building;
import com.pxxy.houduan.entity.Room;
import com.pxxy.houduan.entity.StudentDormitory;
import com.pxxy.houduan.entity.SysUser;
import com.pxxy.houduan.mapper.StudentDormitoryMapper;
import com.pxxy.houduan.service.BuildingService;
import com.pxxy.houduan.service.RoomService;
import com.pxxy.houduan.service.StudentDormitoryService;
import com.pxxy.houduan.service.SysUserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 学生宿舍服务实现类
 * 提供学生宿舍信息查询及详细信息组装等业务逻辑
 */
@Service
public class StudentDormitoryServiceImpl extends ServiceImpl<StudentDormitoryMapper, StudentDormitory> implements StudentDormitoryService {

    private final BuildingService buildingService;
    private final RoomService roomService;
    private final SysUserService sysUserService;

    public StudentDormitoryServiceImpl(BuildingService buildingService, @Lazy RoomService roomService, SysUserService sysUserService) {
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.sysUserService = sysUserService;
    }

    /**
     * 根据学生ID查询其当前有效的宿舍信息（入住中）
     *
     * @param studentId 学生ID
     * @return 返回状态为入住中(1)的宿舍信息，未分配、已退宿或无记录时返回null
     */
    @Override
    public StudentDormitory getActiveDormitoryByStudentId(Long studentId) {
        // 查询学生ID匹配且状态为入住中(1)的宿舍记录
        // status: 0-已退宿, 1-入住中, 3-未分配
        return this.lambdaQuery()
                .eq(StudentDormitory::getStudentId, studentId)
                .eq(StudentDormitory::getStatus, 1)
                .one();
    }

    /**
     * 获取学生的完整宿舍详细信息
     * 包含宿舍、楼栋、房间及维修员信息
     *
     * @param studentId 学生ID
     * @return 包含完整宿舍信息的Map，若未分配宿舍则抛出异常
     * @throws IllegalArgumentException 当学生未分配宿舍时抛出
     */
    @Override
    public Map<String, Object> getStudentDormitoryDetails(Long studentId) {
        // 查询学生有效宿舍记录，未分配则抛出异常
        StudentDormitory dormitory = getActiveDormitoryByStudentId(studentId);
        if (dormitory == null) {
            throw new IllegalArgumentException("未分配宿舍");
        }

        // 关联查询楼栋、房间和维修员的详细信息
        Building building = buildingService.getById(dormitory.getBuildingId());
        Room room = roomService.getById(dormitory.getRoomId());
        
        // 处理维修员信息，避免空指针异常
        Map<String, Object> repairUserMap = null;
        if (building != null && building.getRepairUserId() != null) {
            SysUser repairUser = sysUserService.getById(building.getRepairUserId());
            if (repairUser != null) {
                repairUserMap = Map.of(
                    "id", repairUser.getId(),
                    "realName", repairUser.getRealName(),
                    "phone", repairUser.getPhone()
                );
            }
        }

        // 组装完整的宿舍信息响应数据
        Map<String, Object> result = new HashMap<>();
        result.put("id", dormitory.getId());
        result.put("studentId", dormitory.getStudentId());
        result.put("buildingId", dormitory.getBuildingId());
        result.put("buildingName", building != null ? building.getBuildingName() : null);
        result.put("roomId", dormitory.getRoomId());
        result.put("roomNumber", room != null ? room.getRoomNumber() : null);
        result.put("repairUser", repairUserMap);
        result.put("status", dormitory.getStatus());
        result.put("createTime", dormitory.getCreateTime());

        return result;
    }
}
