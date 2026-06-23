package com.human.digital.digitalhuman.controller;

import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author taoHouChao
 * @Date 23:24 2025/5/17
 */
@RestController
@RequestMapping("/file")
@Tag(name = "文件上传接口", description = "文件上传接口")
public class FileController {

    @Resource
    private OssClientUtils ossClientUtils;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public UploadDTO uploadFile(MultipartFile file) {
        String fileUrl = ossClientUtils.uploadFile(file);
        String signedUrl = ossClientUtils.getSignedUrl(fileUrl);
        return new UploadDTO(signedUrl, fileUrl, file.getOriginalFilename());
    }

    @GetMapping("/signed-url")
    @Operation(summary = "获取文件签名URL")
    public String getSignedUrl(@RequestParam("url") String url){
        return ossClientUtils.getSignedUrl(url);
    }

    @GetMapping("/generate-upload-url")
    @Operation(summary = "生成上传URL")
    public UploadDTO generateUploadUrl(@RequestParam("fileName") String fileName,
                                       @RequestParam("contentType") String contentType) {
        String fileUrl = ossClientUtils.getFileName(fileName);
        String signedUrl = ossClientUtils.getSignedPutUrl(fileUrl, contentType);
        return new UploadDTO(signedUrl, fileUrl, fileName);
    }
}
