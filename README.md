# 校园宿舍报修系统

## 一、项目概述

### 1.1 项目简介

校园宿舍报修系统是一个基于 B/S 架构的宿舍设施报修管理平台，旨在提高校园宿舍报修工作的效率和管理水平。系统实现了报修申请、工单分配、维修处理、进度跟踪、数据统计等全流程数字化管理，为学校后勤管理部门提供了便捷高效的解决方案。

### 1.2 项目信息

| 项目信息 | 内容 |
|---------|------|
| 项目名称 | 校园宿舍报修系统设计与实现 |
| 姓名 | 曾繁盛 |
| 开发技术 | Spring Boot |

---

## 二、技术架构

### 2.1 技术选型

#### 后端技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 3.4.4 | 核心框架，简化 Spring 应用开发 |
| MyBatis-Plus | 3.5.7 | ORM 框架，简化数据库操作 |
| MySQL | 8.0.26 | 关系型数据库 |
| JWT | 0.9.1 | JSON Web Token，实现用户认证 |
| WebSocket | - | 实时双向通信，用于消息推送 |
| Knife4j | 4.4.0 | API 文档生成工具 |
| 阿里云 OSS | 3.17.4 | 对象存储服务，用于图片上传 |
| Apache POI | 5.2.3 | Excel 文件处理，用于数据导出 |

#### 前端技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Vue.js | 3.5.32 | 渐进式 JavaScript 框架 |
| Vue Router | 5.0.4 | 官方路由管理器 |
| Pinia | 3.0.4 | 状态管理库 |
| Element Plus | 2.13.7 | UI 组件库 |
| Axios | 1.16.0 | HTTP 请求库 |
| ECharts | 6.0.0 | 数据可视化图表库 |
| Vite | 8.0.8 | 前端构建工具 |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端展示层                               
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              
│  │   学生端           维修员端          管理员端                
│  │    Vue 3 +         Vue 3 +          Vue 3 +                 
│  │ Element Plus     Element Plus     Element Plus              
│  └─────────────┘  └─────────────┘  └─────────────┘              
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/WebSocket
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         后端服务层                               
│  ┌─────────────────────────────────────────────────────────┐    
│  │                    Spring Boot 3.4.4                        
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       
│  │  │Controller     Service      Mapper       Config    
│  │  │  控制器  │     服务层       数据层        配置层     
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘      
│  └─────────────────────────────────────────────────────────┘   
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐       
│  │    JWT        WebSocket      Knife4j         OSS          
│  │  身份认证       消息推送       API文档       文件存储         
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘        
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         数据存储层                               
│  ┌─────────────────────────────────────────────────────────┐    
│  │                    MySQL 8.0.26                          
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    
│  │  │ sys_user     building       room      fault_type 
│  │  │  用户表        楼栋表       房间表       故障类型    
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐    
│  │  │ repair_       repair_   notification   student_   
│  │  │  order         record                  dormitory    
│  │  │ 报修单表       维修记录     通知表       宿舍分配    
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘    
│  └─────────────────────────────────────────────────────────┘   
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、功能模块

### 3.1 用户角色

系统包含三种用户角色：

| 角色ID | 角色名称 | 权限说明 |
|-------|---------|---------|
| 1 | 学生 | 提交报修申请、查看报修进度、接收通知消息 |
| 2 | 维修员 | 接单、处理报修、更新维修进度、完成报修 |
| 3 | 管理员 | 系统管理、用户管理、数据统计、工单分配 |

### 3.2 学生端功能

| 功能模块 | 功能描述 |
|---------|---------|
| 首页 | 显示报修统计、快捷入口、最新消息 |
| 提交报修 | 选择楼栋房间、故障类型、填写描述、上传图片 |
| 报修列表 | 查看个人所有报修记录，支持状态筛选 |
| 报修详情 | 查看报修进度、维修员信息、处理结果 |
| 消息通知 | 接收系统通知、工单状态变更通知 |
| 在线聊天 | 与维修员实时沟通 |

### 3.3 维修员端功能

| 功能模块 | 功能描述 |
|---------|---------|
| 首页 | 显示待处理工单数量、今日完成数、快捷入口 |
| 待处理订单 | 查看待接单的报修工单，支持接单/拒单 |
| 处理中订单 | 查看正在处理的工单，更新处理进度 |
| 历史订单 | 查看已完成的维修记录 |
| 消息通知 | 接收新工单分配、系统通知 |
| 在线聊天 | 与学生实时沟通 |

### 3.4 管理员端功能

| 功能模块 | 功能描述 |
|---------|---------|
| 首页 | 数据概览、统计图表、待办事项 |
| 用户管理 | 管理学生、维修员账号，支持增删改查 |
| 学生导入 | 批量导入学生信息（Excel） |
| 楼栋管理 | 管理宿舍楼栋信息，分配维修员 |
| 房间管理 | 管理宿舍房间信息 |
| 宿舍管理 | 分配学生住宿信息 |
| 故障类型 | 管理报修故障分类 |
| 报修单管理 | 查看所有报修单，支持手动分配、状态管理 |
| 数据统计 | 报修数据可视化分析（ECharts 图表） |
| 数据导出 | 导出报修数据为 Excel 文件 |
| 消息通知 | 系统消息管理 |
| 在线聊天 | 与学生、维修员实时沟通 |

---

## 四、数据库设计

### 4.1 数据库表结构

#### 4.1.1 用户表 (sys_user)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| username | varchar(50) | 用户名 |
| password | varchar(100) | 密码（加密存储） |
| real_name | varchar(50) | 真实姓名 |
| phone | varchar(20) | 手机号 |
| role | tinyint | 角色：1-学生 2-维修员 3-管理员 |
| status | tinyint | 状态：0-禁用 1-启用 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

#### 4.1.2 楼栋表 (building)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| building_name | varchar(50) | 楼栋名称 |
| repair_user_id | bigint | 负责维修员ID |
| status | tinyint | 状态：0-禁用 1-启用 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

#### 4.1.3 房间表 (room)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| room_number | varchar(20) | 房间号 |
| building_id | bigint | 所属楼栋ID |
| status | tinyint | 状态：0-禁用 1-启用 |
| create_time | datetime | 创建时间 |

#### 4.1.4 故障类型表 (fault_type)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| type_name | varchar(50) | 类型名称 |
| description | varchar(200) | 描述 |
| status | tinyint | 状态：0-禁用 1-启用 |
| create_time | datetime | 创建时间 |

#### 4.1.5 报修单表 (repair_order)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| order_no | varchar(32) | 订单编号 |
| student_id | bigint | 学生ID |
| building_id | bigint | 楼栋ID |
| room_id | bigint | 房间ID |
| fault_type_id | bigint | 故障类型ID |
| description | varchar(500) | 故障描述 |
| images | varchar(1000) | 故障图片 |
| repair_user_id | bigint | 维修员ID |
| status | tinyint | 状态：0-已取消 1-待分配 2-待接单 3-处理中 4-已完成 5-已拒绝 |
| reject_reason | varchar(200) | 拒单理由 |
| repair_result | varchar(500) | 维修结果 |
| repair_images | varchar(1000) | 维修图片 |
| create_time | datetime | 提交时间 |
| accept_time | datetime | 接单时间 |
| complete_time | datetime | 完成时间 |

#### 4.1.6 维修记录表 (repair_record)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| order_id | bigint | 报修单ID |
| repair_user_id | bigint | 维修员ID |
| action | tinyint | 操作类型：1-分配 2-接受 3-拒绝 4-完成 |
| remark | varchar(500) | 备注/说明 |
| create_time | datetime | 操作时间 |

#### 4.1.7 消息通知表 (notification)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| user_id | bigint | 接收用户ID |
| type | tinyint | 消息类型：1-系统消息 2-订单消息 |
| sub_type | tinyint | 子类型 |
| title | varchar(100) | 消息标题 |
| content | text | 消息内容 |
| order_id | bigint | 关联报修单ID |
| related_user_id | bigint | 关联用户ID |
| is_read | tinyint | 是否已读：0-未读 1-已读 |
| create_time | datetime | 创建时间 |

#### 4.1.8 学生宿舍分配表 (student_dormitory)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | bigint | 主键ID |
| student_id | bigint | 学生ID |
| building_id | bigint | 楼栋ID |
| room_id | bigint | 房间ID |
| bed_number | int | 床位号 |
| create_time | datetime | 创建时间 |

### 4.2 ER 关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  sys_user   │       │  building   │       │    room     │
│   (用户)    │       │   (楼栋)    │       │   (房间)    │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄──────│repair_user_id│      │ id (PK)     │
│ username    │       │ id (PK)     │◄──────│ building_id │
│ real_name   │       │building_name│       │ room_number │
│ role        │       └─────────────┘       └─────────────┘
└─────────────┘              │                     │
      │                      │                     │
      │                      │                     │
      ▼                      ▼                     ▼
┌─────────────────────────────────────────────────────────┐
│                    repair_order （报修单）               │ 
├─────────────────────────────────────────────────────────┤
│ id (PK)                                                 │
│ order_no          │ student_id (FK)                     │
│ building_id (FK)  │ room_id (FK)                        │
│ fault_type_id(FK) │ repair_user_id (FK)                 │
│ description       │ status                              │
│ images            │ repair_result                       │
│ create_time       │ complete_time                       │
└─────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│                  repair_record （维修记录）              │
├─────────────────────────────────────────────────────────┤
│ id (PK)          │ order_id (FK)                        │
│ repair_user_id   │ action                               │
│ remark           │ create_time                          │
└─────────────────────────────────────────────────────────┘
```

---

## 五、核心功能流程

### 5.1 报修流程

```
┌─────────┐     ┌─────────┐     ┌─────────┐      ┌─────────┐     ┌─────────┐
│ 学生提交 ───▶   系统分配 ───▶   维修接单 ───▶    维修处理 ───▶  完成确认 
│ 报修申请          维修员         处理工单          上传结果        评价反馈  
└─────────┘     └─────────┘     └─────────┘      └─────────┘     └─────────┘
     │               │               │                 │              │
     │               │               │                 │              │
     ▼               ▼               ▼                 ▼              ▼
  待分配           待接单          处理中             处理中         已完成
  (status=1)    (status=2)      (status=3)         (status=3)    (status=4)
```

### 5.2 工单状态流转

| 状态值 | 状态名称 | 说明 |
|-------|---------|------|
| 0 | 已取消 | 学生取消或系统取消 |
| 1 | 待分配 | 新提交的报修单，等待分配维修员 |
| 2 | 待接单 | 已分配维修员，等待维修员接单 |
| 3 | 处理中 | 维修员已接单，正在处理 |
| 4 | 已完成 | 维修完成 |
| 5 | 已拒绝 | 维修员拒绝接单，需重新分配 |

---

## 六、项目结构

### 6.1 后端项目结构

```
houduan/
└── src/
    └── main/
        ├── java/com/pxxy/houduan/
        │   ├── common/              # 公共模块
        │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
        │   │   ├── JsonUtils.java               # JSON工具类
        │   │   ├── JwtUtils.java                 # JWT工具类
        │   │   ├── PageResult.java               # 分页结果封装
        │   │   ├── PermissionUtils.java         # 权限工具类
        │   │   └── Result.java                   # 统一响应结果
        │   ├── config/               # 配置类
        │   │   ├── JwtInterceptor.java          # JWT拦截器
        │   │   ├── Knife4jConfig.java           # API文档配置
        │   │   ├── MyBatisPlusConfig.java       # MyBatis-Plus配置
        │   │   ├── OSSConfig.java               # 阿里云OSS配置
        │   │   ├── WebConfig.java               # Web配置
        │   │   ├── WebSocketConfig.java         # WebSocket配置
        │   │   └── WebSocketHandler.java         # WebSocket处理器
        │   ├── controller/           # 控制器层
        │   │   ├── AdminController.java         # 管理员接口
        │   │   ├── NotificationController.java  # 消息通知接口
        │   │   ├── RepairOrderController.java    # 报修单接口
        │   │   ├── StudentController.java       # 学生接口
        │   │   ├── SysUserController.java      # 用户接口
        │   │   └── UploadController.java        # 文件上传接口
        │   ├── dto/                  # 数据传输对象
        │   │   └── PageRequest.java             # 分页请求封装
        │   ├── entity/               # 实体类
        │   │   ├── Building.java                # 楼栋实体
        │   │   ├── FaultType.java               # 故障类型实体
        │   │   ├── Notification.java            # 消息通知实体
        │   │   ├── RepairOrder.java             # 报修单实体
        │   │   ├── RepairRecord.java            # 维修记录实体
        │   │   ├── Room.java                    # 房间实体
        │   │   ├── StudentDormitory.java        # 学生宿舍实体
        │   │   └── SysUser.java                 # 用户实体
        │   ├── mapper/               # 数据访问层
        │   │   ├── BuildingMapper.java
        │   │   ├── FaultTypeMapper.java
        │   │   ├── NotificationMapper.java
        │   │   ├── RepairOrderMapper.java
        │   │   ├── RepairRecordMapper.java
        │   │   ├── RoomMapper.java
        │   │   ├── StudentDormitoryMapper.java
        │   │   └── SysUserMapper.java
        │   ├── service/              # 服务层
        │   │   ├── impl/              # 服务实现
        │   │   │   ├── AdminServiceImpl.java
        │   │   │   ├── BuildingServiceImpl.java
        │   │   │   ├── ExportServiceImpl.java
        │   │   │   ├── FaultTypeServiceImpl.java
        │   │   │   ├── NotificationServiceImpl.java
        │   │   │   ├── RepairOrderServiceImpl.java
        │   │   │   ├── RepairRecordServiceImpl.java
        │   │   │   ├── RoomServiceImpl.java
        │   │   │   ├── StatisticsServiceImpl.java
        │   │   │   ├── StudentDormitoryServiceImpl.java
        │   │   │   └── SysUserServiceImpl.java
        │   │   ├── AdminService.java
        │   │   ├── BuildingService.java
        │   │   ├── ExportService.java
        │   │   ├── FaultTypeService.java
        │   │   ├── NotificationService.java
        │   │   ├── RepairOrderService.java
        │   │   ├── RepairRecordService.java
        │   │   ├── RoomService.java
        │   │   ├── StatisticsService.java
        │   │   ├── StudentDormitoryService.java
        │   │   └── SysUserService.java
        │   └── HouduanApplication.java  # 启动类
        └── resources/
            └── application.yaml          # 配置文件
```

### 6.2 前端项目结构

```
qianduan/
├── public/
│   └── favicon.ico               # 网站图标
├── src/
│   ├── api/                      # API 接口
│   │   ├── admin.js              # 管理员接口
│   │   ├── auth.js               # 认证接口
│   │   ├── notification.js       # 消息通知接口
│   │   ├── repair.js             # 报修接口
│   │   └── student.js            # 学生接口
│   ├── assets/                   # 静态资源
│   │   ├── img/                  # 图片资源
│   │   └── styles/               # 样式文件
│   ├── components/               # 公共组件
│   │   ├── Common/               # 通用组件
│   │   │   ├── ActionCard.vue
│   │   │   ├── DetailModal.vue
│   │   │   ├── ImageUploader.vue
│   │   │   ├── Pagination.vue
│   │   │   ├── SearchForm.vue
│   │   │   ├── StatusBadge.vue
│   │   │   └── UnreadBadge.vue
│   │   └── Layout/               # 布局组件
│   │       ├── AdminLayout.vue
│   │       ├── BaseLayout.vue
│   │       ├── RepairmanLayout.vue
│   │       └── StudentLayout.vue
│   ├── composables/              # 组合式函数
│   │   ├── useList.js            # 列表处理
│   │   └── useWebSocket.js       # WebSocket
│   ├── router/                   # 路由配置
│   │   └── index.js
│   ├── stores/                   # 状态管理
│   │   └── user.js               # 用户状态
│   ├── utils/                    # 工具函数
│   │   ├── axios.js              # Axios 封装
│   │   ├── eventBus.js           # 事件总线
│   │   ├── index.js              # 通用工具
│   │   └── websocket.js          # WebSocket 封装
│   ├── views/                    # 页面视图
│   │   ├── Admin/                # 管理员页面
│   │   │   ├── AdminChatView.vue
│   │   │   ├── AdminHomeView.vue
│   │   │   ├── AdminMessagesView.vue
│   │   │   ├── AdminNotificationView.vue
│   │   │   ├── BuildingManagementView.vue
│   │   │   ├── DormitoryManagementView.vue
│   │   │   ├── ExportView.vue
│   │   │   ├── FaultTypeManagementView.vue
│   │   │   ├── ImportStudentsView.vue
│   │   │   ├── RepairOrderManagementView.vue
│   │   │   ├── RoomManagementView.vue
│   │   │   ├── StatisticsView.vue
│   │   │   └── UserManagementView.vue
│   │   ├── Auth/                 # 认证页面
│   │   │   └── LoginView.vue
│   │   ├── Repairman/            # 维修员页面
│   │   │   ├── HistoryOrdersView.vue
│   │   │   ├── PendingOrdersView.vue
│   │   │   ├── ProcessingOrdersView.vue
│   │   │   ├── RepairOrderDetailView.vue
│   │   │   ├── RepairmanChatView.vue
│   │   │   ├── RepairmanHomeView.vue
│   │   │   ├── RepairmanMessagesView.vue
│   │   │   └── RepairmanNotificationView.vue
│   │   └── Student/              # 学生页面
│   │       ├── ChatView.vue
│   │       ├── MessagesView.vue
│   │       ├── NotificationListView.vue
│   │       ├── RepairDetailView.vue
│   │       ├── StudentHomeView.vue
│   │       ├── StudentRepairListView.vue
│   │       └── SubmitRepairView.vue
│   ├── App.vue                   # 根组件
│   └── main.js                   # 入口文件
├── index.html                    # HTML 模板
├── package.json                  # 项目配置
├── vite.config.js                 # Vite 配置
└── jsconfig.json                  # JS 配置
```

---

## 七、环境要求与部署

### 7.1 开发环境

| 环境 | 版本要求 |
|-----|---------|
| JDK | 17+ |
| Node.js | 20.19.0+ 或 22.12.0+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |
| pnpm | 8.0+ |

### 7.2 后端部署

1. **数据库配置**
   ```sql
   -- 创建数据库
   CREATE DATABASE repair_system;
   -- 导入数据库脚本
   SOURCE repair_system.sql;
   ```

2. **修改配置文件**
   编辑 `application.yaml`，配置数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/repair_system
       username: root
       password: your_password
   ```

3. **启动后端服务**
   ```bash
   cd houduan
   mvn spring-boot:run
   ```
   后端服务将在 `http://localhost:8080` 启动

### 7.3 前端部署

1. **安装依赖**
   ```bash
   cd qianduan
   pnpm install
   ```

2. **开发模式运行**
   ```bash
   pnpm dev
   ```

3. **生产环境打包**
   ```bash
   pnpm build
   ```
   打包后的文件在 `dist` 目录下

### 7.4 API 文档

启动后端服务后，访问 Knife4j API 文档：
```
http://localhost:8080/doc.html
```

---

## 八、系统特色

### 8.1 技术亮点

1. **前后端分离架构**：采用 Vue 3 + Spring Boot 的前后端分离架构，提高开发效率和系统可维护性

2. **JWT 身份认证**：使用 JWT 实现无状态身份认证，支持多端登录和会话管理

3. **WebSocket 实时通信**：实现消息实时推送，提升用户体验

4. **阿里云 OSS 存储**：图片文件存储在云端，减轻服务器压力

5. **数据可视化**：使用 ECharts 实现数据统计图表展示

6. **Excel 导入导出**：支持学生信息批量导入和报修数据导出

### 8.2 功能亮点

1. **智能工单分配**：系统自动根据楼栋分配对应维修员

2. **全流程状态跟踪**：从报修提交到完成，全程状态可追踪

3. **实时消息通知**：工单状态变更实时推送通知

4. **多角色权限控制**：基于角色的访问控制，确保数据安全

5. **在线沟通**：学生与维修员可实时在线沟通

---

## 九、总结

本系统采用主流的 Spring Boot + Vue 3 技术栈开发，实现了校园宿舍报修的全流程数字化管理。系统功能完善，涵盖了报修申请、工单分配、维修处理、进度跟踪、数据统计等核心功能，能够有效提升校园后勤管理效率，为学生提供便捷的报修服务体验。

系统具有良好的可扩展性和可维护性，代码结构清晰，注释完善，便于后续功能扩展和维护升级。
