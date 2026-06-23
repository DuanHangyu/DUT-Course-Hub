package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.StudentCourseAssessDetailMapper;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessDetailPO;
import com.human.digital.digitalhuman.repository.service.StudentCourseAssessDetailService;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 10:32 2025/6/15
 */
@Repository
public class StudentCourseAssessDetailServiceImpl extends ServiceImpl<StudentCourseAssessDetailMapper, StudentCourseAssessDetailPO> implements StudentCourseAssessDetailService {
    @Override
    public List<StudentCourseAssessDetailPO> queryCourseNodeAssess(Integer courseId,
                                                                   Integer courseNodeId,
                                                                   int studentId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, 1)
                .orderByAsc(StudentCourseAssessDetailPO::getId));
    }

    @Override
    public List<StudentCourseAssessDetailPO> queryCourseNodeAssessing(Integer courseId, Integer courseNodeId, Integer studentId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, -1)
                .orderByAsc(StudentCourseAssessDetailPO::getId));
    }

    @Override
    public List<StudentCourseAssessDetailPO> queryCourseAssessing(Integer courseId, Integer studentId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getCourseId, courseId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, -1)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getState, -1)
                .orderByAsc(StudentCourseAssessDetailPO::getId));
    }

    @Override
    public List<StudentCourseAssessDetailPO> queryAssessingAndAssessed(Integer studentId, Integer courseNodeId) {
        return this.list(Wrappers.lambdaQuery(StudentCourseAssessDetailPO.class)
                .eq(StudentCourseAssessDetailPO::getStudentId, studentId)
                .eq(StudentCourseAssessDetailPO::getCourseNodeId, courseNodeId)
                .in(StudentCourseAssessDetailPO::getState, -1, 1));
    }
}
