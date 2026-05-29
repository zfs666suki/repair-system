-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: repair_system
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `building`
--

DROP TABLE IF EXISTS `building`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `building` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_name` varchar(50) NOT NULL COMMENT '楼栋名称',
  `repair_user_id` bigint DEFAULT NULL COMMENT '负责维修员ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `repair_user_id` (`repair_user_id`),
  CONSTRAINT `building_ibfk_1` FOREIGN KEY (`repair_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍楼栋表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `building`
--

LOCK TABLES `building` WRITE;
/*!40000 ALTER TABLE `building` DISABLE KEYS */;
INSERT INTO `building` VALUES (1,'1号楼',2,1,'2026-04-25 22:48:36','2026-04-25 22:48:36'),(2,'2号楼',3,1,'2026-04-25 22:48:36','2026-05-06 10:18:10'),(3,'5号楼',9,0,'2026-04-26 20:46:17','2026-05-06 10:20:31');
/*!40000 ALTER TABLE `building` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fault_type`
--

DROP TABLE IF EXISTS `fault_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fault_type` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_name` varchar(50) NOT NULL COMMENT '类型名称',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_name` (`type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='故障类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fault_type`
--

LOCK TABLES `fault_type` WRITE;
/*!40000 ALTER TABLE `fault_type` DISABLE KEYS */;
INSERT INTO `fault_type` VALUES (1,'水电','水管漏水、电路故障等',1,'2026-04-25 22:48:36'),(2,'家具','桌椅损坏、柜子故障等',1,'2026-04-25 22:48:36'),(3,'网络','网络连接问题、路由器故障等',1,'2026-04-25 22:48:36'),(4,'其他','其他类型故障',1,'2026-04-25 22:48:36'),(5,'卫浴设施故障','马桶、洗手池、淋浴花洒的堵塞、漏水等问题',0,'2026-04-27 18:53:34'),(6,'费用','水电费异常',1,'2026-05-06 09:41:49'),(7,'安全','宿舍门坏',0,'2026-05-06 09:48:47');
/*!40000 ALTER TABLE `fault_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` tinyint DEFAULT '1' COMMENT '消息类型：1-系统消息 2-订单消息',
  `sub_type` tinyint DEFAULT NULL COMMENT '子类型',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
  `order_id` bigint DEFAULT NULL COMMENT '关联报修单ID',
  `related_user_id` bigint DEFAULT NULL COMMENT '关联用户ID（发送者）',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0-未读 1-已读',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `notification_ibfk_2` (`related_user_id`),
  KEY `notification_ibfk_3` (`order_id`),
  KEY `idx_user_read_time` (`user_id`,`is_read`,`create_time`),
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `notification_ibfk_2` FOREIGN KEY (`related_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `notification_ibfk_3` FOREIGN KEY (`order_id`) REFERENCES `repair_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (7,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：波比大王',13,NULL,0,'2026-04-27 14:53:16'),(8,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：波比大王',13,NULL,0,'2026-04-27 14:53:16'),(9,5,1,1,'报修单已分配','系统已为您的报修单 BX20260427145315610 分配维修员 [李师傅]',13,NULL,0,'2026-04-27 14:53:16'),(10,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：想玩游戏',14,NULL,1,'2026-04-27 15:07:37'),(11,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：想玩游戏',14,NULL,0,'2026-04-27 15:07:37'),(12,5,1,1,'报修单已分配','系统已为您的报修单 BX20260427150737167 分配维修员 [李师傅]',14,NULL,0,'2026-04-27 15:07:37'),(13,5,1,1,'维修员拒单','维修员 [李师傅] 拒绝了您的报修单 BX20260427150737167，原因：不懂你在说什么',14,NULL,0,'2026-04-27 15:29:36'),(14,1,1,1,'维修员拒单','维修员 [李师傅] 拒绝了您的报修单 BX20260427150737167，原因：不懂你在说什么',14,NULL,0,'2026-04-27 15:29:36'),(15,3,1,1,'报修单已分配','您已被分配新的报修单 BX20260427150737167，请及时处理',14,NULL,0,'2026-04-27 15:33:10'),(16,5,1,1,'报修单已分配','您已被分配新的报修单 BX20260427150737167，请及时处理',14,NULL,0,'2026-04-27 15:33:10'),(17,5,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427150737167',14,NULL,1,'2026-04-27 15:39:21'),(18,1,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427150737167',14,NULL,0,'2026-04-27 15:39:21'),(19,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：头脑风暴中',15,NULL,0,'2026-04-27 15:41:30'),(20,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：头脑风暴中',15,NULL,0,'2026-04-27 15:41:30'),(21,5,1,1,'报修单已分配','系统已为您的报修单 BX20260427154130040 分配维修员 [李师傅]',15,NULL,1,'2026-04-27 15:41:30'),(22,5,1,1,'维修员拒单','维修员 [李师傅] 拒绝了您的报修单 BX20260427154130040，原因：那就慢慢想',15,NULL,1,'2026-04-27 15:42:26'),(23,1,1,1,'维修员拒单','维修员 [李师傅] 拒绝了您的报修单 BX20260427154130040，原因：那就慢慢想',15,NULL,0,'2026-04-27 15:42:26'),(24,3,1,1,'报修单已分配','您已被分配新的报修单 BX20260427154130040，请及时处理',15,NULL,0,'2026-04-27 15:44:01'),(25,5,1,1,'报修单已分配','您已被分配新的报修单 BX20260427154130040，请及时处理',15,NULL,1,'2026-04-27 15:44:01'),(26,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：刚刚弄错了',16,NULL,0,'2026-04-27 15:47:14'),(27,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：刚刚弄错了',16,NULL,0,'2026-04-27 15:47:14'),(28,5,1,1,'报修单已分配','系统已为您的报修单 BX20260427154713862 分配维修员 [李师傅]',16,NULL,1,'2026-04-27 15:47:14'),(29,5,1,1,'维修已完成','您的报修单 BX20260427150737167 已完成维修，结果：想玩游戏就得先做完作业',14,NULL,1,'2026-04-27 15:49:11'),(30,1,1,1,'维修已完成','您的报修单 BX20260427150737167 已完成维修，结果：想玩游戏就得先做完作业',14,NULL,0,'2026-04-27 15:49:11'),(31,3,2,3,'维修时间确认','师傅您好，我今天下午3点后有空，您方便上门维修吗？',NULL,5,0,'2026-04-27 16:45:31'),(32,5,2,3,'维修时间确认','学生你好，我今天没时间，明天下午3点可以么？',NULL,3,1,'2026-04-27 16:47:23'),(33,2,1,1,'新的报修单','学生 [张三] 提交了新的报修单，地址：1号楼，故障：网络好卡卡卡卡卡卡',17,NULL,0,'2026-05-05 17:54:02'),(34,1,1,1,'新的报修单','学生 [张三] 提交了新的报修单，地址：1号楼，故障：网络好卡卡卡卡卡卡',17,NULL,0,'2026-05-05 17:54:02'),(35,4,1,1,'报修单已分配','系统已为您的报修单 BX20260505175402202 分配维修员 [张师傅]',17,NULL,1,'2026-05-05 17:54:02'),(36,3,2,3,'聊天消息','可以，那就明天吧',NULL,5,0,'2026-05-05 18:55:28'),(37,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：。。。。。。',18,NULL,0,'2026-05-05 20:40:52'),(38,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：。。。。。。',18,NULL,0,'2026-05-05 20:40:52'),(39,5,1,1,'报修单已分配','系统已为您的报修单 BX20260505204052451 分配维修员 [李师傅]',18,NULL,1,'2026-05-05 20:40:52'),(40,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：我真的头很大',19,NULL,0,'2026-05-05 20:51:14'),(41,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：我真的头很大',19,NULL,0,'2026-05-05 20:51:14'),(42,5,1,1,'报修单已分配','系统已为您的报修单 BX20260505205114111 分配维修员 [李师傅]',19,NULL,1,'2026-05-05 20:51:14'),(43,2,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260426004437446',8,NULL,0,'2026-05-05 21:09:35'),(44,1,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260426004437446',8,NULL,0,'2026-05-05 21:09:35'),(45,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：停停停停停水了',20,NULL,0,'2026-05-05 22:44:34'),(46,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：停停停停停水了',20,NULL,0,'2026-05-05 22:44:34'),(47,5,1,1,'报修单已分配','系统已为您的报修单 BX20260505224434144 分配维修员 [李师傅]',20,NULL,1,'2026-05-05 22:44:34'),(48,5,1,1,'维修员已接单','维修员 [张师傅] 已接受您的报修单 BX20260426014113631',9,NULL,0,'2026-05-06 00:22:15'),(49,1,1,1,'维修员已接单','维修员 [张师傅] 已接受您的报修单 BX20260426014113631',9,NULL,0,'2026-05-06 00:22:15'),(50,3,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：被充值吱吱吱吱',21,NULL,0,'2026-05-06 00:52:09'),(51,1,1,1,'新的报修单','学生 [李四] 提交了新的报修单，地址：2号楼，故障：被充值吱吱吱吱',21,NULL,0,'2026-05-06 00:52:09'),(52,5,1,1,'报修单已分配','系统已为您的报修单 BX20260506005208526 分配维修员 [李师傅]',21,NULL,0,'2026-05-06 00:52:09'),(53,3,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260506005208526',21,NULL,0,'2026-05-06 00:59:12'),(54,1,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260506005208526',21,NULL,0,'2026-05-06 00:59:12'),(55,3,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260505224434144',20,NULL,0,'2026-05-06 01:12:58'),(56,1,1,1,'报修单已取消','学生 [李四] 取消了报修单 BX20260505224434144',20,NULL,0,'2026-05-06 01:12:58'),(57,5,2,3,'维修时间确认','Ok',NULL,3,0,'2026-05-06 01:42:00'),(58,5,2,3,'维修时间确认','那我准时',NULL,3,0,'2026-05-06 01:47:47'),(59,4,1,1,'维修员拒单','维修员 [张师傅] 拒绝了您的报修单 BX20260505175402202，原因：不懂网络',17,NULL,0,'2026-05-06 02:13:36'),(60,1,1,1,'维修员拒单','维修员 [张师傅] 拒绝了您的报修单 BX20260505175402202，原因：不懂网络',17,NULL,0,'2026-05-06 02:13:36'),(61,5,1,1,'维修员已接单','维修员 [张师傅] 已接受您的报修单 BX20260426014535857',10,NULL,0,'2026-05-06 02:14:03'),(62,1,1,1,'维修员已接单','维修员 [张师傅] 已接受您的报修单 BX20260426014535857',10,NULL,0,'2026-05-06 02:14:03'),(63,5,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260505205114111',19,NULL,0,'2026-05-06 02:14:44'),(64,1,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260505205114111',19,NULL,0,'2026-05-06 02:14:44'),(65,5,1,1,'维修已完成','您的报修单 BX20260505205114111 已完成维修，结果：完美完成任务',19,NULL,0,'2026-05-06 02:15:06'),(66,1,1,1,'维修已完成','您的报修单 BX20260505205114111 已完成维修，结果：完美完成任务',19,NULL,0,'2026-05-06 02:15:06'),(67,5,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427154130040',15,NULL,0,'2026-05-06 02:15:12'),(68,1,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427154130040',15,NULL,0,'2026-05-06 02:15:12'),(69,5,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427145315610',13,NULL,0,'2026-05-06 02:31:26'),(70,1,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260427145315610',13,NULL,0,'2026-05-06 02:31:26'),(71,5,1,1,'维修已完成','您的报修单 BX20260427145315610 已完成维修，结果：还可以',13,NULL,0,'2026-05-06 02:31:48'),(72,1,1,1,'维修已完成','您的报修单 BX20260427145315610 已完成维修，结果：还可以',13,NULL,0,'2026-05-06 02:31:48'),(73,3,2,3,'聊天消息','666',NULL,5,0,'2026-05-06 02:33:44'),(74,5,2,3,'聊天消息','有点东西',NULL,3,0,'2026-05-06 02:34:04'),(75,3,2,3,'聊天消息','哈哈',NULL,5,0,'2026-05-06 02:34:42'),(76,3,2,3,'聊天消息','test1',NULL,5,0,'2026-05-06 02:43:37'),(77,5,2,3,'聊天消息','test2',NULL,3,0,'2026-05-06 02:43:44'),(78,5,2,3,'聊天消息','test3',NULL,3,0,'2026-05-06 02:44:04'),(79,3,2,3,'聊天消息','test4',NULL,5,0,'2026-05-06 02:44:08'),(80,3,2,3,'聊天消息','1',NULL,5,0,'2026-05-06 02:49:12'),(81,5,2,3,'聊天消息','2',NULL,3,0,'2026-05-06 02:49:22'),(82,5,2,3,'聊天消息','3',NULL,3,0,'2026-05-06 02:50:25'),(83,3,2,3,'聊天消息','4',NULL,5,0,'2026-05-06 02:50:42'),(84,5,2,3,'聊天消息','5',NULL,3,0,'2026-05-06 02:56:34'),(85,3,2,3,'聊天消息','6',NULL,5,0,'2026-05-06 02:58:31'),(86,5,2,3,'聊天消息','7',NULL,3,0,'2026-05-06 02:58:55'),(87,3,2,3,'聊天消息','8',NULL,5,0,'2026-05-06 03:01:11'),(88,5,2,3,'聊天消息','9',NULL,3,0,'2026-05-06 03:03:00'),(89,5,2,3,'聊天消息','hh',NULL,3,0,'2026-05-06 03:04:36'),(90,3,2,3,'聊天消息','wll',NULL,5,0,'2026-05-06 03:06:57'),(91,5,2,3,'聊天消息','ay',NULL,3,0,'2026-05-06 03:09:43'),(92,5,2,3,'聊天消息','?',NULL,3,0,'2026-05-06 03:11:25'),(93,5,2,3,'聊天消息','1',NULL,3,0,'2026-05-06 03:13:07'),(94,3,2,3,'聊天消息','2',NULL,5,0,'2026-05-06 03:13:28'),(95,4,2,3,'聊天消息','1',NULL,3,0,'2026-05-06 03:23:12'),(96,3,2,3,'聊天消息','2',NULL,4,0,'2026-05-06 03:23:24'),(97,1,2,3,'聊天消息','qqq',NULL,3,0,'2026-05-06 03:36:29'),(98,5,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260505204052451',18,NULL,0,'2026-05-06 09:27:16'),(99,1,1,1,'维修员已接单','维修员 [李师傅] 已接受您的报修单 BX20260505204052451',18,NULL,0,'2026-05-06 09:27:16'),(100,9,1,1,'报修单已分配','您已被分配新的报修单 BX20260505175402202，请及时处理',17,NULL,0,'2026-05-06 11:42:42'),(101,4,1,1,'报修单已分配','您已被分配新的报修单 BX20260505175402202，请及时处理',17,NULL,0,'2026-05-06 11:42:42'),(102,3,1,1,'报修单已分配','您已被分配新的报修单 20260425001，请及时处理',1,NULL,0,'2026-05-06 12:05:58'),(103,4,1,1,'报修单已分配','您已被分配新的报修单 20260425001，请及时处理',1,NULL,0,'2026-05-06 12:05:58');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repair_order`
--

DROP TABLE IF EXISTS `repair_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `fault_type_id` bigint NOT NULL COMMENT '故障类型ID',
  `description` varchar(500) NOT NULL COMMENT '故障描述',
  `images` varchar(1000) DEFAULT NULL COMMENT '故障图片（阿里云OSS路径）',
  `repair_user_id` bigint DEFAULT NULL COMMENT '维修员ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-已取消 1-待分配 2-待接单 3-处理中 4-已完成 5-已拒绝',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '拒单理由',
  `repair_result` varchar(500) DEFAULT NULL COMMENT '维修结果',
  `repair_images` varchar(1000) DEFAULT NULL COMMENT '维修图片（阿里云OSS路径）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `accept_time` datetime DEFAULT NULL COMMENT '接单时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `repair_user_id` (`repair_user_id`),
  KEY `repair_order_ibfk_1` (`student_id`),
  KEY `repair_order_ibfk_2` (`building_id`),
  KEY `repair_order_ibfk_3` (`room_id`),
  KEY `repair_order_ibfk_4` (`fault_type_id`),
  KEY `idx_status_time` (`status`,`create_time`),
  CONSTRAINT `repair_order_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `repair_order_ibfk_2` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `repair_order_ibfk_3` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `repair_order_ibfk_4` FOREIGN KEY (`fault_type_id`) REFERENCES `fault_type` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `repair_order_ibfk_5` FOREIGN KEY (`repair_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报修单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repair_order`
--

LOCK TABLES `repair_order` WRITE;
/*!40000 ALTER TABLE `repair_order` DISABLE KEYS */;
INSERT INTO `repair_order` VALUES (1,'20260425001',4,1,1,1,'卫生间水龙头漏水',NULL,3,2,NULL,NULL,NULL,'2026-04-25 22:48:36',NULL,NULL,'2026-05-06 12:05:57'),(2,'20260425002',5,2,3,2,'书桌抽屉损坏',NULL,NULL,1,NULL,NULL,NULL,'2026-04-25 22:48:36',NULL,NULL,'2026-04-25 22:48:36'),(6,'BX20260425235532690',5,2,3,4,'写代码好难啊啊啊','',3,0,NULL,NULL,NULL,'2026-04-25 23:55:33',NULL,NULL,'2026-04-25 23:55:32'),(7,'BX20260426004242134',5,2,3,4,'玛卡巴卡','',3,4,NULL,'写不出来 就把思路给AI，让AI帮忙','','2026-04-26 00:42:42','2026-04-26 00:43:01','2026-04-26 00:56:21','2026-04-26 00:42:42'),(8,'BX20260426004437446',5,2,3,4,'我不知道哪里有问题','',2,0,NULL,NULL,NULL,'2026-04-26 00:44:37',NULL,NULL,'2026-04-26 01:56:29'),(9,'BX20260426014113631',5,2,3,4,'还有谁','',2,3,'不懂',NULL,NULL,'2026-04-26 01:41:14','2026-05-06 00:22:15',NULL,'2026-04-26 01:41:13'),(10,'BX20260426014535857',5,2,3,4,'雄霸天下','',2,3,NULL,NULL,NULL,'2026-04-26 01:45:36','2026-05-06 02:14:03',NULL,'2026-04-26 01:52:46'),(13,'BX20260427145315610',5,2,3,4,'波比大王','https://suki-lunwen.oss-cn-beijing.aliyuncs.com/6f41d41a0b504ab7b83e26c0231e7d08.jpg',3,4,NULL,'还可以',NULL,'2026-04-27 14:53:16','2026-05-06 02:31:26','2026-05-06 02:31:48','2026-05-05 22:35:08'),(14,'BX20260427150737167',5,2,3,4,'想玩游戏','',3,4,NULL,'想玩游戏就得先做完作业','','2026-04-27 15:07:37','2026-04-27 15:39:21','2026-04-27 15:49:11','2026-04-27 15:33:09'),(15,'BX20260427154130040',5,2,3,4,'头脑风暴中','',3,3,NULL,NULL,NULL,'2026-04-27 15:41:30','2026-05-06 02:15:12',NULL,'2026-04-27 15:44:01'),(16,'BX20260427154713862',5,2,3,4,'刚刚弄错了','',3,2,NULL,NULL,NULL,'2026-04-27 15:47:14',NULL,NULL,'2026-04-27 15:47:13'),(17,'BX20260505175402202',4,1,1,3,'网络好卡卡卡卡卡卡','[\"https://repair-system.oss-cn-beijing.aliyuncs.com/a12c5d03-c254-4ace-a200-d0ccde883d32.png\"]',9,2,NULL,NULL,NULL,'2026-05-05 17:54:02',NULL,NULL,'2026-05-06 11:42:41'),(18,'BX20260505204052451',5,2,3,4,'。。。。。。','',3,3,NULL,NULL,NULL,'2026-05-05 20:40:52','2026-05-06 09:27:16',NULL,'2026-05-05 20:40:52'),(19,'BX20260505205114111',5,2,3,4,'我真的头很大','',3,4,NULL,'完美完成任务',NULL,'2026-05-05 20:51:14','2026-05-06 02:14:44','2026-05-06 02:15:06','2026-05-06 02:32:46'),(20,'BX20260505224434144',5,2,3,1,'停停停停停水了','',3,0,NULL,NULL,NULL,'2026-05-05 22:44:34',NULL,NULL,'2026-05-05 22:44:34'),(21,'BX20260506005208526',5,2,3,2,'被充值吱吱哈哈哈','',3,0,NULL,NULL,NULL,'2026-05-06 00:52:09',NULL,NULL,'2026-05-06 00:55:19');
/*!40000 ALTER TABLE `repair_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `repair_record`
--

DROP TABLE IF EXISTS `repair_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repair_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '报修单ID',
  `repair_user_id` bigint NOT NULL COMMENT '维修员ID',
  `action` tinyint NOT NULL COMMENT '操作类型：1-分配 2-接受 3-拒绝 4-完成',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注/说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `repair_record_ibfk_2` (`repair_user_id`),
  KEY `idx_order_time` (`order_id`,`create_time`),
  CONSTRAINT `repair_record_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `repair_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `repair_record_ibfk_2` FOREIGN KEY (`repair_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='维修记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repair_record`
--

LOCK TABLES `repair_record` WRITE;
/*!40000 ALTER TABLE `repair_record` DISABLE KEYS */;
INSERT INTO `repair_record` VALUES (4,6,3,1,'系统自动分配','2026-04-25 23:55:33'),(5,7,3,1,'系统自动分配','2026-04-26 00:42:42'),(6,7,3,2,'维修员接受任务','2026-04-26 00:43:01'),(7,8,3,1,'系统自动分配','2026-04-26 00:44:37'),(8,8,3,3,'维修员拒绝任务: 该故障不属于我的维修范围','2026-04-26 00:50:05'),(9,7,3,4,'维修完成: 写不出来 就把思路给AI，让AI帮忙','2026-04-26 00:56:21'),(10,8,2,1,'管理员手动分配','2026-04-26 01:27:22'),(11,8,2,1,'管理员手动分配','2026-04-26 01:30:00'),(12,8,3,1,'管理员手动分配','2026-04-26 01:30:23'),(13,8,2,1,'重新分配','2026-04-26 01:30:53'),(14,8,2,1,'重新分配','2026-04-26 01:32:28'),(15,8,2,1,'重新分配','2026-04-26 01:36:01'),(16,9,3,1,'系统自动分配','2026-04-26 01:41:14'),(17,9,3,3,'维修失败: 不懂','2026-04-26 01:42:13'),(18,9,2,1,'重新分配','2026-04-26 01:42:59'),(19,8,2,1,'重新分配','2026-04-26 01:44:42'),(20,10,3,1,'系统自动分配','2026-04-26 01:45:36'),(21,10,3,3,'维修失败: aikun','2026-04-26 01:46:22'),(22,10,2,1,'重新分配','2026-04-26 01:46:40'),(23,10,2,1,'重新分配','2026-04-26 01:49:49'),(24,10,2,1,'管理员手动分配','2026-04-26 01:50:10'),(25,10,2,1,'管理员手动分配','2026-04-26 01:52:47'),(26,8,2,1,'管理员手动分配','2026-04-26 01:56:13'),(27,8,2,1,'重新分配','2026-04-26 01:56:30'),(30,13,3,1,'系统自动分配','2026-04-27 14:53:16'),(31,14,3,1,'系统自动分配','2026-04-27 15:07:37'),(32,14,3,3,'维修失败: 不懂你在说什么','2026-04-27 15:29:36'),(33,14,3,1,'重新分配','2026-04-27 15:33:10'),(34,14,3,2,'维修员接受任务','2026-04-27 15:39:21'),(35,15,3,1,'系统自动分配','2026-04-27 15:41:30'),(36,15,3,3,'维修失败: 那就慢慢想','2026-04-27 15:42:26'),(37,15,3,1,'重新分配','2026-04-27 15:44:01'),(38,16,3,1,'系统自动分配','2026-04-27 15:47:14'),(39,14,3,4,'维修完成: 想玩游戏就得先做完作业','2026-04-27 15:49:11'),(40,17,2,1,'系统自动分配','2026-05-05 17:54:02'),(41,18,3,1,'系统自动分配','2026-05-05 20:40:53'),(42,19,3,1,'系统自动分配','2026-05-05 20:51:14'),(43,20,3,1,'系统自动分配','2026-05-05 22:44:34'),(44,9,2,2,'维修员接受任务','2026-05-06 00:22:15'),(45,21,3,1,'系统自动分配','2026-05-06 00:52:09'),(46,17,2,3,'维修失败: 不懂网络','2026-05-06 02:13:36'),(47,10,2,2,'维修员接受任务','2026-05-06 02:14:03'),(48,19,3,2,'维修员接受任务','2026-05-06 02:14:44'),(49,19,3,4,'维修完成: 完美完成任务','2026-05-06 02:15:06'),(50,15,3,2,'维修员接受任务','2026-05-06 02:15:12'),(51,13,3,2,'维修员接受任务','2026-05-06 02:31:26'),(52,13,3,4,'维修完成: 还可以','2026-05-06 02:31:48'),(53,18,3,2,'维修员接受任务','2026-05-06 09:27:16'),(54,17,9,1,'重新分配','2026-05-06 11:42:42'),(55,1,3,1,'管理员手动分配','2026-05-06 12:05:58');
/*!40000 ALTER TABLE `repair_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `room_number` varchar(20) NOT NULL COMMENT '房间号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_room` (`building_id`,`room_number`),
  CONSTRAINT `room_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍房间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (1,1,'101',1,'2026-04-25 22:48:36'),(2,1,'102',1,'2026-04-25 22:48:36'),(3,3,'502',1,'2026-04-25 22:48:36'),(5,3,'501',1,'2026-04-26 20:57:39'),(7,2,'502',1,'2026-05-06 10:33:54'),(8,3,'101',1,'2026-05-06 10:34:13');
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_dormitory`
--

DROP TABLE IF EXISTS `student_dormitory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_dormitory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `building_id` bigint NOT NULL COMMENT '楼栋ID',
  `room_id` bigint NOT NULL COMMENT '房间ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-已退宿 1-已入住',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入住时间',
  `end_time` datetime DEFAULT NULL COMMENT '退宿时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_status` (`student_id`,`status`),
  KEY `idx_building_status` (`building_id`,`status`),
  KEY `idx_room_status` (`room_id`,`status`),
  CONSTRAINT `student_dormitory_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `student_dormitory_ibfk_2` FOREIGN KEY (`building_id`) REFERENCES `building` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `student_dormitory_ibfk_3` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生宿舍分配表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_dormitory`
--

LOCK TABLES `student_dormitory` WRITE;
/*!40000 ALTER TABLE `student_dormitory` DISABLE KEYS */;
INSERT INTO `student_dormitory` VALUES (1,4,1,1,1,'2026-04-25 22:48:36',NULL),(2,5,2,3,1,'2026-04-25 22:48:36',NULL),(3,7,2,3,0,'2026-04-26 15:28:48','2026-04-26 16:02:57'),(4,7,1,1,1,'2026-04-26 16:02:57',NULL),(5,8,2,3,0,'2026-04-26 16:04:52','2026-04-26 16:05:58'),(6,8,2,3,0,'2026-05-04 17:00:42','2026-05-06 10:41:37'),(7,8,2,3,0,'2026-05-06 10:41:37','2026-05-06 11:01:43'),(8,8,2,7,0,'2026-05-06 11:01:49','2026-05-06 11:02:19'),(9,8,1,1,1,'2026-05-06 11:02:27',NULL),(10,18,1,2,1,'2026-05-06 11:02:42',NULL);
/*!40000 ALTER TABLE `student_dormitory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名（学号/工号）',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `role` tinyint NOT NULL COMMENT '角色：1-学生 2-维修员 3-管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `class_name` varchar(50) DEFAULT NULL COMMENT '班级',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','123456','管理员','13800138000',3,1,'2026-04-25 22:48:36','2026-04-25 22:48:36',NULL),(2,'wx001','123456','张师傅','13800138001',2,1,'2026-04-25 22:48:36','2026-04-25 22:48:36',NULL),(3,'wx002','123456','李师傅','13800138002',2,1,'2026-04-25 22:48:36','2026-04-25 22:48:36',NULL),(4,'20210001','123456','张三','13800138010',1,1,'2026-04-25 22:48:36','2026-05-04 17:46:06','2201'),(5,'20210002','123456','李四','13800138011',1,1,'2026-04-25 22:48:36','2026-05-04 17:46:06','2202'),(6,'wx003','123456','王小二师傅','13800138001',2,1,'2026-04-26 14:29:35','2026-04-26 20:38:54',NULL),(7,'20210003','123456','王五大王','13800138001',1,1,'2026-04-26 15:06:15','2026-05-04 17:46:06','2203'),(8,'20210004','123456','老六','13800138013',1,1,'2026-04-26 16:04:10','2026-05-04 17:46:06','2204'),(9,'wx004','123456','曾师傅','13800138003',2,1,'2026-05-03 19:37:11','2026-05-04 17:46:06',''),(10,'wx005','123456','帅师傅','13800138003',2,1,'2026-05-03 19:39:23','2026-05-03 19:39:22',NULL),(11,'2026001','123456','L','13800138001',1,1,'2026-05-04 19:06:34','2026-05-04 19:06:34','2201'),(12,'2026002','123456','M','13800138001',1,1,'2026-05-04 19:06:34','2026-05-04 19:06:34','2202'),(13,'2026003','123456','N','13800138001',1,1,'2026-05-04 19:06:34','2026-05-04 19:06:34','2203'),(14,'wx007','123456','大帅师傅','13800138007',2,1,'2026-05-06 09:23:44','2026-05-06 09:24:24',''),(15,'2026006','123456','Q','13800138001',1,1,'2026-05-06 09:25:44','2026-05-06 09:32:54','2203'),(16,'2026007','123456','W','13800138001',1,1,'2026-05-06 09:25:44','2026-05-06 09:25:43','2202'),(17,'2026008','123456','E','13800138001',1,1,'2026-05-06 09:25:44','2026-05-06 09:25:43','2203'),(18,'2027001','123456','R','13899103002',1,1,'2026-05-06 10:58:47','2026-05-06 10:58:46','2204'),(19,'2027002','123456','F','13879902200',1,1,'2026-05-06 13:56:23','2026-05-06 13:56:22','2701');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `uploaded_file`
--

DROP TABLE IF EXISTS `uploaded_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `uploaded_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名（OSS存储的文件名）',
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL',
  `used` tinyint DEFAULT '0' COMMENT '是否被使用（0-未使用，1-已使用）',
  `repair_order_id` bigint DEFAULT NULL COMMENT '关联的报修单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_used` (`used`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_repair_order_id` (`repair_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上传文件记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `uploaded_file`
--

LOCK TABLES `uploaded_file` WRITE;
/*!40000 ALTER TABLE `uploaded_file` DISABLE KEYS */;
INSERT INTO `uploaded_file` VALUES (2,'377d9c6f930e47a6a9630c3ab9a20c00.jpg','q.jpg',181322,'https://suki-lunwen.oss-cn-beijing.aliyuncs.com/377d9c6f930e47a6a9630c3ab9a20c00.jpg',0,NULL,'2026-05-05 22:29:24','2026-05-05 22:29:24'),(3,'b636c12626f84b939ee61dbdf1be348e.jpg','q.jpg',181322,'https://suki-lunwen.oss-cn-beijing.aliyuncs.com/b636c12626f84b939ee61dbdf1be348e.jpg',0,NULL,'2026-05-05 22:30:28','2026-05-05 22:30:28'),(4,'6f41d41a0b504ab7b83e26c0231e7d08.jpg','q.jpg',181322,'https://suki-lunwen.oss-cn-beijing.aliyuncs.com/6f41d41a0b504ab7b83e26c0231e7d08.jpg',1,13,'2026-05-05 22:35:05','2026-05-05 22:35:08');
/*!40000 ALTER TABLE `uploaded_file` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-06 17:34:40
