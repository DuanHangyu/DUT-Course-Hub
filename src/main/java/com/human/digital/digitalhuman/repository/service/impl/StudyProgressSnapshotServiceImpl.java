package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudyProgressSnapshotMapper;
import com.human.digital.digitalhuman.repository.po.StudyProgressSnapshotPO;
import com.human.digital.digitalhuman.repository.service.StudyProgressSnapshotService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习进度快照服务实现类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Service
public class StudyProgressSnapshotServiceImpl extends ServiceImpl<StudyProgressSnapshotMapper, StudyProgressSnapshotPO>
        implements StudyProgressSnapshotService {

    @Override
    public void saveBatch(List<StudyProgressSnapshotPO> snapshotList) {
        // 原代码 this.saveBatch 是无限递归；改为调父类的批量保存
        snapshotList.forEach(this::save);
    }

    @Override
    public List<StudyProgressSnapshotPO> listByCourseIdAndDate(Long courseId, LocalDate snapshotDate) {
        return this.list(Wrappers.lambdaQuery(StudyProgressSnapshotPO.class)
                .eq(StudyProgressSnapshotPO::getCourseId, courseId)
                .eq(StudyProgressSnapshotPO::getSnapshotDate, snapshotDate));
    }

    @Override
    public StudyProgressSnapshotPO getLatestByCourseAndStudent(Long courseId, Long studentId) {
        return this.getOne(Wrappers.lambdaQuery(StudyProgressSnapshotPO.class)
                .eq(StudyProgressSnapshotPO::getCourseId, courseId)
                .eq(StudyProgressSnapshotPO::getStudentId, studentId)
                .orderByDesc(StudyProgressSnapshotPO::getSnapshotDate)
                .last("LIMIT 1"));
    }

    @Override
    public List<StudyProgressSnapshotPO> listLatestByCourseAndStudentIds(Long courseId, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }
        // 查询所有快照，按快照日期降序排列，然后按学生ID分组，每组取第一条
        List<StudyProgressSnapshotPO> allSnapshots = this.list(Wrappers.lambdaQuery(StudyProgressSnapshotPO.class)
                .eq(StudyProgressSnapshotPO::getCourseId, courseId)
                .in(StudyProgressSnapshotPO::getStudentId, studentIds)
                .orderByDesc(StudyProgressSnapshotPO::getSnapshotDate));

        // 按学生ID分组，每组取第一条（最新日期的快照）
        return allSnapshots.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudyProgressSnapshotPO::getStudentId))
                .values()
                .stream()
                .map(list -> list.getFirst())
                .toList();
    }
}
