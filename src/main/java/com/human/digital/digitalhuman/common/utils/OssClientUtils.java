package com.human.digital.digitalhuman.common.utils;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * @Author taoHouChao
 * @Date 23:22 2025/5/17
 */
@Component
@Slf4j
public class OssClientUtils {

    @Resource
    private OSS ossClient;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public String uploadFile(File file){
        //获取原生文件名
        String originalFilename = file.getName();
        String uploadFileName = getFileName(originalFilename);
        // 设置成在线预览
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentDisposition("inline");
        try {
            byte[] data = FileUtils.readFileToByteArray(file);
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, uploadFileName, new ByteArrayInputStream(data), objectMetadata);
            if (putObjectResult != null) {
                String url = "https://" + bucketName + "." + endpoint + "/" + uploadFileName;
                log.info("upload url:[{}]", url);
                return url;
            }
        } catch (IOException e) {
            log.error("上传文件失败,e", e);
        }
        return null;
    }

    public String uploadFile(MultipartFile file) {
        //获取原生文件名
        String originalFilename = file.getOriginalFilename();
        String uploadFileName = getFileName(originalFilename);
        // 设置成在线预览
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentDisposition("inline");
        try {
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, uploadFileName, file.getInputStream(), objectMetadata);
            if (putObjectResult != null) {
                String url = "https://" + bucketName + "." + endpoint + "/" + uploadFileName;
                log.info("upload url:[{}]", url);
                return uploadFileName;
            }
        } catch (IOException e) {
            log.error("上传文件失败,e", e);
        }
        return null;
    }

    @NotNull
    public String getFileName(String originalFilename) {
        DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime time = LocalDateTime.now();
        //拼装OSS上存储的路径
        String folder = dft.format(time);
        String fileName = generateUUID();
        // 扩展名
        String extension = Optional.ofNullable(originalFilename).map(item -> item.substring(item.lastIndexOf("."))).orElse("");

        //在OSS上bucket下的文件名
        return "digital/" + folder + "/" + fileName + extension;
    }

    private String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

    @Cacheable(cacheNames = "picture", key = "#url")
    public String getSignedUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        }
        // 设置URL过期时间（单位：毫秒，建议30分钟）现在设置为6小时
        Date expiration = new Date(System.currentTimeMillis() + 6 * 60 * 60 * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, url, HttpMethod.GET);
        request.setExpiration(expiration);
        String httpUrl = ossClient.generatePresignedUrl(request).toString();
        return httpUrl.replace("http","https");
    }

    public String getSignedPutUrl(String url, String contentType){
        // 设置URL过期时间（单位：毫秒，建议30分钟）现在设置为6小时
        Date expiration = new Date(System.currentTimeMillis() + 6 * 60 * 60 * 1000);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, url, HttpMethod.PUT);
        request.setContentType(contentType);
        request.setExpiration(expiration);
        String httpUrl = ossClient.generatePresignedUrl(request).toString();
        return httpUrl.replace("http","https");
    }
}
