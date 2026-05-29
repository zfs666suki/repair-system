package com.pxxy.houduan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置类
 * 配置OpenAPI规范，定义API文档的元数据信息
 */
@Configuration
public class Knife4jConfig {

    /**
     * 自定义OpenAPI配置
     * @return OpenAPI对象，包含API标题、版本、描述、联系方式和许可证信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园宿舍报修系统API")
                        .version("1.0")
                        .description("校园宿舍报修系统是一个专为高校宿舍管理设计的报修平台，\n" +
                                "支持学生提交报修申请、维修人员处理维修任务、管理员进行系统管理。\n" +
                                "系统主要功能包括：\n" +
                                "- 学生：提交报修申请、上传故障图片、查看维修状态\n" +
                                "- 维修人员：接收维修任务、更新维修状态、记录维修过程\n" +
                                "- 管理员：用户管理、宿舍楼管理、宿舍管理、维修任务分配\n" +
                                "系统采用Spring Boot 3.4.4 + Vue 3 + Element Plus技术栈，\n" +
                                "支持JWT认证、WebSocket实时通知、阿里云OSS图片存储等功能。")
                        //联系方式信息
                        .contact(new Contact()
                                .name("suki-系统管理员")
                                .email("admin@repair-system.com"))
                        //许可证信息
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
/*
OpenAPI 对象来自 Swagger Core 库，是 OpenAPI 3.0 规范的核心类。
  常用方法：
    info(Info info)：设置 API 基本信息（标题、版本、描述）
    paths(Paths paths)：定义 API 路径和操作
    components(Components components)：配置可重用的组件（如安全方案、Schema）
    servers(List<Server> servers)：设置 API 服务器地址
    tags(List<Tag> tags)：定义操作标签，用于分组
    externalDocs(ExternalDocumentation externalDocs)：添加外部文档链接
    实际使用：在此代码中，info() 方法配置了 API 的元数据信息，用于在 Knife4j/Swagger UI 界面展示
*/
/*
Info 对象来自 Swagger Core 库，用于描述 API 的元数据信息。
   常用方法：
    `title(String title)`：设置 API 标题
    `version(String version)`：设置 API 版本号
    `description(String description)`：设置 API 详细描述
    `termsOfService(String termsOfService)`：设置服务条款
    `contact(Contact contact)`：设置联系人信息（名称、邮箱、URL）
    `license(License license)`：设置许可证信息（名称、URL）
    `summary(String summary)`：设置简要摘要
   实际使用：在此代码中，通过链式调用配置了 API 的标题、版本、描述、联系方式和许可证，这些信息会显示在 Knife4j/Swagger UI 文档首页
*/
}
