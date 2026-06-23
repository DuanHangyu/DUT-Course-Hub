package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseStudentRelationMapper;
import com.human.digital.digitalhuman.repository.po.CourseStudentRelationPO;
import com.human.digital.digitalhuman.repository.service.CourseStudentRelationService;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 19:56 2025/9/11
 */
@Repository
public class CourseStudentRelationServiceImpl extends ServiceImpl<CourseStudentRelationMapper, CourseStudentRelationPO> implements CourseStudentRelationService {
    @Override
    public List<CourseStudentRelationPO> queryByStudentIds(List<Integer> studentIds) {
        return this.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .in(CourseStudentRelationPO::getStudentId, studentIds));
    }

    @Override
    public List<CourseStudentRelationPO> queryByStudentId(Integer studentId) {
        return this.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getStudentId, studentId));
    }

    @Override
    public List<CourseStudentRelationPO> queryByCourseIds(List<Integer> courseIds) {
        return this.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .in(CourseStudentRelationPO::getCourseId, courseIds));
    }

    @Override
    public void deleteByCourseId(Integer courseId) {
        this.remove(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId));
    }
}
