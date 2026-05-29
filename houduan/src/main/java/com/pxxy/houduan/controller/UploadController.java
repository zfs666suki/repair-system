package com.pxxy.houduan.controller;

import com.aliyun.oss.OSS;
import com.pxxy.houduan.common.Result;
import com.pxxy.houduan.config.OSSConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器
 * 提供图片上传到阿里云OSS的功能
 */
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "图片上传相关接口")
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private final OSS ossClient;
    private final OSSConfig ossConfig;

    public UploadController(OSS ossClient, OSSConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    /**
     * 上传图片到阿里云OSS
     * 生成唯一文件名并上传，返回图片访问URL
     *
     * @param file 上传的图片文件
     * @return 上传成功返回图片访问URL，失败返回错误信息
     */
    @Operation(summary = "上传图片", description = "上传图片到阿里云OSS，返回图片访问URL")
    @PostMapping("/image")
    public Result<?> uploadImage(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("文件上传失败: 文件为空");
            return Result.error("文件不能为空");
        }

        try {
            // 生成唯一文件名防止冲突
            //file.getOriginalFilename() 是 MultipartFile 接口的方法，用于获取上传文件的原始文件名（包含扩展名）
            String originalFilename = file.getOriginalFilename();
            //lastIndexOf(".") 是字符串方法，用于查找最后一个点号（.）在文件名中的位置索引。
            //substring() 是字符串截取方法，从指定索引位置开始提取子字符串。
            //例如，"example.jpg" 的 lastIndexOf(".") 返回 7，substring(7) 返回 ".jpg"
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            //UUID.randomUUID() 是 Java 生成唯一标识符的方法，返回一个 UUID 对象
            //toString() 是 Object 类的方法，用于将对象转换为字符串
            //UUID 对象本身：内部由两个 long 值（128位）组成；调用.toString()后：才转换为人类可读的字符串格式字符串格式（36位）
            //550e8400-e29b-41d4-a716-446655440000 32个十六进制字符 和 4个连字符 -
            //550e8400-e29b-41d4-a716-446655440000.jpg
            String fileName = UUID.randomUUID().toString() + suffix;

            logger.info("上传图片: originalFilename={}, size={}KB", originalFilename, file.getSize() / 1024);

            // 上传文件到阿里云OSS
            ossClient.putObject(ossConfig.getBucketName(), fileName, file.getInputStream());

            // 构建并返回图片访问URL
            String url = ossConfig.getUrlPrefix() + fileName;
            logger.info("上传成功: url={}", url);

            return Result.success(url);
        } catch (IOException e) {
            logger.error("上传失败", e);
            return Result.error("上传失败");
        }
    }

    /**
     * 从阿里云OSS删除图片
     *
     * @param fileName 要删除的图片文件名
     * @return 操作结果提示
     */
    @Operation(summary = "删除图片", description = "从阿里云OSS删除指定图片")
    @DeleteMapping("/image")
    public Result<?> deleteImage(@RequestParam("fileName") String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
        logger.warn("删除图片失败: 文件名为空");
        return Result.error("文件名不能为空");
    }

    try {
        String bucketName = ossConfig.getBucketName();
        
        // 先检查文件是否存在
        if (!ossClient.doesObjectExist(bucketName, fileName)) {
            logger.warn("删除图片失败: 文件不存在, fileName={}", fileName);
            return Result.error(404, "文件不存在");
        }
        
        // 文件存在，执行删除
        logger.info("删除图片: fileName={}", fileName);
        ossClient.deleteObject(bucketName, fileName);
        logger.info("删除成功: fileName={}", fileName);
        return Result.success("删除成功");
        
    } catch (Exception e) {
        logger.error("删除失败, fileName={}", fileName, e);
        return Result.error("删除失败");
    }
}
/*  @Tag - 接口分组标签
    作用：将API接口按功能分组
    位置：类级别
    属性：
    name：标签名称（必填），用于API分组显示
    description：标签的详细描述
    externalDocs：外部文档链接*/
/*  @Operation - 接口操作说明
    作用：描述单个API接口的功能
    位置：方法级别
    属性：
    summary：接口简要说明
    description：接口详细描述
    operationId：操作的唯一标识
    tags：指定该接口所属的标签组
    responses：响应码说明*/
/*  @Parameter - 参数说明
    作用：描述API请求参数
    位置：方法参数前
    属性：
    description：参数描述
    required：是否必填（true/false）
    example：示例值
    name：参数名称
    in：参数位置（query、path、header、cookie）
    schema：参数类型定义
    examples：多个示例值
    allowEmptyValue：是否允许空值
    deprecated：是否已废弃*/
}
