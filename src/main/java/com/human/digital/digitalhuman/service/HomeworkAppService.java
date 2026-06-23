package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.*;
import com.human.digital.digitalhuman.repository.service.*;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import com.human.digital.digitalhuman.service.model.request.HomeworkCreateCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkDeleteCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkGradeCmd;
import com.human.digital.digitalhuman.service.model.request.HomeworkModifyCmd;
import com.human.digital.digitalhuman.service.model.response.HomeworkFileDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkListDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmitDetailDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmissionListDTO;
import com.human.digital.digitalhuman.service.model.response.HomeworkSubmissionStatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 作业管理业务逻辑（教师端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HomeworkAppService {

    private final HomeworkService homeworkService;
    private final HomeworkSubmitService homeworkSubmitService;
    private final CourseService courseService;
    private final CourseStudentRelationService courseStudentRelationService;
    private final StudentService studentService;
    private final OssClientUtils ossClientUtils;
    private final SchoolService schoolService;
    private final SchoolClassService schoolClassService;

    /**
     * 创建作业
     *
     * @param cmd 创建请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(HomeworkCreateCmd cmd) {
        // 获取当前登录教师ID
        Integer teacherId = StpUtil.getLoginIdAsInt();

        // 校验课程是否存在
        CoursePO course = courseService.getById(cmd.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        // 校验截止时间需晚于当前时间
        if (cmd.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_DEADLINE_PASSED);
        }

        // 创建作业
        HomeworkPO homework = cmd.toPo(teacherId);
        if (CollectionUtil.isNotEmpty(cmd.getAttachments())) {
            homework.setAttachments(JSONUtil.toJsonStr(cmd.getAttachments()));
        }
        homeworkService.save(homework);

        log.info("创建作业成功，作业ID: {}, 教师ID: {}", homework.getId(), teacherId);
        return true;
    }

    /**
     * 修改作业
     *
     * @param cmd 修改请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean modify(HomeworkModifyCmd cmd) {
        // 获取当前登录教师ID
        Integer teacherId = StpUtil.getLoginIdAsInt();

        // 校验作业是否存在
        HomeworkPO homework = homeworkService.getById(cmd.getId());
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        // 校验操作权限
        if (!Objects.equals(homework.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 已截止的作业不可修改截止时间
        if (homework.getDeadline().isBefore(LocalDateTime.now()) && cmd.getDeadline() != null) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_DEADLINE_PASSED);
        }

        // 更新作业信息
        homeworkService.update(Wrappers.lambdaUpdate(HomeworkPO.class)
                .set(StrUtil.isNotBlank(cmd.getTitle()), HomeworkPO::getTitle, cmd.getTitle())
                .set(StrUtil.isNotBlank(cmd.getDescription()), HomeworkPO::getDescription, cmd.getDescription())
                .set(cmd.getDeadline() != null, HomeworkPO::getDeadline, cmd.getDeadline())
                .set(cmd.getAttachments() != null, HomeworkPO::getAttachments,
                        cmd.getAttachments() != null ? JSONUtil.toJsonStr(cmd.getAttachments()) : null)
                .eq(HomeworkPO::getId, cmd.getId()));

        log.info("修改作业成功，作业ID: {}", cmd.getId());
        return true;
    }

    /**
     * 删除作业
     *
     * @param cmd 删除请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(HomeworkDeleteCmd cmd) {
        // 获取当前登录教师ID
        Integer teacherId = StpUtil.getLoginIdAsInt();

        // 校验作业是否存在
        HomeworkPO homework = homeworkService.getById(cmd.getId());
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        // 校验操作权限
        if (!Objects.equals(homework.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 逻辑删除
        homeworkService.update(Wrappers.lambdaUpdate(HomeworkPO.class)
                .set(HomeworkPO::getDeleted, 1)
                .eq(HomeworkPO::getId, cmd.getId()));

        log.info("删除作业成功，作业ID: {}", cmd.getId());
        return true;
    }

    /**
     * 查询作业列表
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    public List<HomeworkListDTO> list(Integer courseId) {
        // 获取当前登录教师ID
        Integer teacherId = StpUtil.getLoginIdAsInt();

        // 校验课程是否存在
        CoursePO course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        // 查询课程下的作业列表
        List<HomeworkPO> homeworkList = homeworkService.listByCourseId(courseId);

        if (CollectionUtil.isEmpty(homeworkList)) {
            return new ArrayList<>();
        }

        // 获取课程关联的学生总数
        long totalCount = courseStudentRelationService.count(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, courseId));

        // 获取每个作业的提交人数
        List<Integer> homeworkIds = homeworkList.stream().map(HomeworkPO::getId).toList();
        List<HomeworkSubmitPO> submitList = homeworkSubmitService.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                .in(HomeworkSubmitPO::getHomeworkId, homeworkIds)
                .eq(HomeworkSubmitPO::getDeleted, 0));

        // 按作业ID分组统计提交人数
        Map<Integer, Long> submitCountMap = submitList.stream()
                .collect(Collectors.groupingBy(HomeworkSubmitPO::getHomeworkId, Collectors.counting()));

        // 转换为 DTO
        return homeworkList.stream()
                .filter(h -> Objects.equals(h.getTeacherId(), teacherId))
                .map(h -> {
                    HomeworkListDTO dto = new HomeworkListDTO();
                    dto.setId(h.getId());
                    dto.setTitle(h.getTitle());
                    dto.setDescription(h.getDescription());
                    dto.setDeadline(h.getDeadline());
                    dto.setCreateTime(h.getCreateTime());
                    dto.setTotalCount((int) totalCount);
                    dto.setSubmittedCount(submitCountMap.getOrDefault(h.getId(), 0L).intValue());

                    // 解析附件
                    if (StrUtil.isNotBlank(h.getAttachments())) {
                        List<UploadDTO> attachments = JSONUtil.toList(h.getAttachments(), UploadDTO.class);
                        dto.setAttachments(attachments.stream()
                                .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                                .toList());
                    }

                    return dto;
                })
                .toList();
    }

    /**
     * 查询作业提交详情
     *
     * @param homeworkId 作业ID
     * @return 提交详情列表
     */
    public List<HomeworkSubmitDetailDTO> submitDetail(Integer homeworkId) {
        // 获取当前登录教师ID
        Integer teacherId = StpUtil.getLoginIdAsInt();

        // 校验作业是否存在
        HomeworkPO homework = homeworkService.getById(homeworkId);
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        // 校验操作权限
        if (!Objects.equals(homework.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        // 查询课程关联的所有学生
        List<CourseStudentRelationPO> relations = courseStudentRelationService.list(
                Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                        .eq(CourseStudentRelationPO::getCourseId, homework.getCourseId()));

        if (CollectionUtil.isEmpty(relations)) {
            return new ArrayList<>();
        }

        // 获取学生ID列表
        List<Integer> studentIds = relations.stream()
                .map(CourseStudentRelationPO::getStudentId)
                .distinct()
                .toList();

        // 查询学生信息
        List<StudentPO> students = studentService.listByIds(studentIds);
        Map<Integer, StudentPO> studentMap = students.stream()
                .collect(Collectors.toMap(StudentPO::getId, s -> s));

        // 查询提交记录
        List<HomeworkSubmitPO> submitList = homeworkSubmitService.listByHomeworkId(homeworkId);
        Map<Integer, HomeworkSubmitPO> submitMap = submitList.stream()
                .collect(Collectors.toMap(HomeworkSubmitPO::getStudentId, s -> s));

        // 构建返回结果
        return studentIds.stream()
                .map(studentId -> {
                    HomeworkSubmitDetailDTO dto = new HomeworkSubmitDetailDTO();
                    dto.setStudentId(studentId);

                    StudentPO student = studentMap.get(studentId);
                    if (student != null) {
                        dto.setStudentName(student.getStudentName());
                        // StudentPO 没有 studentNo 字段，暂不设置
                    }

                    HomeworkSubmitPO submit = submitMap.get(studentId);
                    if (submit != null) {
                        dto.setSubmitStatus(1);
                        dto.setSubmitTime(submit.getSubmitTime());
                        if (StrUtil.isNotBlank(submit.getFiles())) {
                            List<UploadDTO> files = JSONUtil.toList(submit.getFiles(), UploadDTO.class);
                            dto.setFiles(files.stream()
                                    .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                                    .toList());
                        }
                    } else {
                        dto.setSubmitStatus(0);
                    }

                    return dto;
                })
                .toList();
    }

    /**
     * 获取作业提交统计
     *
     * @param homeworkId 作业ID
     * @return 提交统计
     */
    public HomeworkSubmissionStatisticsDTO submissionStatistics(Integer homeworkId) {
        // 校验作业存在性和权限
        HomeworkPO homework = validateHomeworkPermission(homeworkId);

        // 获取课程关联的学生总数
        long totalCount = courseStudentRelationService.count(Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                .eq(CourseStudentRelationPO::getCourseId, homework.getCourseId()));

        // 获取提交记录
        List<HomeworkSubmitPO> submitList = homeworkSubmitService.listByHomeworkId(homeworkId);

        // 统计已提交数量（submit_time IS NOT NULL）
        long submittedCount = submitList.stream()
                .filter(s -> s.getSubmitTime() != null)
                .count();

        // 统计已批改数量（score IS NOT NULL）
        long gradedCount = submitList.stream()
                .filter(s -> s.getScore() != null)
                .count();

        // 构建响应
        HomeworkSubmissionStatisticsDTO dto = new HomeworkSubmissionStatisticsDTO();
        dto.setTotalCount((int) totalCount);
        dto.setSubmittedCount((int) submittedCount);
        dto.setUnsubmittedCount((int) (totalCount - submittedCount));
        dto.setGradedCount((int) gradedCount);

        return dto;
    }

    /**
     * 获取作业关联的班级列表
     *
     * @param homeworkId 作业ID
     * @return 班级列表
     */
    public List<ClassDTO> getClassesByHomeworkId(Integer homeworkId) {
        // 校验作业存在性和权限
        HomeworkPO homework = validateHomeworkPermission(homeworkId);

        // 查询课程关联的所有学生
        List<CourseStudentRelationPO> relations = courseStudentRelationService.list(
                Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                        .eq(CourseStudentRelationPO::getCourseId, homework.getCourseId()));

        if (CollectionUtil.isEmpty(relations)) {
            return new ArrayList<>();
        }

        // 获取学生ID列表
        List<Integer> studentIds = relations.stream()
                .map(CourseStudentRelationPO::getStudentId)
                .distinct()
                .toList();

        // 查询学生信息，获取班级ID列表
        List<StudentPO> students = studentService.listByIds(studentIds);
        List<Integer> classIds = students.stream()
                .map(StudentPO::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (CollectionUtil.isEmpty(classIds)) {
            return new ArrayList<>();
        }

        // 查询班级信息
        List<SchoolClassPO> schoolClasses = schoolClassService.listByIds(classIds);
        return schoolClasses.stream()
                .map(sc -> ClassDTO.of(sc, null))
                .toList();
    }

    /**
     * 校验作业存在性和权限
     *
     * @param homeworkId 作业ID
     * @return 作业实体
     */
    private HomeworkPO validateHomeworkPermission(Integer homeworkId) {
        Integer teacherId = StpUtil.getLoginIdAsInt();

        HomeworkPO homework = homeworkService.getById(homeworkId);
        if (homework == null || Objects.equals(homework.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_EXIST);
        }

        if (!Objects.equals(homework.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NO_PERMISSION);
        }

        return homework;
    }

    /**
     * 获取作业提交列表
     *
     * @param homeworkId 作业ID
     * @param name        学生姓名（可选）
     * @param studentNo   学号（可选，模糊搜索）
     * @param classId     班级ID（可选）
     * @param status      提交状态（可选）
     * @return 提交列表
     */
    public List<HomeworkSubmissionListDTO> submissionList(Integer homeworkId, String name, String studentNo, Integer classId, String status) {
        // 校验作业存在性和权限
        HomeworkPO homework = validateHomeworkPermission(homeworkId);

        // 查询课程关联的所有学生
        List<CourseStudentRelationPO> relations = courseStudentRelationService.list(
                Wrappers.lambdaQuery(CourseStudentRelationPO.class)
                        .eq(CourseStudentRelationPO::getCourseId, homework.getCourseId()));

        if (CollectionUtil.isEmpty(relations)) {
            return new ArrayList<>();
        }

        // 获取学生ID列表
        List<Integer> studentIds = relations.stream()
                .map(CourseStudentRelationPO::getStudentId)
                .distinct()
                .toList();

        // 查询学生信息
        List<StudentPO> students = studentService.listByIds(studentIds);

        // 根据姓名、学号、班级进行过滤
        if (StringUtils.isNotBlank(name) || StringUtils.isNotBlank(studentNo) || classId != null) {
            students = students.stream()
                    .filter(s -> {
                        // 姓名模糊匹配
                        if (StringUtils.isNotBlank(name)) {
                            if (s.getStudentName() == null || !s.getStudentName().contains(name)) {
                                return false;
                            }
                        }
                        // 学号模糊匹配
                        if (StringUtils.isNotBlank(studentNo)) {
                            if (s.getIdNumber() == null || !s.getIdNumber().contains(studentNo)) {
                                return false;
                            }
                        }
                        // 班级ID精确匹配
                        if (classId != null) {
                            if (s.getClassId() == null || !s.getClassId().equals(classId)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .toList();
            // 更新学生ID列表
            studentIds = students.stream()
                    .map(StudentPO::getId)
                    .toList();
        }

        Map<Integer, StudentPO> studentMap = students.stream()
                .collect(Collectors.toMap(StudentPO::getId, s -> s));

        List<Integer> schoolIds = students.stream()
                .map(StudentPO::getSchoolId)
                .filter(Objects::nonNull)
                .toList();
        List<SchoolClassPO> schoolClassPOS = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(schoolIds)) {
            schoolClassPOS = schoolClassService.listByIds(schoolIds);
        }
        Map<Integer, SchoolClassPO> shoolClassIdMap = schoolClassPOS.stream().collect(Collectors.toMap(SchoolClassPO::getId, s -> s));

        // 查询提交记录
        List<HomeworkSubmitPO> submitList = homeworkSubmitService.listByHomeworkId(homeworkId);
        Map<Integer, HomeworkSubmitPO> submitMap = submitList.stream()
                .collect(Collectors.toMap(HomeworkSubmitPO::getStudentId, s -> s));

        // 构建返回结果
        return studentIds.stream()
                .map(studentId -> {
                    StudentPO student = studentMap.get(studentId);
                    HomeworkSubmitPO submit = submitMap.get(studentId);

                    HomeworkSubmissionListDTO dto = new HomeworkSubmissionListDTO();
                    dto.setStudentId(studentId);

                    if (student != null) {
                        dto.setStudentName(student.getStudentName());
                        dto.setStudentNo(student.getIdNumber());
                        if (Objects.nonNull(student.getSchoolId())) {
                            SchoolClassPO schoolClassPO = shoolClassIdMap.get(student.getSchoolId());
                            if (Objects.nonNull(schoolClassPO)) {
                                dto.setClassName(schoolClassPO.getClassName());
                            }
                        }
                    }

                    // 设置状态和成绩
                    if (submit == null || submit.getSubmitTime() == null) {
                        dto.setStatus("UNSUBMITTED");
                    } else if (submit.getScore() != null) {
                        dto.setStatus("GRADED");
                        dto.setScore(submit.getScore());
                        dto.setComment(submit.getComment());
                        dto.setSubmitTime(submit.getSubmitTime());
                        String files = submit.getFiles();
                        if (StringUtils.isNotBlank(files)) {
                            List<UploadDTO> uploads = JSONUtil.toList(files, UploadDTO.class);
                            dto.setFiles(uploads.stream()
                                    .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                                    .toList());
                        }
                        dto.setId(submit.getId());
                    } else {
                        dto.setStatus("SUBMITTED");
                        dto.setSubmitTime(submit.getSubmitTime());
                        dto.setId(submit.getId());
                        String files = submit.getFiles();
                        if (StringUtils.isNotBlank(files)) {
                            List<UploadDTO> uploads = JSONUtil.toList(files, UploadDTO.class);
                            dto.setFiles(uploads.stream()
                                    .map(item -> HomeworkFileDTO.from(item, ossClientUtils::getSignedUrl))
                                    .toList());
                        }
                    }

                    return dto;
                })
                .filter(dto -> filterByCondition(dto, name, status))
                .toList();
    }

    /**
     * 条件筛选
     */
    private boolean filterByCondition(HomeworkSubmissionListDTO dto, String name, String status) {
        // 姓名筛选（模糊匹配）
        if (StrUtil.isNotBlank(name) && !dto.getStudentName().contains(name)) {
            return false;
        }

        // 状态筛选
        if (StrUtil.isNotBlank(status) && !status.equals(dto.getStatus())) {
            return false;
        }

        return true;
    }

    /**
     * 批改作业
     *
     * @param cmd 批改请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean grade(HomeworkGradeCmd cmd) {
        // 校验提交记录存在
        HomeworkSubmitPO submit = homeworkSubmitService.getById(cmd.getSubmissionId());
        if (submit == null || Objects.equals(submit.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_SUBMIT_NOT_EXIST);
        }

        // 校验作业权限
        validateHomeworkPermission(submit.getHomeworkId());

        // 校验已提交
        if (submit.getSubmitTime() == null) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_SUBMITTED);
        }

        // 更新批改信息
        Integer graderId = StpUtil.getLoginIdAsInt();
        homeworkSubmitService.update(Wrappers.lambdaUpdate(HomeworkSubmitPO.class)
                .set(HomeworkSubmitPO::getScore, cmd.getScore())
                .set(HomeworkSubmitPO::getComment, cmd.getComment())
                .set(HomeworkSubmitPO::getGraderId, graderId)
                .set(HomeworkSubmitPO::getGradeTime, LocalDateTime.now())
                .eq(HomeworkSubmitPO::getId, cmd.getSubmissionId()));

        log.info("批改作业成功，提交记录ID: {}, 批改人ID: {}", cmd.getSubmissionId(), graderId);
        return true;
    }

    /**
     * 重新批改作业
     *
     * @param cmd 批改请求
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean regrade(HomeworkGradeCmd cmd) {
        // 校验提交记录存在
        HomeworkSubmitPO submit = homeworkSubmitService.getById(cmd.getSubmissionId());
        if (submit == null || Objects.equals(submit.getDeleted(), 1)) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_SUBMIT_NOT_EXIST);
        }

        // 校验作业权限
        validateHomeworkPermission(submit.getHomeworkId());

        // 校验已批改过
        if (submit.getScore() == null) {
            throw new BusinessException(ErrorCodeEnums.HOMEWORK_NOT_SUBMITTED);
        }

        // 更新批改信息
        Integer graderId = StpUtil.getLoginIdAsInt();
        homeworkSubmitService.update(Wrappers.lambdaUpdate(HomeworkSubmitPO.class)
                .set(HomeworkSubmitPO::getScore, cmd.getScore())
                .set(HomeworkSubmitPO::getComment, cmd.getComment())
                .set(HomeworkSubmitPO::getGraderId, graderId)
                .set(HomeworkSubmitPO::getGradeTime, LocalDateTime.now())
                .eq(HomeworkSubmitPO::getId, cmd.getSubmissionId()));

        log.info("重新批改作业成功，提交记录ID: {}, 批改人ID: {}", cmd.getSubmissionId(), graderId);
        return true;
    }
}
