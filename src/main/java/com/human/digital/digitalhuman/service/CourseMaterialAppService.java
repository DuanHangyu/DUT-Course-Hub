package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFilePO;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFolderPO;
import com.human.digital.digitalhuman.repository.service.CourseMaterialFileService;
import com.human.digital.digitalhuman.repository.service.CourseMaterialFolderService;
import com.human.digital.digitalhuman.repository.service.CourseService;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileMoveCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileRenameCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFileUploadCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFolderCreateCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialFolderRenameCmd;
import com.human.digital.digitalhuman.service.model.request.CourseMaterialSortCmd;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialFileDTO;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialFolderDTO;
import com.human.digital.digitalhuman.service.model.response.CourseMaterialListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程资料库应用服务
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Service
@RequiredArgsConstructor
public class CourseMaterialAppService {

    private static final int MAX_FILE_COUNT = 50;

    private final CourseMaterialFolderService folderService;
    private final CourseMaterialFileService fileService;
    private final CourseService courseService;
    private final OssClientUtils ossClientUtils;

    // ==================== 文件夹操作 ====================

    /**
     * 创建文件夹
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createFolder(CourseMaterialFolderCreateCmd cmd) {
        // 校验课程是否存在
        validateCourseExist(cmd.getCourseId());
        // 校验文件夹名称是否重复
        if (folderService.existsByCourseIdAndFolderName(cmd.getCourseId(), cmd.getFolderName())) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FOLDER_NAME_DUPLICATE);
        }
        // 计算排序值
        Integer maxSort = folderService.queryMaxSortOrder(cmd.getCourseId());
        int sortOrder = maxSort != null ? maxSort + 1 : 0;

        CourseMaterialFolderPO po = new CourseMaterialFolderPO();
        po.setCourseId(cmd.getCourseId());
        po.setFolderName(cmd.getFolderName());
        po.setSortOrder(sortOrder);
        po.setCreatorId(StpUtil.getLoginIdAsInt());
        return folderService.save(po);
    }

    /**
     * 重命名文件夹
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean renameFolder(CourseMaterialFolderRenameCmd cmd) {
        CourseMaterialFolderPO folder = validateFolderBelongToCourse(cmd.getId(), cmd.getCourseId());
        // 未改名直接返回
        if (folder.getFolderName().equals(cmd.getFolderName())) {
            return true;
        }
        // 校验新名称是否与其他文件夹重复
        if (folderService.existsByCourseIdAndFolderName(cmd.getCourseId(), cmd.getFolderName())) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FOLDER_NAME_DUPLICATE);
        }
        return folderService.update(Wrappers.lambdaUpdate(CourseMaterialFolderPO.class)
                .set(CourseMaterialFolderPO::getFolderName, cmd.getFolderName())
                .eq(CourseMaterialFolderPO::getId, cmd.getId()));
    }

    /**
     * 删除文件夹（连同文件）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFolder(Integer folderId, Integer courseId) {
        validateFolderBelongToCourse(folderId, courseId);
        // 逻辑删除文件夹
        folderService.removeById(folderId);
        // 逻辑删除文件夹下的文件
        fileService.deleteByFolderId(folderId);
        return true;
    }

    /**
     * 文件夹排序
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean sortFolders(CourseMaterialSortCmd cmd) {
        for (CourseMaterialSortCmd.SortItem item : cmd.getSortItems()) {
            folderService.update(Wrappers.lambdaUpdate(CourseMaterialFolderPO.class)
                    .set(CourseMaterialFolderPO::getSortOrder, item.getSortOrder())
                    .eq(CourseMaterialFolderPO::getId, item.getId())
                    .eq(CourseMaterialFolderPO::getCourseId, cmd.getCourseId()));
        }
        return true;
    }

    // ==================== 文件操作 ====================

    /**
     * 上传文件（创建文件记录）
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadFile(CourseMaterialFileUploadCmd cmd) {
        // 校验课程是否存在
        validateCourseExist(cmd.getCourseId());
        // 校验文件数量上限
        long currentCount = fileService.countByCourseId(cmd.getCourseId());
        if (currentCount >= MAX_FILE_COUNT) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FILE_LIMIT_EXCEEDED);
        }
        // 校验文件夹归属
        if (cmd.getFolderId() != null) {
            validateFolderBelongToCourse(cmd.getFolderId(), cmd.getCourseId());
        }
        // 计算排序值
        Integer maxSort = fileService.queryMaxSortOrder(cmd.getCourseId());
        int sortOrder = maxSort != null ? maxSort + 1 : 0;

        CourseMaterialFilePO po = new CourseMaterialFilePO();
        po.setCourseId(cmd.getCourseId());
        po.setFolderId(cmd.getFolderId());
        po.setFileName(cmd.getFileName());
        po.setOssKey(cmd.getOssKey());
        po.setFileSize(cmd.getFileSize());
        po.setFileType(cmd.getFileType());
        po.setSortOrder(sortOrder);
        po.setCreatorId(StpUtil.getLoginIdAsInt());
        return fileService.save(po);
    }

    /**
     * 重命名文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean renameFile(CourseMaterialFileRenameCmd cmd) {
        validateFileBelongToCourse(cmd.getId(), cmd.getCourseId());
        return fileService.update(Wrappers.lambdaUpdate(CourseMaterialFilePO.class)
                .set(CourseMaterialFilePO::getFileName, cmd.getFileName())
                .eq(CourseMaterialFilePO::getId, cmd.getId()));
    }

    /**
     * 移动文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveFile(CourseMaterialFileMoveCmd cmd) {
        validateFileBelongToCourse(cmd.getId(), cmd.getCourseId());
        // 校验目标文件夹归属
        if (cmd.getFolderId() != null) {
            validateFolderBelongToCourse(cmd.getFolderId(), cmd.getCourseId());
        }
        return fileService.update(Wrappers.lambdaUpdate(CourseMaterialFilePO.class)
                .set(CourseMaterialFilePO::getFolderId, cmd.getFolderId())
                .eq(CourseMaterialFilePO::getId, cmd.getId()));
    }

    /**
     * 删除文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFile(Integer fileId, Integer courseId) {
        validateFileBelongToCourse(fileId, courseId);
        return fileService.removeById(fileId);
    }

    /**
     * 文件排序
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean sortFiles(CourseMaterialSortCmd cmd) {
        for (CourseMaterialSortCmd.SortItem item : cmd.getSortItems()) {
            fileService.update(Wrappers.lambdaUpdate(CourseMaterialFilePO.class)
                    .set(CourseMaterialFilePO::getSortOrder, item.getSortOrder())
                    .eq(CourseMaterialFilePO::getId, item.getId())
                    .eq(CourseMaterialFilePO::getCourseId, cmd.getCourseId()));
        }
        return true;
    }

    // ==================== 查询操作 ====================

    /**
     * 获取课程资料列表（含文件夹和根目录文件）
     */
    @Transactional(readOnly = true)
    public CourseMaterialListDTO getMaterialList(Integer courseId) {
        validateCourseExist(courseId);
        // 查询文件夹
        List<CourseMaterialFolderPO> folders = folderService.queryByCourseId(courseId);
        // 查询根目录文件
        List<CourseMaterialFilePO> rootFiles = fileService.queryRootFilesByCourseId(courseId);

        // 统计每个文件夹的文件数量
        Map<Integer, Long> folderFileCountMap;
        if (CollectionUtil.isNotEmpty(folders)) {
            List<Integer> folderIds = folders.stream().map(CourseMaterialFolderPO::getId).toList();
            folderFileCountMap = fileService.countGroupByFolderIds(folderIds);
        } else {
            folderFileCountMap = new HashMap<>();
        }

        // 转换文件夹 DTO
        List<CourseMaterialFolderDTO> folderDTOs = folders.stream()
                .map(f -> CourseMaterialFolderDTO.builder()
                        .id(f.getId())
                        .folderName(f.getFolderName())
                        .fileCount(folderFileCountMap.getOrDefault(f.getId(), 0L))
                        .sortOrder(f.getSortOrder())
                        .build())
                .toList();

        // 转换文件 DTO（生成签名 URL）
        List<CourseMaterialFileDTO> fileDTOs = rootFiles.stream()
                .map(f -> CourseMaterialFileDTO.builder()
                        .id(f.getId())
                        .fileName(f.getFileName())
                        .ossUrl(ossClientUtils.getSignedUrl(f.getOssKey()))
                        .fileSize(f.getFileSize())
                        .fileType(f.getFileType())
                        .sortOrder(f.getSortOrder())
                        .createTime(f.getCreateTime())
                        .build())
                .toList();

        return CourseMaterialListDTO.builder()
                .folders(folderDTOs)
                .files(fileDTOs)
                .build();
    }

    /**
     * 获取文件夹内文件列表
     */
    @Transactional(readOnly = true)
    public List<CourseMaterialFileDTO> getFolderFiles(Integer folderId, Integer courseId) {
        validateFolderBelongToCourse(folderId, courseId);
        List<CourseMaterialFilePO> files = fileService.queryByFolderId(folderId);
        return files.stream()
                .map(f -> CourseMaterialFileDTO.builder()
                        .id(f.getId())
                        .fileName(f.getFileName())
                        .ossUrl(ossClientUtils.getSignedUrl(f.getOssKey()))
                        .fileSize(f.getFileSize())
                        .fileType(f.getFileType())
                        .sortOrder(f.getSortOrder())
                        .createTime(f.getCreateTime())
                        .build())
                .toList();
    }

    /**
     * 获取文件签名下载 URL
     */
    public String getFileDownloadUrl(Integer fileId, Integer courseId) {
        validateCourseExist(courseId);
        validateFileBelongToCourse(fileId, courseId);
        CourseMaterialFilePO file = fileService.getById(fileId);
        return ossClientUtils.getSignedUrl(file.getOssKey());
    }

    // ==================== 私有校验方法 ====================

    private void validateCourseExist(Integer courseId) {
        if (courseService.getById(courseId) == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
    }

    private CourseMaterialFolderPO validateFolderBelongToCourse(Integer folderId, Integer courseId) {
        CourseMaterialFolderPO folder = folderService.getById(folderId);
        if (folder == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FOLDER_NOT_EXIST);
        }
        if (!folder.getCourseId().equals(courseId)) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FOLDER_NOT_BELONG_COURSE);
        }
        return folder;
    }

    private CourseMaterialFilePO validateFileBelongToCourse(Integer fileId, Integer courseId) {
        CourseMaterialFilePO file = fileService.getById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FILE_NOT_EXIST);
        }
        if (!file.getCourseId().equals(courseId)) {
            throw new BusinessException(ErrorCodeEnums.COURSE_MATERIAL_FILE_NOT_EXIST);
        }
        return file;
    }
}
