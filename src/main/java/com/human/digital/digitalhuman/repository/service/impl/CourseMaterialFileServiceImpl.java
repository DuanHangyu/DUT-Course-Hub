package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseMaterialFileMapper;
import com.human.digital.digitalhuman.repository.po.CourseMaterialFilePO;
import com.human.digital.digitalhuman.repository.service.CourseMaterialFileService;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程资料文件 Service 实现
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Repository
public class CourseMaterialFileServiceImpl extends ServiceImpl<CourseMaterialFileMapper, CourseMaterialFilePO>
        implements CourseMaterialFileService {

    @Override
    public List<CourseMaterialFilePO> queryRootFilesByCourseId(Integer courseId) {
        return this.list(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .eq(CourseMaterialFilePO::getCourseId, courseId)
                .isNull(CourseMaterialFilePO::getFolderId)
                .orderByAsc(CourseMaterialFilePO::getSortOrder));
    }

    @Override
    public List<CourseMaterialFilePO> queryByFolderId(Integer folderId) {
        return this.list(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .eq(CourseMaterialFilePO::getFolderId, folderId)
                .orderByAsc(CourseMaterialFilePO::getSortOrder));
    }

    @Override
    public long countByCourseId(Integer courseId) {
        return this.count(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .eq(CourseMaterialFilePO::getCourseId, courseId));
    }

    @Override
    public void deleteByFolderId(Integer folderId) {
        this.remove(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .eq(CourseMaterialFilePO::getFolderId, folderId));
    }

    @Override
    public long countByFolderId(Integer folderId) {
        return this.count(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .eq(CourseMaterialFilePO::getFolderId, folderId));
    }

    @Override
    public Integer queryMaxSortOrder(Integer courseId) {
        CourseMaterialFilePO po = this.getOne(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                .select(CourseMaterialFilePO::getSortOrder)
                .eq(CourseMaterialFilePO::getCourseId, courseId)
                .orderByDesc(CourseMaterialFilePO::getSortOrder)
                .last("LIMIT 1"));
        return po != null ? po.getSortOrder() : null;
    }

    @Override
    public Map<Integer, Long> countGroupByFolderIds(List<Integer> folderIds) {
        if (CollectionUtil.isEmpty(folderIds)) {
            return Collections.emptyMap();
        }
        return this.list(Wrappers.lambdaQuery(CourseMaterialFilePO.class)
                        .select(CourseMaterialFilePO::getFolderId)
                        .in(CourseMaterialFilePO::getFolderId, folderIds))
                .stream()
                .filter(f -> f.getFolderId() != null)
                .collect(Collectors.groupingBy(CourseMaterialFilePO::getFolderId, Collectors.counting()));
    }
}
