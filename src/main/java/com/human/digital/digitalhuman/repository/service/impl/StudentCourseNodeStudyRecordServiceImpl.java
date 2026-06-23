package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseNodeMapper;
import com.human.digital.digitalhuman.repository.mapper.StudentCourseNodeStudyRecordMapper;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.po.StudentCourseNodeStudyRecordPO;
import com.human.digital.digitalhuman.repository.service.StudentCourseNodeStudyRecordService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @USER taoHouChao
 * @DATE 16:42 2025/9/12
 */
@Repository
@RequiredArgsConstructor
public class StudentCourseNodeStudyRecordServiceImpl extends ServiceImpl<StudentCourseNodeStudyRecordMapper, StudentCourseNodeStudyRecordPO> implements StudentCourseNodeStudyRecordService {

    private final CourseNodeMapper courseNodeMapper;

    @Override
    public Optional<StudentCourseNodeStudyRecordPO> findLastRecord(Integer studentId, Integer courseNodeId) {
        return Optional.ofNullable(this.getOne(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                .eq(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeId)
                .orderByDesc(StudentCourseNodeStudyRecordPO::getId)
                .last("limit 1")));
    }

    @Override
    public List<StudentCourseNodeStudyRecordPO> findByCourseNodeId(Integer studentId, Integer courseNodeId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                .eq(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeId));
    }

    @Override
    public List<StudentCourseNodeStudyRecordPO> findByStudentIdAndDateRange(Integer studentId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                .ge(StudentCourseNodeStudyRecordPO::getStudyStartTime, startDateTime)
                .le(StudentCourseNodeStudyRecordPO::getStudyStartTime, endDateTime));
    }

    @Override
    public List<StudentCourseNodeStudyRecordPO> findByStudentIdsAndDateRange(List<Integer> studentIds, LocalDate startDate, LocalDate endDate) {
        if (studentIds == null || studentIds.isEmpty()) {
            return List.of();
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                .ge(StudentCourseNodeStudyRecordPO::getStudyStartTime, startDateTime)
                .le(StudentCourseNodeStudyRecordPO::getStudyStartTime, endDateTime));
    }

    @Override
    public boolean hasCompletedRecord(Integer studentId, Integer courseNodeId) {
        List<StudentCourseNodeStudyRecordPO> records = this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                .eq(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseNodeStudyRecordPO::getCompleted, true));
        return !records.isEmpty();
    }

    @Override
    public List<StudentCourseNodeStudyRecordPO> findByCourseIdAndStudentId(Integer courseId, Integer studentId) {
        List<CourseNodePO> courseNodes = courseNodeMapper.selectList(Wrappers.lambdaQuery(CourseNodePO.class)
                .eq(CourseNodePO::getCourseId, courseId));
        if (CollectionUtils.isEmpty(courseNodes)) {
            return Collections.emptyList();
        }
        List<Integer> courseNodeIds = courseNodes.stream().map(CourseNodePO::getId).toList();
        return this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .in(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeIds));
    }

    @Override
    public List<Integer> listCompletedStudentIds(Integer courseNodeId, List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .eq(StudentCourseNodeStudyRecordPO::getCourseNodeId, courseNodeId)
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                        .eq(StudentCourseNodeStudyRecordPO::getCompleted, true))
                .stream()
                .map(StudentCourseNodeStudyRecordPO::getStudentId)
                .distinct()
                .toList();
    }

    @Override
    public List<StudentCourseNodeStudyRecordPO> findLastRecordsByStudentIds(List<Integer> studentIds) {
        if (CollectionUtils.isEmpty(studentIds)) {
            return Collections.emptyList();
        }
        // 查询所有学生的学习记录，按学生ID分组，每组取ID最大的记录
        List<StudentCourseNodeStudyRecordPO> allRecords = this.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds)
                .orderByDesc(StudentCourseNodeStudyRecordPO::getId));

        // 按学生ID分组，每组取第一条（即ID最大的）
        return allRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId))
                .values()
                .stream()
                .map(list -> list.getFirst())
                .toList();
    }
}
