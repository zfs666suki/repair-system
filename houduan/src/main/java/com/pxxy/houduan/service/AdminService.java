package com.pxxy.houduan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pxxy.houduan.entity.*;

import java.util.List;
import java.util.Map;

public interface AdminService {

    SysUser addRepairman(SysUser user);

    SysUser updateRepairman(Long id, SysUser user);

    void toggleRepairmanStatus(Long id);

    SysUser addStudent(SysUser user);

    Map<String, Object> importStudents(byte[] fileData);

    void downloadStudentTemplate(jakarta.servlet.http.HttpServletResponse response);

    SysUser updateStudent(Long id, SysUser user);

    void toggleStudentStatus(Long id);

    void assignDormitory(StudentDormitory dormitory);

    void checkoutDormitory(Long id);

    Building addBuilding(Building building);

    Building updateBuilding(Long id, Building building);

    void toggleBuildingStatus(Long id);

    void deleteBuilding(Long id);

    Room addRoom(Room room);

    Room updateRoom(Long id, Room room);

    void toggleRoomStatus(Long id);

    void deleteRoom(Long id);

    FaultType addFaultType(FaultType faultType);

    FaultType updateFaultType(Long id, FaultType faultType);

    void toggleFaultTypeStatus(Long id);

    IPage<SysUser> listRepairmenPage(long current, long size);

    IPage<SysUser> listStudentsPage(long current, long size, String className);

    IPage<StudentDormitory> listDormitoriesPage(long current, long size);

    IPage<Room> listRoomsPage(Long buildingId, long current, long size);

    Map<String, Object> getRepairOrderDetail(Long orderId);

    void exportRepairOrders(String startDate, String endDate, jakarta.servlet.http.HttpServletResponse response);
}
