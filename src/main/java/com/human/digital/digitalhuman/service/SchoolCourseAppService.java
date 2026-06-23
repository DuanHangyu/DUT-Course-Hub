package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.*;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.CourseStatsDTO;
import com.human.digital.digitalhuman.service.model.response.SchoolCourseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 校本课程应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolCourseAppService {

    private final CourseService courseService;
    private final CourseNodeService courseNodeService;
    private final CourseStudentRelationService courseStudentRelationService;
    private final StudentService studentService;
    private final SchoolClassService schoolClassService;
    private final UserService userService;
    private final OssClientUtils ossClientUtils;
    private final CourseInviteService courseInviteService;
    private final SubjectService subjectService;

    /**
     * 分页查询校本课程
     *
     * @param userId
     * @param query  查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public IPage<SchoolCourseDTO> pageQuery(int userId, SchoolCourseQuery query) {
        log.info("分页查询校本课程, query:{}", query);
        int pageNum = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getSize() == null ? 10 : query.getSize();

        IPage<CoursePO> page;
        UserPO userPO = userService.getById(userId);
        // 如果是管理员，保持原有逻辑；否则只查询自己创建或关联的课程
        if (Objects.nonNull(userPO) && (Objects.equals(0, userPO.getRole())) || Objects.equals(1, userPO.getRole())) {
            page = courseService.pageQuerySchool(
                    pageNum,
                    pageSize,
                    query.getSchoolId(),
                    query.getCourseName(),
                    query.getState(),
                    query.getSubject()
            );
        } else {
            // 获取当前登录用户ID
            int currentUserId = StpUtil.getLoginIdAsInt();
            page = courseService.pageQuerySchoolMyCourse(
                    pageNum,
                    pageSize,
                    query.getSchoolId(),
                    query.getCourseName(),
                    query.getState(),
                    query.getSubject(),
                    currentUserId
            );
        }

        // 批量查询用户信息，避免 N+1 问题
        final Map<Integer, String> userNameMap;
        Map<Integer, String> tempMap = Collections.emptyMap();
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            List<Integer> creatorIds = page.getRecords().stream()
                    .map(CoursePO::getCreatorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (CollectionUtil.isNotEmpty(creatorIds)) {
                tempMap = userService.listByIds(creatorIds).stream()
                        .collect(Collectors.toMap(UserPO::getId, UserPO::getUserName, (a, b) -> a));
            }
        }
        userNameMap = tempMap;

        // 批量查询老师和学生的关联信息，避免 N+1 问题
        final Map<Integer, List<SchoolCourseDTO.TeacherInfo>> teacherInfoMap;
        final Map<Integer, List<SchoolCourseDTO.StudentInfo>> studentInfoMap;
        if (CollectionUtil.isNotEmpty(page.getRecords())) {
            List<Integer> courseIds = page.getRecords().stream().map(CoursePO::getId).toList();
            // 批量查询课程-老师关系
            List<CourseInvitePO> allInvites = courseInviteService.list(Wrappers.lambdaQuery(CourseInvitePO.class)
                    .in(CourseInvitePO::getCourseId, courseIds));
            // 批量查询课程-学生关系
            List<CourseStudentRelationPO> allRelations = courseStudentRelationService.queryByCourseIds(courseIds);

            // 收集所有老师ID和学生ID
            List<Integer> allTeacherIds = allInvites.stream()
                    .map(CourseInvitePO::getInviteeTeacherId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            List<Integer> allStudentIds = allRelations.stream()
                    .map(CourseStudentRelationPO::getStudentId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询老师和学生信息
            if (CollectionUtils.isEmpty(allTeacherIds)) {
                allTeacherIds.add(-1);
            }
            if (CollectionUtils.isEmpty(allStudentIds)) {
                allStudentIds.add(-1);
            }
            Map<Integer, UserPO> teacherMap = userService.listByIds(allTeacherIds).stream()
                    .collect(Collectors.toMap(UserPO::getId, u -> u, (a, b) -> a));
            Map<Integer, StudentPO> studentMap = studentService.listByIds(allStudentIds).stream()
                    .collect(Collectors.toMap(StudentPO::getId, s -> s, (a, b) -> a));

            // 按课程ID分组老师信息
            teacherInfoMap = allInvites.stream()
                    .collect(Collectors.groupingBy(
                            CourseInvitePO::getCourseId,
                            Collectors.mapping(
                                    invite -> {
                                        UserPO teacher = teacherMap.get(invite.getInviteeTeacherId());
                                        if (teacher == null) return null;
                                        return SchoolCourseDTO.TeacherInfo.builder()
                                                .id(teacher.getId())
                                                .userName(teacher.getUserName())
                                                .build();
                                    },
                                    Collectors.filtering(Objects::nonNull, Collectors.toList())
                            )
                    ));

            // 按课程ID分组学生信息
            studentInfoMap = allRelations.stream()
                    .collect(Collectors.groupingBy(
                            CourseStudentRelationPO::getCourseId,
                            Collectors.mapping(
                                    relation -> {
                                        StudentPO student = studentMap.get(relation.getStudentId());
                                        if (student == null) return null;
                                        return SchoolCourseDTO.StudentInfo.builder()
                                                .id(student.getId())
                                                .studentName(student.getStudentName())
                                                .build();
                                    },
                                    Collectors.filtering(Objects::nonNull, Collectors.toList())
                            )
                    ));
        } else {
            teacherInfoMap = java.util.Collections.emptyMap();
            studentInfoMap = java.util.Collections.emptyMap();
        }

        return page.convert(po -> {
            SchoolCourseDTO dto = SchoolCourseDTO.of(po, ossClientUtils::getSignedUrl);
            // 设置创建人名称
            dto.setCreateName(userNameMap.getOrDefault(po.getCreatorId(), ""));
            // 设置老师信息
            dto.setTeachers(teacherInfoMap.getOrDefault(po.getId(), java.util.Collections.emptyList()));
            // 设置学生信息
            dto.setStudents(studentInfoMap.getOrDefault(po.getId(), java.util.Collections.emptyList()));
            return dto;
        });
    }

    /**
     * 创建校本课程
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(SchoolCourseCmd cmd) {
        log.info("创建校本课程, cmd:{}", cmd);
        // 确保学科存在，不存在则自动新增
        ensureSubjectExists(cmd.getSchoolId(), cmd.getSubject());
        CoursePO coursePO = cmd.toPo();
        boolean result = courseService.save(coursePO);
        // 保存老师关联
        saveTeacherRelations(coursePO.getId(), cmd.getTeacherIds());
        // 如果存在模版课程ID，复制模版课程的节点信息
        if (cmd.getRelationCourseId() != null) {
            copyCourseNodesFromTemplate(cmd.getRelationCourseId(), coursePO.getId());
        }
        log.info("创建校本课程成功, courseId:{}", coursePO.getId());
        return result;
    }

    /**
     * 编辑校本课程
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SchoolCourseCmd cmd) {
        log.info("编辑校本课程, cmd:{}", cmd);
        CoursePO existCourse = courseService.getById(cmd.getId());
        if (existCourse == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        // 确保学科存在，不存在则自动新增
        ensureSubjectExists(cmd.getSchoolId(), cmd.getSubject());

        CoursePO coursePO = cmd.toPo();
        coursePO.setId(cmd.getId());
        coursePO.setCreateTime(existCourse.getCreateTime());
        coursePO.setCreatorId(existCourse.getCreatorId());
        // 不更新inviteCode和studentCount
        coursePO.setStudentCount(existCourse.getStudentCount());
        boolean result = courseService.updateById(coursePO);
        // 更新老师关联
        updateTeacherRelations(cmd.getId(), cmd.getTeacherIds());
        log.info("编辑校本课程成功, courseId:{}", cmd.getId());
        return result;
    }

    /**
     * 删除校本课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        log.info("删除校本课程, courseId:{}", id);
        CoursePO existCourse = courseService.getById(id);
        if (existCourse == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        // 删除课程关联的节点
        List<CourseNodePO> nodes = courseNodeService.queryByCourseId(id);
        if (CollectionUtil.isNotEmpty(nodes)) {
            courseNodeService.removeByIds(nodes.stream().map(CourseNodePO::getId).collect(Collectors.toList()));
            log.info("删除课程关联节点, count:{}", nodes.size());
        }
        // 删除课程学生关联
        courseStudentRelationService.remove(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, id));
        boolean result = courseService.removeById(id);
        log.info("删除校本课程成功, courseId:{}", id);
        return result;
    }

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @Transactional(readOnly = true)
    public SchoolCourseDTO detail(Integer id) {
        log.info("获取课程详情, courseId:{}", id);
        CoursePO coursePO = courseService.getById(id);
        if (coursePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        SchoolCourseDTO dto = SchoolCourseDTO.of(coursePO, ossClientUtils::getSignedUrl);
        if (coursePO.getCreatorId() != null) {
            UserPO user = userService.getById(coursePO.getCreatorId());
            dto.setCreateName(user != null ? user.getUserName() : "");
        }
        // 设置老师信息
        dto.setTeachers(getTeacherInfos(id));
        // 设置学生信息
        List<SchoolCourseDTO.StudentInfo> studentInfos = getStudentInfos(id);
        dto.setStudents(studentInfos.stream()
                .filter(item -> Objects.equals(item.getRelationType(), 0))
                .toList());
        // 设置班级信息
        List<Integer> classIds = studentInfos.stream()
                .filter(item -> Objects.equals(item.getRelationType(), 1))
                .map(SchoolCourseDTO.StudentInfo::getSchoolClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<SchoolCourseDTO.SchoolClassInfo> schoolClasses = null;
        if (CollectionUtils.isNotEmpty(classIds)) {
            List<SchoolClassPO> schoolClassPOS = schoolClassService.listByIds(classIds);
            schoolClasses = schoolClassPOS.stream()
                    .map(po -> SchoolCourseDTO.SchoolClassInfo.builder()
                            .id(po.getId())
                            .schoolClassName(po.getClassName())
                            .build())
                    .toList();
        }
        dto.setSchoolClasses(schoolClasses);
        return dto;
    }

    /**
     * 设置/取消精选
     *
     * @param cmd 精选命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean setFeatured(CourseFeaturedCmd cmd) {
        log.info("设置课程精选状态, courseId:{}, isFeatured:{}", cmd.getCourseId(), cmd.getIsFeatured());
        CoursePO coursePO = courseService.getById(cmd.getCourseId());
        if (coursePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        coursePO.setIsFeatured(cmd.getIsFeatured());
        boolean result = courseService.updateById(coursePO);
        log.info("设置课程精选状态成功, courseId:{}", cmd.getCourseId());
        return result;
    }

    /**
     * 添加学生到课程
     *
     * @param cmd 添加学生命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addStudents(AddStudentsCmd cmd) {
        log.info("添加学生到课程, courseId:{}, studentIds:{}", cmd.getCourseId(), cmd.getStudentIds());
        CoursePO coursePO = courseService.getById(cmd.getCourseId());
        if (coursePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        List<CourseStudentRelationPO> relations = new ArrayList<>();
        for (Integer studentId : cmd.getStudentIds()) {
            // 检查是否已存在
            CourseStudentRelationPO exist = courseService.getCourseStudentRelation(cmd.getCourseId(), studentId);
            if (exist == null) {
                CourseStudentRelationPO relation = new CourseStudentRelationPO();
                relation.setCourseId(cmd.getCourseId());
                relation.setStudentId(studentId);
                relation.setRelationType(0); // 学生
                relations.add(relation);
            }
        }

        if (CollectionUtil.isNotEmpty(relations)) {
            courseService.saveBatchCourseStudentRelation(relations);
            log.info("批量添加学生关系, count:{}", relations.size());
        }

        // 更新学生人数
        updateStudentCount(cmd.getCourseId());
        log.info("添加学生到课程成功, courseId:{}", cmd.getCourseId());
        return true;
    }

    /**
     * 添加班级到课程
     *
     * @param cmd 添加班级命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean addClasses(AddClassesCmd cmd) {
        log.info("添加班级到课程, courseId:{}, classIds:{}", cmd.getCourseId(), cmd.getClassIds());
        CoursePO coursePO = courseService.getById(cmd.getCourseId());
        if (coursePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        int totalAddedCount = 0;
        // 获取班级下的所有学生
        for (Integer classId : cmd.getClassIds()) {
            SchoolClassPO schoolClass = schoolClassService.getById(classId);
            if (schoolClass == null) {
                log.warn("添加班级到课程失败，班级不存在, classId:{}", classId);
                continue;
            }

            List<StudentPO> students = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                    .eq(StudentPO::getClassId, classId));

            if (CollectionUtil.isNotEmpty(students)) {
                List<CourseStudentRelationPO> relations = new ArrayList<>();
                for (StudentPO student : students) {
                    // 检查是否已存在
                    CourseStudentRelationPO exist = courseService.getCourseStudentRelation(cmd.getCourseId(), student.getId());
                    if (exist == null) {
                        CourseStudentRelationPO relation = new CourseStudentRelationPO();
                        relation.setCourseId(cmd.getCourseId());
                        relation.setStudentId(student.getId());
                        relation.setRelationType(1); // 班级
                        relations.add(relation);
                    }
                }

                if (CollectionUtil.isNotEmpty(relations)) {
                    courseService.saveBatchCourseStudentRelation(relations);
                    totalAddedCount += relations.size();
                }
            }
        }

        // 更新学生人数
        updateStudentCount(cmd.getCourseId());
        log.info("添加班级到课程成功, courseId:{}, totalAddedCount:{}", cmd.getCourseId(), totalAddedCount);
        return true;
    }

    /**
     * 生成邀请码
     *
     * @return 邀请码
     */
    @Transactional(rollbackFor = Exception.class)
    public String generateInviteCode() {
        return generateUniqueInviteCode();
    }

    /**
     * 获取课程统计
     *
     * @param schoolId 学校ID
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    public CourseStatsDTO getStats(Integer schoolId) {
        log.info("获取课程统计, schoolId:{}", schoolId);
        List<CoursePO> courses = courseService.list(Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getCourseType, 2) // 校本课程
                .eq(schoolId != null, CoursePO::getSchoolId, schoolId));

        long publishedCount = courses.stream().filter(c -> c.getState() != null && c.getState()).count();
        long unpublishedCount = courses.stream().filter(c -> c.getState() == null || !c.getState()).count();

        log.info("获取课程统计成功, schoolId:{}, publishedCount:{}, unpublishedCount:{}", schoolId, publishedCount, unpublishedCount);
        return new CourseStatsDTO(publishedCount, unpublishedCount);
    }

    /**
     * 从模板创建校本课程
     *
     * @param templateId 模板课程ID
     * @param schoolId   学校ID
     * @return 课程ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createFromTemplate(Integer templateId, Integer schoolId) {
        log.info("从模板创建校本课程, templateId:{}, schoolId:{}", templateId, schoolId);
        // 获取模板课程
        CoursePO template = courseService.getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        // 复制课程信息
        CoursePO course = new CoursePO();
        course.setCourseName(template.getCourseName());
        course.setCourseIntroduce(template.getCourseIntroduce());
        course.setState(false); // 默认下架
        course.setPictureUrl(template.getPictureUrl());
        course.setSubject(template.getSubject());
        course.setTeacherName(template.getTeacherName());
        course.setHeadUrl(template.getHeadUrl());
        course.setCourseType(2); // 校本课程
        course.setSchoolId(schoolId);
        course.setInviteCode(generateUniqueInviteCode());
        course.setStudentCount(0);
        courseService.save(course);

        // 复制课程节点
        List<CourseNodePO> templateNodes = courseNodeService.queryByCourseId(templateId);
        if (CollectionUtil.isNotEmpty(templateNodes)) {
            List<CourseNodePO> newNodes = templateNodes.stream().map(node -> {
                CourseNodePO newNode = new CourseNodePO();
                newNode.setCourseId(course.getId());
                newNode.setNodeName(node.getNodeName());
                newNode.setNodeIntroduce(node.getNodeIntroduce());
                newNode.setNodeSize(node.getNodeSize());
                newNode.setNodeColour(node.getNodeColour());
                newNode.setXAxis(node.getXAxis());
                newNode.setYAxis(node.getYAxis());
                newNode.setRelateNode(node.getRelateNode());
                newNode.setVideoUrl(node.getVideoUrl());
                newNode.setFilesUrl(node.getFilesUrl());
                return newNode;
            }).collect(Collectors.toList());
            courseNodeService.saveBatch(newNodes);
            log.info("从模板创建课程节点成功, templateId:{}, newCourseId:{}, nodeCount:{}", templateId, course.getId(), newNodes.size());
        }

        log.info("从模板创建校本课程成功, newCourseId:{}", course.getId());
        return course.getId();
    }

    /**
     * 获取学科列表
     *
     * @return 学科列表
     */
    @Transactional(readOnly = true)
    public List<String> subjectList(Integer schoolId) {
        log.info("获取学科列表");
        List<CoursePO> coursePOS = courseService.list(Wrappers.lambdaQuery(CoursePO.class)
                .eq(schoolId != null, CoursePO::getSchoolId, schoolId)
                .eq(CoursePO::getCourseType, 2)); // 校本课程
        List<String> subjects = coursePOS.stream()
                .map(CoursePO::getSubject)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        log.info("获取学科列表成功, count:{}", subjects.size());
        return subjects;
    }

    /**
     * 更新学生人数
     *
     * @param courseId 课程ID
     */
    private void updateStudentCount(Integer courseId) {
        Long count = courseService.countCourseStudent(courseId);
        CoursePO course = courseService.getById(courseId);
        if (course != null) {
            course.setStudentCount(count.intValue());
            courseService.updateById(course);
        }
    }

    /**
     * 生成唯一邀请码
     *
     * @return 邀请码
     */
    private String generateUniqueInviteCode() {
        String inviteCode;
        do {
            inviteCode = RandomStringUtils.randomAlphabetic(6).toUpperCase();
        } while (courseService.getByInviteCode(inviteCode) != null);
        return inviteCode;
    }

    /**
     * 确保学科存在，不存在则自动新增
     *
     * @param schoolId    学校ID
     * @param subjectName 学科名称
     */
    private void ensureSubjectExists(Integer schoolId, String subjectName) {
        if (schoolId == null || StrUtil.isBlank(subjectName)) {
            return;
        }
        // 查询学科是否已存在
        List<SubjectPO> existingSubjects = subjectService.listBySchoolId(schoolId);
        boolean exists = existingSubjects.stream()
                .anyMatch(s -> s.getName().equals(subjectName));
        if (!exists) {
            // 新增学科
            SubjectPO newSubject = new SubjectPO();
            newSubject.setSchoolId(schoolId);
            newSubject.setName(subjectName);
            newSubject.setSortOrder(existingSubjects.size());
            subjectService.save(newSubject);
            log.info("自动新增学科, schoolId:{}, subjectName:{}", schoolId, subjectName);
        }
    }

    /**
     * 保存老师关联关系
     *
     * @param courseId   课程ID
     * @param teacherIds 老师ID列表
     */
    private void saveTeacherRelations(Integer courseId, List<Integer> teacherIds) {
        if (CollectionUtil.isEmpty(teacherIds)) {
            return;
        }
        int teacherId = StpUtil.getLoginIdAsInt();
        List<CourseInvitePO> invites = teacherIds.stream()
                .map(id -> CourseInvitePO.builder()
                        .courseId(courseId)
                        .inviteeTeacherId(id)
                        .InviterTeacherId(teacherId)
                        .build())
                .toList();
        courseInviteService.saveBatch(invites);
        log.info("保存老师关联关系成功, courseId:{}, teacherIds:{}", courseId, teacherIds);
    }

    /**
     * 更新老师关联关系
     *
     * @param courseId   课程ID
     * @param teacherIds 老师ID列表
     */
    private void updateTeacherRelations(Integer courseId, List<Integer> teacherIds) {
        // 先删除原有的关联
        courseInviteService.remove(Wrappers.lambdaQuery(CourseInvitePO.class)
                .eq(CourseInvitePO::getCourseId, courseId));
        // 再保存新的关联
        saveTeacherRelations(courseId, teacherIds);
    }

    /**
     * 获取课程关联的老师信息列表
     *
     * @param courseId 课程ID
     * @return 老师信息列表
     */
    private List<SchoolCourseDTO.TeacherInfo> getTeacherInfos(Integer courseId) {
        List<CourseInvitePO> invites = courseInviteService.list(Wrappers.lambdaQuery(CourseInvitePO.class)
                .eq(CourseInvitePO::getCourseId, courseId));
        if (CollectionUtil.isEmpty(invites)) {
            return java.util.Collections.emptyList();
        }
        List<Integer> teacherIds = invites.stream()
                .map(CourseInvitePO::getInviteeTeacherId)
                .distinct()
                .toList();
        List<UserPO> teachers = userService.listByIds(teacherIds);
        if (CollectionUtil.isEmpty(teachers)) {
            return java.util.Collections.emptyList();
        }
        return teachers.stream()
                .map(teacher -> SchoolCourseDTO.TeacherInfo.builder()
                        .id(teacher.getId())
                        .userName(teacher.getUserName())
                        .build())
                .toList();
    }

    private List<SchoolCourseDTO.StudentInfo> getStudentInfos(Integer courseId) {
        List<CourseStudentRelationPO> relations = courseStudentRelationService.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId));
        if (CollectionUtil.isEmpty(relations)) {
            return java.util.Collections.emptyList();
        }
        List<Integer> studentIds = relations.stream()
                .map(CourseStudentRelationPO::getStudentId)
                .distinct()
                .toList();
        Map<Integer, Integer> studentIdRelationMap = relations.stream().collect(Collectors.toMap(CourseStudentRelationPO::getStudentId, CourseStudentRelationPO::getRelationType, (v1, v2) -> v1));
        List<StudentPO> students = studentService.listByIds(studentIds);
        if (CollectionUtil.isEmpty(students)) {
            return java.util.Collections.emptyList();
        }
        return students.stream()
                .map(student -> SchoolCourseDTO.StudentInfo.builder()
                        .id(student.getId())
                        .relationType(studentIdRelationMap.getOrDefault(student.getId(), 0))
                        .schoolClassId(student.getClassId())
                        .studentName(student.getStudentName())
                        .build())
                .toList();
    }

    /**
     * 从模版课程复制节点信息到新课程
     *
     * @param templateCourseId 模版课程ID
     * @param newCourseId      新课程ID
     */
    private void copyCourseNodesFromTemplate(Integer templateCourseId, Integer newCourseId) {
        log.info("从模版课程复制节点信息, templateCourseId:{}, newCourseId:{}", templateCourseId, newCourseId);
        // 获取模版课程的节点列表
        List<CourseNodePO> templateNodes = courseNodeService.queryByCourseId(templateCourseId);
        if (CollectionUtil.isEmpty(templateNodes)) {
            log.info("模版课程没有节点信息, templateCourseId:{}", templateCourseId);
            return;
        }

        // 建立旧节点ID到新节点的映射（用于后续更新关联节点ID）
        Map<Integer, CourseNodePO> oldNodeIdToNewNodeMap = new java.util.HashMap<>();

        // 复制节点信息到新课程（暂不设置relateNode）
        List<CourseNodePO> newNodes = templateNodes.stream().map(node -> {
            CourseNodePO newNode = new CourseNodePO();
            newNode.setCourseId(newCourseId);
            newNode.setNodeName(node.getNodeName());
            newNode.setNodeIntroduce(node.getNodeIntroduce());
            newNode.setNodeSize(node.getNodeSize());
            newNode.setNodeColour(node.getNodeColour());
            newNode.setVideoUrl(node.getVideoUrl());
            newNode.setTaskId(node.getTaskId());
            newNode.setContent(node.getContent());
            newNode.setVideoToTextErrorMessage(node.getVideoToTextErrorMessage());
            newNode.setFilesUrl(node.getFilesUrl());
            newNode.setPassLine(node.getPassLine());
            newNode.setXAxis(node.getXAxis());
            newNode.setYAxis(node.getYAxis());
            newNode.setQuestionCount(node.getQuestionCount());
            newNode.setCreatorId(StpUtil.getLoginIdAsInt());
            // 记录旧节点ID到新节点的映射
            oldNodeIdToNewNodeMap.put(node.getId(), newNode);
            return newNode;
        }).collect(Collectors.toList());

        // 批量保存新节点，此时会生成新的ID
        courseNodeService.saveBatch(newNodes);

        // 更新关联节点ID：将relateNode中的旧节点ID替换为新节点ID
        List<CourseNodePO> nodesNeedUpdate = new ArrayList<>();
        for (CourseNodePO templateNode : templateNodes) {
            CourseNodePO newNode = oldNodeIdToNewNodeMap.get(templateNode.getId());
            if (newNode != null && StrUtil.isNotBlank(templateNode.getRelateNode())) {
                // 将旧的关联节点ID列表转换为新ID列表
                List<Integer> newRelateNode = updateRelateNodeIds(templateNode.getRelateNode(), oldNodeIdToNewNodeMap);
                newNode.setRelateNode(JSONUtil.toJsonStr(newRelateNode));
                nodesNeedUpdate.add(newNode);
            }
        }

        // 批量更新有关联关系变化的节点
        if (CollectionUtil.isNotEmpty(nodesNeedUpdate)) {
            courseNodeService.updateBatchById(nodesNeedUpdate);
            log.info("更新关联节点ID成功, count:{}", nodesNeedUpdate.size());
        }

        log.info("从模版课程复制节点信息成功, templateCourseId:{}, newCourseId:{}, nodeCount:{}",
                templateCourseId, newCourseId, newNodes.size());
    }

    /**
     * 将关联节点ID字符串中的旧ID替换为新ID
     *
     * @param relateNode        原关联节点ID字符串（逗号分隔）
     * @param oldToNewNodeIdMap 旧节点ID到新节点的映射
     * @return 更新后的关联节点ID字符串
     */
    private List<Integer> updateRelateNodeIds(String relateNode, Map<Integer, CourseNodePO> oldToNewNodeIdMap) {
        if (StrUtil.isBlank(relateNode)) {
            return List.of();
        }
        List<Integer> oldIds = StringUtils.isNotBlank(relateNode) ? JSONUtil.toList(relateNode, Integer.class) : List.of();
        List<Integer> newIds = new ArrayList<>();
        for (Integer oldId : oldIds) {
            CourseNodePO newNode = oldToNewNodeIdMap.get(oldId);
            if (newNode != null && newNode.getId() != null) {
                newIds.add(newNode.getId());
            }
        }
        return newIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean addRelationStudent(@Valid AddRelationStudentCmd cmd) {
        courseStudentRelationService.deleteByCourseId(cmd.getCourseId());
        if (CollectionUtils.isNotEmpty(cmd.getStudentIds())) {
            AddStudentsCmd addStudentsCmd = new AddStudentsCmd();
            addStudentsCmd.setCourseId(cmd.getCourseId());
            addStudentsCmd.setStudentIds(cmd.getStudentIds());
            addStudents(addStudentsCmd);
        }
        if (CollectionUtils.isNotEmpty(cmd.getClassIds())) {
            AddClassesCmd addClassesCmd = new AddClassesCmd();
            addClassesCmd.setCourseId(cmd.getCourseId());
            addClassesCmd.setClassIds(cmd.getClassIds());
            addClasses(addClassesCmd);
        }
        return true;
    }
}
