package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessDetailPO;

import java.util.List;

public interface StudentCourseAssessDetailService extends IService<StudentCourseAssessDetailPO> {
    /**
     * 根据课程节点ID和学生ID查询考核详情
     *
     * @param courseNodeId 课程节点ID
     * @param studentId    学生ID
     * @return 考核详情列表
     */
    List<StudentCourseAssessDetailPO> queryCourseNodeAssess(Integer courseId, Integer courseNodeId, int studentId);

    /**
     * 根据课程ID和课程节点ID和学生ID查询考核中的考核详情
     *
     * @param courseId     课程ID
     * @param courseNodeId 课程节点ID
     * @param studentId    学生ID
     * @return 考核详情列表
     */
    List<StudentCourseAssessDetailPO> queryCourseNodeAssessing(Integer courseId, Integer courseNodeId, Integer studentId);

    /**
     * 根据课程ID和学生ID查询考核中的考核详情
     *
     * @param courseId     课程ID
     * @param studentId    学生ID
     * @return 考核详情列表
     */
    List<StudentCourseAssessDetailPO> queryCourseAssessing(Integer courseId, Integer studentId);

    /**
     *  查询正在考核和已考核的课程详情
     * @param studentId 学生ID
     * @param courseNodeId 课程节点id
     * @return 考核记录列表
     */
    List<StudentCourseAssessDetailPO> queryAssessingAndAssessed(Integer studentId, Integer courseNodeId);
}
