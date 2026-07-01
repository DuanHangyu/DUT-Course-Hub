package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.CourseMaterialAppService;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileMoveCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileRenameCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileUploadCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFolderCreateCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFolderRenameCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialSortCmd;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialFileDTO;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialListDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程资料后台管理
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@RestController
@RequestMapping("/course-material")
@Tag(name = "课程资料管理", description = "课程资料后台管理")
@Validated
@RequiredArgsConstructor
public class CourseMaterialBackendController {

    private final CourseMaterialAppService courseMaterialAppService;

    @GetMapping("/list")
    @Operation(summary = "获取课程资料列表")
    public CourseMaterialListDTO getMaterialList(
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.getMaterialList(courseId);
    }

    @GetMapping("/folder/{folderId}/files")
    @Operation(summary = "获取文件夹内文件列表")
    public List<CourseMaterialFileDTO> getFolderFiles(
            @PathVariable("folderId") @Parameter(description = "文件夹ID") Integer folderId,
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.getFolderFiles(folderId, courseId);
    }

    @PostMapping("/folder/create")
    @Operation(summary = "创建文件夹")
    public Boolean createFolder(@RequestBody @Validated CourseMaterialFolderCreateCmd cmd) {
        return courseMaterialAppService.createFolder(cmd);
    }

    @PutMapping("/folder/rename")
    @Operation(summary = "重命名文件夹")
    public Boolean renameFolder(@RequestBody @Validated CourseMaterialFolderRenameCmd cmd) {
        return courseMaterialAppService.renameFolder(cmd);
    }

    @DeleteMapping("/folder/{folderId}")
    @Operation(summary = "删除文件夹")
    public Boolean deleteFolder(
            @PathVariable("folderId") @Parameter(description = "文件夹ID") Integer folderId,
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.deleteFolder(folderId, courseId);
    }

    @PutMapping("/folder/sort")
    @Operation(summary = "文件夹排序")
    public Boolean sortFolders(@RequestBody @Validated CourseMaterialSortCmd cmd) {
        return courseMaterialAppService.sortFolders(cmd);
    }

    @PostMapping("/file/upload")
    @Operation(summary = "上传文件到课程")
    public Boolean uploadFile(@RequestBody @Validated CourseMaterialFileUploadCmd cmd) {
        return courseMaterialAppService.uploadFile(cmd);
    }

    @PutMapping("/file/rename")
    @Operation(summary = "重命名文件")
    public Boolean renameFile(@RequestBody @Validated CourseMaterialFileRenameCmd cmd) {
        return courseMaterialAppService.renameFile(cmd);
    }

    @PutMapping("/file/move")
    @Operation(summary = "移动文件")
    public Boolean moveFile(@RequestBody @Validated CourseMaterialFileMoveCmd cmd) {
        return courseMaterialAppService.moveFile(cmd);
    }

    @DeleteMapping("/file/{fileId}")
    @Operation(summary = "删除文件")
    public Boolean deleteFile(
            @PathVariable("fileId") @Parameter(description = "文件ID") Integer fileId,
            @RequestParam("courseId") @Parameter(description = "课程ID") Integer courseId) {
        return courseMaterialAppService.deleteFile(fileId, courseId);
    }

    @PutMapping("/file/sort")
    @Operation(summary = "文件排序")
    public Boolean sortFiles(@RequestBody @Validated CourseMaterialSortCmd cmd) {
        return courseMaterialAppService.sortFiles(cmd);
    }
}
