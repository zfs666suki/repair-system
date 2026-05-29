package com.pxxy.houduan.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 从配置文件读取aliyun.oss相关属性，并创建OSS客户端Bean
 */
@Data
@Configuration
//从配置文件中读取aliyun.oss.*属性
@ConfigurationProperties(prefix = "aliyun.oss")
public class OSSConfig {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;

    /**
     * 创建阿里云OSS客户端实例
     * @return OSS客户端对象，用于执行文件上传、下载等操作
     */
    @Bean
    public OSS ossClient() {
        //OSSClientBuilder 是OSS SDK的客户端构建器
        //build() 方法传入三个参数：服务端点、访问密钥ID、访问密钥秘钥
        //返回配置好的 OSS 对象，用于执行文件上传、下载等操作
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /*ossClient 是阿里云 OSS 客户端对象，提供文件存储操作：
      核心方法：
        ossClient.putObject(bucketName, fileName, inputStream); - 上传文件
        ossClient.getObject(bucketName, fileName); - 下载文件
        ossClient.deleteObject(bucketName, fileName); - 删除文件
        ossClient.listObjects(bucketName); - 列出文件
        ossClient.doesObjectExist(bucketName, fileName); - 判断文件是否存在
        ossClient.shutdown(); - 关闭客户端
    */

}
