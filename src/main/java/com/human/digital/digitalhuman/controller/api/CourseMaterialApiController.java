package com.human.digital.digitalhuman.controller.api;

import com.human.digital.digitalhuman.service.CourseMaterialAppService;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialFileDTO;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialListDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程资料学生端接口
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@RestController
@RequestMapping("/api/course-material")
@RequiredArgsConstructor
@Tag(name = "课程资料查询", description = "课程资料学生端查询")
public class CourseMaterialApiController {

    private final CourseMaterialAppService courseMaterialAppService;

    @GetMapping("/list")
    @Operation(summary = "获取课程资料列表")
    public CourseMaterialListDTO getMaterialList(
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.getMaterialList(courseId);
    }

    @GetMapping("/file/{fileId}/download")
    @Operation(summary = "获取文件签名下载URL")
    @Deprecated
    public String getFileDownloadUrl(
            @PathVariable("fileId") @Parameter(description = "文件ID") Integer fileId,
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.getFileDownloadUrl(fileId, courseId);
    }

    @GetMapping("/folder/{folderId}/files")
    @Operation(summary = "获取文件夹内文件列表")
    public List<CourseMaterialFileDTO> getFolderFiles(
            @PathVariable("folderId") @Parameter(description = "文件夹ID") Integer folderId,
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.getFolderFiles(folderId, courseId);
    }
}
