package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFilePO;

import java.util.List;
import java.util.Map;

/**
 * 课程资料文件 Service
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
public interface CourseMaterialFileService extends IService<CourseMaterialFilePO> {

    /**
     * 查询课程根目录下的文件
     *
     * @param courseId 课程ID
     * @return 根目录文件列表
     */
    List<CourseMaterialFilePO> queryRootFilesByCourseId(Integer courseId);

    /**
     * 查询文件夹下的文件
     *
     * @param folderId 文件夹ID
     * @return 文件列表
     */
    List<CourseMaterialFilePO> queryByFolderId(Integer folderId);

    /**
     * 统计课程下文件总数
     *
     * @param courseId 课程ID
     * @return 文件数量
     */
    long countByCourseId(Integer courseId);

    /**
     * 逻辑删除文件夹下的所有文件
     *
     * @param folderId 文件夹ID
     */
    void deleteByFolderId(Integer folderId);

    /**
     * 统计文件夹下文件数量
     *
     * @param folderId 文件夹ID
     * @return 文件数量
     */
    long countByFolderId(Integer folderId);

    /**
     * 查询课程下文件的最大排序值
     *
     * @param courseId 课程ID
     * @return 最大排序值，无记录时返回 null
     */
    Integer queryMaxSortOrder(Integer courseId);

    /**
     * 按文件夹ID分组统计文件数量
     *
     * @param folderIds 文件夹ID列表
     * @return 文件夹ID -> 文件数量
     */
    Map<Integer, Long> countGroupByFolderIds(List<Integer> folderIds);
}
