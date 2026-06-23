package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFolderPO;

import java.util.List;

/**
 * 课程资料文件夹 Service
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
public interface CourseMaterialFolderService extends IService<CourseMaterialFolderPO> {

    /**
     * 查询课程下所有文件夹
     *
     * @param courseId 课程ID
     * @return 文件夹列表
     */
    List<CourseMaterialFolderPO> queryByCourseId(Integer courseId);

    /**
     * 查询课程下文件夹名是否重复
     *
     * @param courseId   课程ID
     * @param folderName 文件夹名称
     * @return 是否存在
     */
    boolean existsByCourseIdAndFolderName(Integer courseId, String folderName);

    /**
     * 查询课程下文件夹的最大排序值
     *
     * @param courseId 课程ID
     * @return 最大排序值，无记录时返回 null
     */
    Integer queryMaxSortOrder(Integer courseId);
}
