package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudentCourseAssessPO;

import java.time.LocalDate;
import java.util.List;

public interface StudentCourseAssessService extends IService<StudentCourseAssessPO> {
    /**
     * 查询课程
     * @param courseId 课程id
     * @param studentId 学生id
     * @return 课程
     */
    List<StudentCourseAssessPO> queryCourseAssess(Integer courseId, int studentId);

    /**
     * 查询课程节点
     * @param courseNodeId 课程节点id
     * @param studentId 学生id
     * @return 课程节点
     */
    StudentCourseAssessPO findCourseNodeAssess(Integer courseNodeId, int studentId);

    /**
     * 查询学生课程考核结果
     * @param studentIds 学生id
     * @return 考核结果
     */
    List<StudentCourseAssessPO> queryByStudentIds(List<Integer> studentIds);

    /**
     * 查询学生在指定日期范围内的考核记录
     * @param studentId 学生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考核记录列表
     */
    List<StudentCourseAssessPO> findByStudentIdAndDateRange(Integer studentId, LocalDate startDate, LocalDate endDate);
}
