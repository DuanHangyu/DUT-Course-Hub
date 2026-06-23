package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseMaterialFolderMapper;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFolderPO;
import com.human.digital.digitalhuman.repository.service.CourseMaterialFolderService;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程资料文件夹 Service 实现
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Repository
public class CourseMaterialFolderServiceImpl extends ServiceImpl<CourseMaterialFolderMapper, CourseMaterialFolderPO>
        implements CourseMaterialFolderService {

    @Override
    public List<CourseMaterialFolderPO> queryByCourseId(Integer courseId) {
        return this.list(Wrappers.lambdaQuery(CourseMaterialFolderPO.class)
                .eq(CourseMaterialFolderPO::getCourseId, courseId)
                .orderByAsc(CourseMaterialFolderPO::getSortOrder));
    }

    @Override
    public boolean existsByCourseIdAndFolderName(Integer courseId, String folderName) {
        return this.count(Wrappers.lambdaQuery(CourseMaterialFolderPO.class)
                .eq(CourseMaterialFolderPO::getCourseId, courseId)
                .eq(CourseMaterialFolderPO::getFolderName, folderName)) > 0;
    }

    @Override
    public Integer queryMaxSortOrder(Integer courseId) {
        CourseMaterialFolderPO po = this.getOne(Wrappers.lambdaQuery(CourseMaterialFolderPO.class)
                .select(CourseMaterialFolderPO::getSortOrder)
                .eq(CourseMaterialFolderPO::getCourseId, courseId)
                .orderByDesc(CourseMaterialFolderPO::getSortOrder)
                .last("LIMIT 1"));
        return po != null ? po.getSortOrder() : null;
    }
}
