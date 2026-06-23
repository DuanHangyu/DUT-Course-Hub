package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
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
import com.human.digital.digitalhuman.service.model.event.NodeModifyEvent;
import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.request.SchoolStudentDTO;
import com.human.digital.digitalhuman.service.model.response.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author taoHouChao
 * @Date 20:33 2025/6/11
 */
@Service
@RequiredArgsConstructor
public class CourseAppService {

    private final CourseService courseService;

    private final CourseNodeService courseNodeService;

    private final StudentCourseAssessService studentCourseAssessService;

    private final OssClientUtils ossClientUtils;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final StudentService studentService;

    private final CourseStudentRelationService relationService;

    private final CourseInviteService inviteService;

    private final UserService userService;

    private final StudentCourseNodeStudyRecordService studyRecordService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean create(CourseCreateCmd cmd) {
        int teacherId = StpUtil.getLoginIdAsInt();
        CoursePO course = cmd.toPo();
        courseService.save(course);
        saveInviteAndStudent(teacherId, course.getId(), cmd.getInviteTeachers(), cmd.getSchoolClassList(), cmd.getStudentIds());
        return true;
    }

    private void saveInviteAndStudent(Integer teacherId, Integer courseId,
                                      List<Integer> inviteTeachers,
                                      List<SchoolStudentDTO> schoolClassList,
                                      List<Integer> studentIds) {
        Set<Integer> existStudentIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(schoolClassList)) {
            List<CourseStudentRelationPO> relationPOList = new ArrayList<>();
            for (SchoolStudentDTO schoolStudentDTO : schoolClassList) {
                List<Integer> studentIdList = schoolStudentDTO.getStudentIds();
                for (Integer studentId : studentIdList) {
                    existStudentIds.add(studentId);
                    relationPOList.add(CourseStudentRelationPO.builder()
                            .courseId(courseId)
                            .studentId(studentId)
                            .relationType(1)
                            .build());
                }
            }
            relationService.saveBatch(relationPOList);
        }

        if (CollectionUtils.isNotEmpty(studentIds)) {
            boolean existStudentId = studentIds.stream().anyMatch(existStudentIds::contains);
            if (existStudentId) {
                throw new BusinessException(ErrorCodeEnums.STUDENT_EXIST);
            }
            Optional.of(studentIds)
                    .map(studentId -> studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                            .in(StudentPO::getId, studentId)))
                    .map(students -> students.stream().map(student -> CourseStudentRelationPO.builder()
                            .courseId(courseId)
                            .studentId(student.getId())
                            .relationType(0)
                            .build()).toList())
                    .ifPresent(relationService::saveBatch);
        }

        if (CollectionUtils.isNotEmpty(inviteTeachers)) {
            Optional.of(inviteTeachers)
                    .map(teacherIds -> userService.list(Wrappers.lambdaQuery(UserPO.class)
                            .in(UserPO::getId, teacherIds)))
                    .map(teachers -> teachers.stream().map(teacher -> CourseInvitePO.builder()
                            .courseId(courseId)
                            .inviteeTeacherId(teacher.getId())
                            .InviterTeacherId(teacherId)
                            .build()).toList())
                    .ifPresent(inviteService::saveBatch);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean modify(CourseModifyCmd cmd) {
        int teacherId = StpUtil.getLoginIdAsInt();
        // 先删除之前的绑定数据，再绑定
        relationService.remove(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, cmd.getId()));
        inviteService.remove(Wrappers.lambdaQuery(CourseInvitePO.class)
                .eq(CourseInvitePO::getCourseId, cmd.getId()));
        saveInviteAndStudent(teacherId, cmd.getId(), cmd.getInviteTeachers(), cmd.getSchoolClassList(), cmd.getStudentIds());
        return courseService.update(Wrappers.lambdaUpdate(CoursePO.class)
                .set(StrUtil.isNotBlank(cmd.getCourseName()), CoursePO::getCourseName, cmd.getCourseName())
                .set(StrUtil.isNotBlank(cmd.getCourseIntroduce()), CoursePO::getCourseIntroduce, cmd.getCourseIntroduce())
                .set(StrUtil.isNotBlank(cmd.getTeacherName()), CoursePO::getTeacherName, cmd.getTeacherName())
                .set(StrUtil.isNotBlank(cmd.getHeadUrl()), CoursePO::getHeadUrl, cmd.getHeadUrl())
                .set(StrUtil.isNotBlank(cmd.getPictureUrl()), CoursePO::getPictureUrl, cmd.getPictureUrl())
                .set(cmd.getState() != null, CoursePO::getState, cmd.getState())
                .set(cmd.getLastDefenseQuestionsCount() != null, CoursePO::getLastDefenseQuestionsCount, cmd.getLastDefenseQuestionsCount())
                .set(CoursePO::getRelationType, cmd.generateRelationType())
                .eq(CoursePO::getId, cmd.getId()));
    }

    public Boolean delete(CourseModifyCmd cmd) {
        relationService.remove(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, cmd.getId()));
        inviteService.remove(Wrappers.lambdaQuery(CourseInvitePO.class)
                .eq(CourseInvitePO::getCourseId, cmd.getId()));
        return courseService.removeById(cmd.getId());
    }

    /**
     * 分页查询课程列表
     *
     * @param query 课程列表查询参数，包含课程名称、状态、页码、每页数量等信息
     * @return 返回分页的课程摘要信息，包括课程基本信息、关联的学生和教师信息
     */
    @Transactional(readOnly = true)
    public IPage<CourseSummaryDTO> pageQuery(CourseListQuery query) {
        // 获取当前登录教师ID
        int teacherId = StpUtil.getLoginIdAsInt();
        // 根据教师ID和查询条件分页查询课程信息
        IPage<CoursePO> page = courseService.pageQuery(teacherId, query.getPage(), query.getSize(), query.getCourseName(), query.getState());
        // 将CoursePO转换为CourseSummaryDTO
        IPage<CourseSummaryDTO> resultPage = page.convert(coursePO -> BeanUtil.copyProperties(coursePO, CourseSummaryDTO.class));
        List<CourseSummaryDTO> records = resultPage.getRecords();
        if (CollectionUtil.isNotEmpty(records)) {
            // 提取所有课程ID
            List<Integer> courseIds = records.stream().map(CourseSummaryDTO::getId).toList();
            // 查询这些课程的所有邀请教师信息
            List<CourseInvitePO> invites = inviteService.list(Wrappers.lambdaQuery(CourseInvitePO.class)
                    .in(CourseInvitePO::getCourseId, courseIds));
            Map<Integer, List<UserSummaryDTO>> courseIdInviteMap = new HashMap<>(10);
            if (CollectionUtil.isNotEmpty(invites)) {
                // 获取被邀请教师的ID列表
                List<Integer> teacherIds = invites.stream().map(CourseInvitePO::getInviteeTeacherId).distinct().toList();
                List<UserPO> userPOS = userService.listByIds(teacherIds);
                // 构建教师ID到UserSummaryDTO的映射
                Map<Integer, UserSummaryDTO> teacherIdMap = userPOS.stream().collect(Collectors.toMap(UserPO::getId, item -> BeanUtil.copyProperties(item, UserSummaryDTO.class)));
                // 按课程ID分组，构建课程ID到被邀请教师列表的映射
                courseIdInviteMap = invites.stream()
                        .collect(Collectors.groupingBy(CourseInvitePO::getCourseId, Collectors.mapping(item -> teacherIdMap.get(item.getInviteeTeacherId()), Collectors.toList())));
            }
            // 查询课程与学生的关联关系
            List<CourseStudentRelationPO> relations = relationService.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                    .in(CourseStudentRelationPO::getCourseId, courseIds));
            Map<Integer, List<CourseStudentRelationPO>> relationCourseIdMap = relations.stream().collect(Collectors.groupingBy(CourseStudentRelationPO::getCourseId));
            Map<Integer, StudentSummaryDTO> studentIdMap = new HashMap<>();
            if (CollectionUtil.isNotEmpty(relations)) {
                // 获取学生ID列表
                List<Integer> studentIds = relations.stream().map(CourseStudentRelationPO::getStudentId).distinct().toList();
                List<StudentPO> students = studentService.listByIds(studentIds);
                // 构建学生ID到StudentSummaryDTO的映射
                studentIdMap.putAll(students.stream().collect(Collectors.toMap(StudentPO::getId, item -> BeanUtil.copyProperties(item, StudentSummaryDTO.class))));
            }
            // 获取课程创建者ID列表
            List<Integer> teacherIds = records.stream().map(CourseSummaryDTO::getCreatorId).distinct().toList();
            // 构建创建者ID到UserPO的映射
            Map<Integer, UserPO> userIdMap = userService.listByIds(teacherIds)
                    .stream()
                    .collect(Collectors.toMap(UserPO::getId, item -> item));
            // 填充每门课程的详细信息
            for (CourseSummaryDTO record : records) {
                // 设置课程创建者名称
                record.setCreateName(Optional.ofNullable(userIdMap.get(record.getCreatorId())).map(UserPO::getUserName).orElse(""));

                List<CourseStudentRelationPO> matchRelations = relationCourseIdMap.getOrDefault(record.getId(), new ArrayList<>());
                Integer relationType = record.getRelationType();
                // 根据关系类型设置学生列表或班级列表
                if (Objects.equals(relationType, 0)) {
                    record.setStudents(matchRelations.stream().distinct().map(item -> studentIdMap.get(item.getStudentId())).toList());
                } else if (Objects.equals(relationType, 1)) {
                    record.setSchoolClassList(matchRelations.stream()
                            .map(item -> studentIdMap.get(item.getStudentId()))
                            .map(StudentSummaryDTO::getSchoolClass)
                            .distinct().toList());
                } else if (Objects.equals(relationType, 2)) {
                    record.setStudents(matchRelations.stream()
                            .filter(item -> Objects.equals(item.getRelationType(), 0))
                            .distinct()
                            .map(item -> studentIdMap.get(item.getStudentId())).toList());
                    record.setSchoolClassList(matchRelations.stream()
                            .filter(item -> Objects.equals(item.getRelationType(), 1))
                            .map(item -> studentIdMap.get(item.getStudentId()))
                            .map(StudentSummaryDTO::getSchoolClass)
                            .distinct().toList());
                }
                // 设置邀请教师列表
                List<UserSummaryDTO> orDefault = courseIdInviteMap.getOrDefault(record.getId(), new ArrayList<>());
                record.setInviteTeachers(orDefault);
            }
        }
        return resultPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer createNode(CourseNodeCreateCmd createCmd) {
        CourseNodePO courseNode = createCmd.toPo();
        courseNodeService.save(courseNode);
        // 处理双关问题 新节点关联的节点，也需要关联新节点
        List<Integer> relateNode = createCmd.getRelateNode();
        if (CollectionUtil.isNotEmpty(relateNode)) {
            addRelateNode(relateNode, courseNode.getId());
        }
        applicationEventPublisher.publishEvent(new NodeModifyEvent(this, courseNode));
        return courseNode.getId();
    }

    public Boolean modifyNode(CourseNodeModifyCmd modifyCmd) {
        CourseNodePO courseNode = courseNodeService.getById(modifyCmd.getId());
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }
        courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                .set(CourseNodePO::getNodeName, modifyCmd.getNodeName())
                .set(CourseNodePO::getNodeIntroduce, modifyCmd.getNodeIntroduce())
                .set(CourseNodePO::getRelateNode, JSONUtil.toJsonStr(modifyCmd.getRelateNode()))
                .set(CourseNodePO::getNodeSize, modifyCmd.getNodeSize())
                .set(CourseNodePO::getNodeColour, modifyCmd.getNodeColour())
                .set(CourseNodePO::getVideoUrl, JSONUtil.toJsonStr(modifyCmd.getVideo()))
                .set(CourseNodePO::getFilesUrl, JSONUtil.toJsonStr(modifyCmd.getFiles()))
                .set(CourseNodePO::getQuestionCount, modifyCmd.getQuestionCount())
                .set(CourseNodePO::getPassLine, modifyCmd.getPassLine())
                .eq(CourseNodePO::getId, modifyCmd.getId()));
        // 修改了关联节点,1.原先没有，修改后新增， 2.原先有，修改后没有，3.修改了关联节点
        if (!Objects.equals(courseNode.getRelateNode(), JSONUtil.toJsonStr(modifyCmd.getRelateNode()))) {
            // 原先是空的，新增关联节点
            if (StrUtil.isBlank(courseNode.getRelateNode()) && CollectionUtil.isNotEmpty(modifyCmd.getRelateNode())) {
                addRelateNode(modifyCmd.getRelateNode(), courseNode.getId());
            }
            // 删除了关联节点
            else if (StrUtil.isNotBlank(courseNode.getRelateNode()) && CollectionUtil.isEmpty(modifyCmd.getRelateNode())) {
                deleteRelateNode(JSONUtil.toList(courseNode.getRelateNode(), Integer.class), courseNode.getId());
            }
            // 修改了关联节点
            else if (StrUtil.isNotBlank(courseNode.getRelateNode()) && CollectionUtil.isNotEmpty(modifyCmd.getRelateNode())) {
                List<Integer> originRelateList = JSONUtil.toList(courseNode.getRelateNode(), Integer.class);
                List<Integer> modifyRelateList = modifyCmd.getRelateNode();

                List<Integer> deleteNodeIds = originRelateList.stream().filter(origin -> !modifyRelateList.contains(origin)).toList();
                deleteRelateNode(deleteNodeIds, courseNode.getId());

                List<Integer> addNodeIds = modifyRelateList.stream().filter(modify -> !originRelateList.contains(modify)).toList();
                addRelateNode(addNodeIds, courseNode.getId());
            }
        }
        // 修改了视频，需要重新生成课程内容
        if (!Objects.equals(courseNode.getVideoUrl(), JSONUtil.toJsonStr(modifyCmd.getVideo()))) {
            courseNode = courseNodeService.getById(courseNode.getId());
            applicationEventPublisher.publishEvent(new NodeModifyEvent(this, courseNode));
        }
        return true;
    }

    private void deleteRelateNode(List<Integer> deleteNodeIds, Integer id) {
        if (CollectionUtil.isEmpty(deleteNodeIds)) {
            return;
        }
        List<CourseNodePO> courseNodes = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .in(CourseNodePO::getId, deleteNodeIds));
        if (CollectionUtil.isNotEmpty(courseNodes)) {
            for (CourseNodePO node : courseNodes) {
                if (node.startNode() || node.endNode()) {
                    continue;
                }
                List<Integer> nodeRelates = JSONUtil.toList(node.getRelateNode(), Integer.class);
                nodeRelates.remove(id);
                node.setRelateNode(JSONUtil.toJsonStr(nodeRelates));
            }
            courseNodeService.updateBatchById(courseNodes);
        }
    }

    private void addRelateNode(List<Integer> modifyCmd, Integer id) {
        if (CollectionUtil.isEmpty(modifyCmd)) {
            return;
        }
        List<CourseNodePO> courseNodes = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .in(CourseNodePO::getId, modifyCmd));
        if (CollectionUtil.isNotEmpty(courseNodes)) {
            for (CourseNodePO node : courseNodes) {
                if (node.startNode() || node.endNode()) {
                    continue;
                }
                List<Integer> nodeRelates = JSONUtil.toList(node.getRelateNode(), Integer.class);
                nodeRelates.add(id);
                node.setRelateNode(JSONUtil.toJsonStr(nodeRelates));
            }
            courseNodeService.updateBatchById(courseNodes);
        }
    }

    public Boolean moveNode(CourseNodeMoveCmd moveCmd) {
        CourseNodePO courseNode = courseNodeService.getById(moveCmd.getCourseNodeId());
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }
        return courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                .set(StrUtil.isNotBlank(moveCmd.getXAxis()), CourseNodePO::getXAxis, moveCmd.getXAxis())
                .set(StrUtil.isNotBlank(moveCmd.getYAxis()), CourseNodePO::getYAxis, moveCmd.getYAxis())
                .eq(CourseNodePO::getId, moveCmd.getCourseNodeId()));
    }

    public List<CourseNodeDTO> queryNode(Integer courseId) {
        CoursePO course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        List<CourseNodePO> courseNodePOList = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .eq(CourseNodePO::getCourseId, courseId)
                .orderByAsc(CourseNodePO::getId));
        return courseNodePOList.stream()
                .map(CourseNodeDTO::of)
                .toList();
    }

    public Boolean deleteNode(CourseNodeModifyCmd modifyCmd) {
        CourseNodePO courseNode = courseNodeService.getById(modifyCmd.getId());
        String relateNode = courseNode.getRelateNode();
        if (StrUtil.isNotBlank(relateNode)) {
            List<Integer> relateNodeIds = JSONUtil.toList(relateNode, Integer.class);
            deleteRelateNode(relateNodeIds, modifyCmd.getId());
        }
        return courseNodeService.removeById(modifyCmd.getId());
    }

    public List<CourseSimpleDTO> optionalCourseList(String subject) {
        int studentId = StpUtil.getLoginIdAsInt();
        // 获取学生所在学校ID
        StudentPO student = studentService.getById(studentId);
        if (student == null || student.getSchoolId() == null) {
            return new ArrayList<>();
        }
        // 按学校ID查询启用的课程
        List<CoursePO> courses = courseService.enableListBySchoolId(student.getSchoolId(), subject);
        if (CollectionUtil.isEmpty(courses)) {
            return new ArrayList<>();
        }
        List<Integer> courseIds = courses.stream().map(CoursePO::getId).toList();
        Set<Integer> courseIdSet = relationService.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                        .eq(CourseStudentRelationPO::getStudentId, studentId)
                        .in(CourseStudentRelationPO::getCourseId, courseIds))
                .stream()
                .map(CourseStudentRelationPO::getCourseId)
                .collect(Collectors.toSet());
        return courses.stream()
                .map(course -> CourseSimpleDTO.of(course, ossClientUtils::getSignedUrl, courseIdSet::contains))
                .toList();
    }

    /**
     * 获取精品课程列表（学生所在学校的精品课程）
     *
     * @return 精品课程列表
     */
    public List<CourseSimpleDTO> featuredCourseList() {
        int studentId = StpUtil.getLoginIdAsInt();
        // 获取学生所在学校ID
        StudentPO student = studentService.getById(studentId);
        if (student == null || student.getSchoolId() == null) {
            return new ArrayList<>();
        }
        // 查询学生所在学校的精品课程
        List<CoursePO> courses = courseService.list(Wrappers.lambdaQuery(CoursePO.class)
                .eq(CoursePO::getState, true)
                .eq(CoursePO::getSchoolId, student.getSchoolId())
                .eq(CoursePO::getIsFeatured, 1)
                .orderByDesc(CoursePO::getId));
        if (CollectionUtil.isEmpty(courses)) {
            return new ArrayList<>();
        }
        List<Integer> courseIds = courses.stream().map(CoursePO::getId).toList();
        Set<Integer> courseIdSet = relationService.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                        .eq(CourseStudentRelationPO::getStudentId, studentId)
                        .in(CourseStudentRelationPO::getCourseId, courseIds))
                .stream()
                .map(CourseStudentRelationPO::getCourseId)
                .collect(Collectors.toSet());
        return courses.stream()
                .map(course -> CourseSimpleDTO.of(course, ossClientUtils::getSignedUrl, courseIdSet::contains))
                .toList();
    }

    public CourseDetailDTO courseDetail(Integer id, Integer studentId) {
        CoursePO course = courseService.getById(id);
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        CourseDetailDTO courseDetail = BeanUtil.copyProperties(course, CourseDetailDTO.class);
        courseDetail.setPictureUrl(ossClientUtils.getSignedUrl(courseDetail.getPictureUrl()));
        courseDetail.setHeadUrl(ossClientUtils.getSignedUrl(courseDetail.getHeadUrl()));
        courseDetail.setPublishTime(course.getCreateTime());

        List<CourseNodePO> courseNodes = courseNodeService.queryByCourseId(id);
        courseDetail.setNodeSize(courseNodes.size());
        CourseNodePO endCourseNode = courseNodes.stream().filter(item -> Objects.equals("结束", item.getNodeName())).findFirst().orElse(null);
        if (endCourseNode == null) {
            throw new BusinessException(ErrorCodeEnums.LAST_NODE_NOT_EXIST);
        }
        CourseNodePO startCourseNode = courseNodes.stream().filter(item -> Objects.equals("开始", item.getNodeName())).findFirst().orElse(null);
        if (startCourseNode == null) {
            throw new BusinessException(ErrorCodeEnums.FIRST_NODE_NOT_EXIST);
        }
        // 筛选终极节点，所有链接了结束节点的节点
        Set<Integer> lastNodeIds = courseNodes.stream().filter(item -> item.getRelateNode().contains(endCourseNode.getId() + ""))
                .map(CourseNodePO::getId)
                .collect(Collectors.toSet());

        // 考核记录
        List<StudentCourseNodeStudyRecordPO> records = studyRecordService.findByCourseIdAndStudentId(id, studentId);
        List<StudentCourseAssessPO> courseAssessList = studentCourseAssessService.queryCourseAssess(id, studentId);
        Set<Integer> assessedNodeIds = records.stream()
                .filter(item -> Objects.equals(item.getCompleted(), true))
                .map(StudentCourseNodeStudyRecordPO::getCourseNodeId)
                .collect(Collectors.toSet());

        // 考核通过的节点
        Map<Integer, Integer> courseNodePassLineMap = courseNodes.stream().collect(Collectors.toMap(CourseNodePO::getId, CourseNodePO::getPassLine));
        Set<Integer> passedNodeIds = courseAssessList.stream()
                .filter(item -> item.getCourseNodeId() != -1)
                .filter(item -> item.passed(courseNodePassLineMap.get(item.getCourseNodeId())) || item.getNoQuestion())
                .map(StudentCourseAssessPO::getCourseNodeId)
                .collect(Collectors.toSet());

        // 获取可考核的节点,去掉没有考核通过的节点
        courseDetail.setNodes(courseNodes.stream()
                .map(item -> CourseDetailDTO.CourseNodeDTO.of(item,
                        lastNodeIds::contains,
                        ossClientUtils::getSignedUrl,
                        assessedNodeIds::contains))
                .toList());
        // 如果用户的考核节点包含了终极节点，则课程完成
        boolean lastAssess = courseAssessList.stream().anyMatch(item -> Objects.equals(2, item.getAssessMode()));
        if (lastAssess) {
            courseDetail.setState(3);
        } else {
            boolean preLastNode = courseDetail.getNodes().stream()
                    .anyMatch(item -> !Objects.equals("0", item.getNodeColour()) && item.getLast());
            if (preLastNode) {
                courseDetail.setState(2);
            } else {
                courseDetail.setState(1);
            }
        }
        courseDetail.setFinished(courseDetail.getNodes().stream()
                .anyMatch(item -> !Objects.equals("0", item.getNodeColour()) && item.getLast()));
        return courseDetail;
    }

    @NotNull
    private static Set<Integer> getCanAssessNodeIds(List<CourseNodePO> courseNodes,
                                                    List<StudentCourseAssessPO> courseAssessList,
                                                    CourseNodePO startCourseNode,
                                                    Set<Integer> passedNodeIds) {

        // 可以考核的节点（考核过的节点连接的节点或连接了考核节点的节点）
        Set<Integer> canAssessNodeIds = new HashSet<>();
        // 考核过的节点
        List<CourseNodePO> assessedNodes = courseNodes.stream().filter(item -> passedNodeIds.contains(item.getId())).toList();
        for (CourseNodePO assessedNode : assessedNodes) {
            canAssessNodeIds.addAll(JSONUtil.toList(assessedNode.getRelateNode(), Integer.class));
        }
        for (CourseNodePO courseNode : courseNodes) {
            if (Objects.equals("开始", courseNode.getNodeName()) || Objects.equals("结束", courseNode.getNodeName())) {
                continue;
            }
            List<Integer> relateNodeIds = JSONUtil.toList(courseNode.getRelateNode(), Integer.class);
            boolean relateAssessNode = relateNodeIds.stream().anyMatch(canAssessNodeIds::contains);
            if (relateAssessNode) {
                canAssessNodeIds.add(courseNode.getId());
            }
        }
        // 所有关联了开始节点的也是可以考核的
        Set<Integer> relateStartNodeIds = courseNodes.stream().filter(item -> StrUtil.isNotBlank(item.getRelateNode()) && JSONUtil.toList(item.getRelateNode(), Integer.class).contains(startCourseNode.getId()))
                .map(CourseNodePO::getId).collect(Collectors.toSet());
        canAssessNodeIds.addAll(relateStartNodeIds);
        return canAssessNodeIds;
    }

    public Boolean changeState(CourseModifyCmd cmd) {
        CoursePO course = courseService.getById(cmd.getId());
        if (Objects.isNull(course)) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        if (Objects.equals(Boolean.TRUE, cmd.getState())) {
            List<CourseNodePO> courseNodes = courseNodeService.queryByCourseId(cmd.getId());
            if (CollectionUtil.isEmpty(courseNodes)) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
            }
            boolean analysising = courseNodes.stream()
                    .filter(item -> !item.startNode() && !item.endNode())
                    .anyMatch(item -> StrUtil.isBlank(item.getContent()));
            if (analysising) {
                throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_ANALYSIS);
            }
        }
        return courseService.update(Wrappers.lambdaUpdate(CoursePO.class)
                .set(CoursePO::getState, cmd.getState())
                .eq(CoursePO::getId, cmd.getId()));
    }

    public Boolean startStudy(StudentStudyCmd studyCmd, Integer studentId) {
        return studyRecordService.save(StudentCourseNodeStudyRecordPO.builder()
                .studentId(studentId)
                .courseNodeId(studyCmd.getCourseNodeId())
                .build());
    }

    public Boolean endStudy(Integer courseNodeId, Integer studentId, Boolean completed) {
        Optional<StudentCourseNodeStudyRecordPO> recordOp = studyRecordService.findLastRecord(studentId, courseNodeId);
        if (recordOp.isPresent()) {
            StudentCourseNodeStudyRecordPO record = recordOp.get();
            record.setStudyEndTime(LocalDateTime.now());
            record.setCompleted(Objects.equals(Boolean.TRUE, completed));
            return studyRecordService.updateById(record);
        }
        return false;
    }

    /**
     * 获取课程节详情
     *
     * @param nodeId    课程节点ID
     * @param studentId 学生ID
     * @return 课程节详情
     */
    public CourseNodeDetailDTO courseNodeDetail(Integer nodeId, Integer studentId) {
        // 获取课程节点信息
        CourseNodePO courseNode = courseNodeService.getById(nodeId);
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }

        // 获取课程信息
        CoursePO course = courseService.getById(courseNode.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        // 计算学习时长（该节点的所有学习记录的时间差总和，单位分钟）
        List<StudentCourseNodeStudyRecordPO> studyRecords = studyRecordService.findByCourseNodeId(studentId, nodeId);
        int totalStudyTime = studyRecords.stream()
                .mapToInt(StudentCourseNodeStudyRecordPO::getStudyTime)
                .sum();

        // 计算学习进度（学习过的节点数量 / 总节点数量，去掉开始和结束节点）
        List<CourseNodePO> allNodes = courseNodeService.queryByCourseId(course.getId());
        // 过滤掉开始和结束节点
        List<CourseNodePO> validNodes = allNodes.stream()
                .filter(node -> !node.startNode() && !node.endNode())
                .toList();
        int totalNodeCount = validNodes.size();

        // 获取学生的学习记录，计算学习过的节点数
        List<StudentCourseNodeStudyRecordPO> allStudyRecords = studyRecordService.list(Wrappers.lambdaQuery(
                StudentCourseNodeStudyRecordPO.class)
                .eq(StudentCourseNodeStudyRecordPO::getStudentId, studentId)
                .eq(StudentCourseNodeStudyRecordPO::getCompleted, true));
        // 获取学习过的节点ID集合
        Set<Integer> studiedNodeIds = allStudyRecords.stream()
                .map(StudentCourseNodeStudyRecordPO::getCourseNodeId)
                .collect(Collectors.toSet());

        // 计算学习进度百分比
        int studyProgress = totalNodeCount > 0 ? (studiedNodeIds.size() * 100 / totalNodeCount) : 0;

        // 计算 state（是否学过：0-未学过，1-学过）
        Integer state = null;
        if (!courseNode.startNode() && !courseNode.endNode()) {
            // 根据学习记录判断是否学过
            if (CollectionUtils.isNotEmpty(studyRecords)) {
                state = 1;  // 学过
            } else {
                state = 0;  // 未学过
            }
        }

        // 处理视频URL
        FileInfoDTO video = null;
        if (StrUtil.isNotBlank(courseNode.getVideoUrl())) {
            video = JSONUtil.toBean(courseNode.getVideoUrl(), FileInfoDTO.class);
            if (video != null && StrUtil.isNotBlank(video.getUrl())) {
                video.setUrl(ossClientUtils.getSignedUrl(video.getUrl()));
            }
        }

        // 处理文件URL列表
        List<FileInfoDTO> files = new ArrayList<>();
        if (StrUtil.isNotBlank(courseNode.getFilesUrl())) {
            List<FileInfoDTO> fileDTOList = JSONUtil.toList(courseNode.getFilesUrl(), FileInfoDTO.class);
            files = fileDTOList.stream()
                    .map(file -> {
                        FileInfoDTO fileDTO = new FileInfoDTO();
                        fileDTO.setFileName(file.getFileName());
                        fileDTO.setUrl(ossClientUtils.getSignedUrl(file.getUrl()));
                        return fileDTO;
                    })
                    .toList();
        }

        return CourseNodeDetailDTO.builder()
                .id(courseNode.getId())
                .nodeName(courseNode.getNodeName())
                .nodeIntroduce(courseNode.getNodeIntroduce())
                .courseName(course.getCourseName())
                .subject(course.getSubject())
                .studyTime(totalStudyTime)
                .studyProgress(studyProgress)
                .state(state)
                .video(video)
                .files(files)
                .build();
    }

    public Boolean checkCoursePermission(Integer courseId, int studentId) {
        List<CourseStudentRelationPO> relationPOS = relationService.list(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId)
                .eq(CourseStudentRelationPO::getStudentId, studentId));
        return CollectionUtils.isNotEmpty(relationPOS);
    }

    public Boolean inviteCodeStudy(InviteCodeStudyCmd inviteCode, int loginIdAsInt) {
        CoursePO course = courseService.getById(inviteCode.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        if (!Objects.equals(inviteCode.getInviteCode(), course.getInviteCode())) {
            throw new BusinessException(ErrorCodeEnums.INVITE_CODE_ERROR);
        }
        CourseStudentRelationPO relationPO = relationService.getOne(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, inviteCode.getCourseId())
                .eq(CourseStudentRelationPO::getStudentId, loginIdAsInt));
        if (relationPO == null) {
            CourseStudentRelationPO save = CourseStudentRelationPO.builder()
                    .courseId(inviteCode.getCourseId())
                    .studentId(loginIdAsInt)
                    .relationType(0)
                    .build();
            relationService.save(save);
            return true;
        }
        throw new BusinessException(ErrorCodeEnums.RELATION_EXIST);
    }
}
