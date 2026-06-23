package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseInviteMapper;
import com.human.digital.digitalhuman.repository.mapper.CourseMapper;
import com.human.digital.digitalhuman.repository.mapper.CourseStudentRelationMapper;
import com.human.digital.digitalhuman.repository.mapper.UserMapper;
import com.human.digital.digitalhuman.repository.po.CourseInvitePO;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.repository.po.CourseStudentRelationPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.CourseService;
import com.human.digital.digitalhuman.repository.service.CourseStudentRelationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author taoHouChao
 * @Date 19:28 2025/6/11
 */
@Repository
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, CoursePO> implements CourseService {

    private final CourseInviteMapper courseInviteMapper;

    private final UserMapper userMapper;

    private final CourseStudentRelationMapper courseStudentRelationMapper;

    private final CourseStudentRelationService courseStudentRelationService;

    @Override
    public IPage<CoursePO> pageQuery(Integer teacherId, Integer page, Integer size, String courseName, Boolean state) {
        UserPO userPO = userMapper.selectById(teacherId);
        // 老师
        if (Objects.equals(userPO.getRole(), 1)) {
            List<Integer> courseIds = courseInviteMapper.selectList(Wrappers.lambdaQuery(CourseInvitePO.class)
                            .eq(CourseInvitePO::getInviteeTeacherId, teacherId))
                    .stream()
                    .map(CourseInvitePO::getCourseId)
                    .distinct()
                    .toList();
            return this.page(
                    new Page<>(page, size),
                    Wrappers.lambdaQuery(CoursePO.class)
                            .and(q -> q.eq(CoursePO::getCreatorId, teacherId)
                                    .like(StrUtil.isNotBlank(courseName), CoursePO::getCourseName, courseName)
                                    .eq(state != null, CoursePO::getState, state)
                            )
                            .or(CollectionUtil.isNotEmpty(courseIds), q -> q.in(CoursePO::getId, courseIds))
                            .orderByDesc(CoursePO::getId)
            );
        } else {
            return this.page(
                    new Page<>(page, size),
                    Wrappers.lambdaQuery(CoursePO.class)
                            .like(StrUtil.isNotBlank(courseName), CoursePO::getCourseName, courseName)
                            .eq(state != null, CoursePO::getState, state)
            );
        }
    }

    @Override
    public List<CoursePO> enableList() {
        return this.list(Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getState, true)
                .orderByDesc(CoursePO::getId));
    }

    @Override
    public List<CoursePO> enableListBySchoolId(Integer schoolId, String subject) {
        return this.list(Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getState, true)
                .eq(schoolId != null, CoursePO::getSchoolId, schoolId)
                .eq(StringUtils.isNotBlank(subject), CoursePO::getSubject, subject)
                .orderByDesc(CoursePO::getId));
    }

    @Override
    public Map<Integer, CoursePO> queryByIds(List<Integer> courseIds) {
        if (CollectionUtil.isEmpty(courseIds)) {
            return Collections.emptyMap();
        }
        return this.list(Wrappers.lambdaQuery(CoursePO.class)
                        .in(CoursePO::getId, courseIds))
                .stream()
                .collect(Collectors.toMap(CoursePO::getId, coursePO -> coursePO));
    }

    @Override
    public List<Integer> queryIdByName(String courseName) {
        if (StrUtil.isBlank(courseName)) {
            return null;
        }
        return this.list(Wrappers.lambdaQuery(CoursePO.class)
                        .select(CoursePO::getId)
                        .like(CoursePO::getCourseName, courseName))
                .stream()
                .map(CoursePO::getId)
                .toList();
    }

    @Override
    public IPage<CoursePO> pageQueryTemplate(Integer page, Integer size, String courseName, Boolean state, String subject) {
        return this.page(
                new Page<>(page, size),
                Wrappers.lambdaQuery(CoursePO.class)
                        .eq(CoursePO::getCourseType, 1)
                        .like(StrUtil.isNotBlank(courseName), CoursePO::getCourseName, courseName)
                        .eq(state != null, CoursePO::getState, state)
                        .like(StrUtil.isNotBlank(subject), CoursePO::getSubject, subject)
                        .orderByDesc(CoursePO::getId)
        );
    }

    @Override
    public List<CoursePO> queryAll() {
        return this.list(Wrappers.lambdaQuery(CoursePO.class)
                .orderByAsc(CoursePO::getId));
    }

    /**
     * 校本课程分页查询
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param schoolId 学校ID
     * @param name     课程名称
     * @param state    课程状态
     * @param subject  所属学科
     * @return 分页结果
     */
    @Override
    public IPage<CoursePO> pageQuerySchool(int pageNum, int pageSize, Integer schoolId, String name, Boolean state, String subject) {
        Page<CoursePO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getCourseType, 2) // 校本课程
                .eq(schoolId != null, CoursePO::getSchoolId, schoolId)
                .like(StrUtil.isNotBlank(name), CoursePO::getCourseName, name)
                .eq(state != null, CoursePO::getState, state)
                .like(StrUtil.isNotBlank(subject), CoursePO::getSubject, subject)
                .orderByDesc(CoursePO::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 校本课程分页查询（我的课程：创建人或关联老师）
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param schoolId  学校ID
     * @param name      课程名称
     * @param state     课程状态
     * @param subject   所属学科
     * @param creatorId 创建人ID
     * @return 分页结果
     */
    @Override
    public IPage<CoursePO> pageQuerySchoolMyCourse(int pageNum, int pageSize, Integer schoolId, String name, Boolean state, String subject, Integer creatorId) {
        Page<CoursePO> page = new Page<>(pageNum, pageSize);

        // 查询该用户关联的课程ID列表
        List<Integer> relatedCourseIds = courseInviteMapper.selectList(Wrappers.lambdaQuery(CourseInvitePO.class)
                        .eq(CourseInvitePO::getInviteeTeacherId, creatorId))
                .stream()
                .map(CourseInvitePO::getCourseId)
                .distinct()
                .toList();

        LambdaQueryWrapper<CoursePO> wrapper = Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getCourseType, 2) // 校本课程
                .eq(schoolId != null, CoursePO::getSchoolId, schoolId)
                .like(StrUtil.isNotBlank(name), CoursePO::getCourseName, name)
                .eq(state != null, CoursePO::getState, state)
                .like(StrUtil.isNotBlank(subject), CoursePO::getSubject, subject)
                .and(w -> w.eq(CoursePO::getCreatorId, creatorId)
                        .or(CollectionUtil.isNotEmpty(relatedCourseIds), o -> o.in(CoursePO::getId, relatedCourseIds))
                )
                .orderByDesc(CoursePO::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 根据邀请码查询课程
     *
     * @param inviteCode 邀请码
     * @return 课程信息
     */
    @Override
    public CoursePO getByInviteCode(String inviteCode) {
        if (StrUtil.isBlank(inviteCode)) {
            return null;
        }
        return getOne(Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getInviteCode, inviteCode));
    }

    /**
     * 获取课程学生关联
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 关联关系
     */
    @Override
    public CourseStudentRelationPO getCourseStudentRelation(Integer courseId, Integer studentId) {
        if (courseId == null || studentId == null) {
            return null;
        }
        return courseStudentRelationMapper.selectOne(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId)
                .eq(CourseStudentRelationPO::getStudentId, studentId));
    }

    /**
     * 批量保存课程学生关联
     *
     * @param relations 关联列表
     */
    @Override
    public void saveBatchCourseStudentRelation(List<CourseStudentRelationPO> relations) {
        if (CollectionUtil.isNotEmpty(relations)) {
            // 使用 MyBatis-Plus 批量插入
            courseStudentRelationService.saveBatch(relations);
        }
    }

    /**
     * 统计课程学生人数
     *
     * @param courseId 课程ID
     * @return 学生人数
     */
    @Override
    public Long countCourseStudent(Integer courseId) {
        return courseStudentRelationMapper.selectCount(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId));
    }
}
