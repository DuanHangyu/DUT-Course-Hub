package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudentCourseAssessMapper;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessPO;
import com.human.digital.digitalhuman.repository.service.StudentCourseAssessService;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 10:29 2025/6/15
 */
@Repository
public class StudentCourseAssessServiceImpl extends ServiceImpl<StudentCourseAssessMapper, StudentCourseAssessPO> implements StudentCourseAssessService {
    @Override
    public List<StudentCourseAssessPO> queryCourseAssess(Integer courseId, int studentId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                .eq(StudentCourseAssessPO::getCourseId, courseId)
                .eq(StudentCourseAssessPO::getStudentId, studentId));
    }

    @Override
    public StudentCourseAssessPO findCourseNodeAssess(Integer courseNodeId, int studentId) {
        return this.getOne(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                .eq(StudentCourseAssessPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessPO::getStudentId, studentId));
    }

    @Override
    public List<StudentCourseAssessPO> queryByStudentIds(List<Integer> studentIds) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                .in(StudentCourseAssessPO::getStudentId, studentIds));
    }

    @Override
    public List<StudentCourseAssessPO> findByStudentIdAndDateRange(Integer studentId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return this.list(Wrappers.lambdaQuery(StudentCourseAssessPO.class)
                .eq(StudentCourseAssessPO::getStudentId, studentId)
                .ge(StudentCourseAssessPO::getCreateTime, startDateTime)
                .le(StudentCourseAssessPO::getCreateTime, endDateTime));
    }
}
