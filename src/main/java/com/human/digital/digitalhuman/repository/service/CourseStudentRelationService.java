package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.CourseStudentRelationPO;

import java.util.List;

public interface CourseStudentRelationService extends IService<CourseStudentRelationPO> {
    /**
     * 根据学生id查询课程关系
     * @param studentIds 学生id
     * @return 课程关系
     */
    List<CourseStudentRelationPO> queryByStudentIds(List<Integer> studentIds);

    /**
     * 根据学生id查询课程关系
     * @param studentId 学生id
     * @return 课程关系
     */
    List<CourseStudentRelationPO> queryByStudentId(Integer studentId);

    /**
     * 根据课程id查询课程关系
     * @param courseIds 课程id
     * @return 课程关系
     */
    List<CourseStudentRelationPO> queryByCourseIds(List<Integer> courseIds);

    /**
     * 根据课程id删除课程关系
     * @param courseId 课程id
     */
    void deleteByCourseId(Integer courseId);
}
