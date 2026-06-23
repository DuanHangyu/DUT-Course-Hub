package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.StudentCourseNodeStudyRecordPO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentCourseNodeStudyRecordService extends IService<StudentCourseNodeStudyRecordPO> {
    /**
     * 查询最后一次学习记录
     * @param studentId 学生id
     * @param courseNodeId 课程节id
     * @return 最近一次该课程节点的学习记录
     */
    Optional<StudentCourseNodeStudyRecordPO> findLastRecord(Integer studentId, Integer courseNodeId);

    /**
     * 查询该课程节下的所有学习记录
     * @param courseNodeId 课程节id
     * @return 该课程节下的所有学习记录
     */
    List<StudentCourseNodeStudyRecordPO> findByCourseNodeId(Integer studentId, Integer courseNodeId);

    /**
     * 查询学生在指定日期范围内的学习记录
     * @param studentId 学生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习记录列表
     */
    List<StudentCourseNodeStudyRecordPO> findByStudentIdAndDateRange(Integer studentId, LocalDate startDate, LocalDate endDate);

    /**
     * 批量查询多个学生在指定日期范围内的学习记录
     *
     * @param studentIds 学生ID列表
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 学习记录列表（按学生ID分组）
     */
    List<StudentCourseNodeStudyRecordPO> findByStudentIdsAndDateRange(List<Integer> studentIds, LocalDate startDate, LocalDate endDate);

    /**
     * 判断学生是否有指定课程节点的完成记录
     * 同一学生同一节点有多条学习记录时，只要其中一条completed为true即为完成
     *
     * @param studentId    学生ID
     * @param courseNodeId 课程节点ID
     * @return 是否有完成记录
     */
    boolean hasCompletedRecord(Integer studentId, Integer courseNodeId);

    /**
     * 根据课程ID和学生ID查询学习记录
     *
     * @param courseId        课程ID
     * @param studentId 学生ID
     * @return 学习记录列表
     */
    List<StudentCourseNodeStudyRecordPO> findByCourseIdAndStudentId(Integer courseId, Integer studentId);

    /**
     * 批量查询指定节点下已完成学生的ID列表
     *
     * @param courseNodeId 课程节点ID
     * @param studentIds   学生ID列表
     * @return 已完成学生的ID列表
     */
    List<Integer> listCompletedStudentIds(Integer courseNodeId, List<Integer> studentIds);

    /**
     * 批量查询多个学生的最新学习记录
     *
     * @param studentIds 学生ID列表
     * @return 学习记录列表（每个学生取最新的一条，按学生ID分组）
     */
    List<StudentCourseNodeStudyRecordPO> findLastRecordsByStudentIds(List<Integer> studentIds);
}
