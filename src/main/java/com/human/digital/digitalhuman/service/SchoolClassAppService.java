package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.po.StudentCourseNodeStudyRecordPO;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.SchoolClassService;
import com.human.digital.digitalhuman.repository.service.StudentCourseNodeStudyRecordService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.dto.ClassWithStudentsDTO;
import com.human.digital.digitalhuman.service.model.request.ClassCmd;
import com.human.digital.digitalhuman.service.model.request.ClassQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 班级管理服务（简化版）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Service
@RequiredArgsConstructor
public class SchoolClassAppService {

    private final SchoolClassService schoolClassService;
    private final UserService userService;
    private final StudentService studentService;
    private final StudentCourseNodeStudyRecordService studentCourseNodeStudyRecordService;
    /**
     * 分页查询班级
     *
     * @param query 查询条件
     * @return 分页结果
     */
    public IPage<ClassDTO> pageQuery(ClassQuery query) {
        IPage<SchoolClassPO> page = schoolClassService.pageQuery(
                query.getPage(),
                query.getSize(),
                query.getSchoolId(),
                query.getClassName()
        );

        List<SchoolClassPO> classList = page.getRecords();
        if (classList.isEmpty()) {
            return page.convert(po -> ClassDTO.of(po, null, 0));
        }

        // 批量查询班主任信息，避免N+1
        List<Integer> teacherIds = classList.stream()
                .map(SchoolClassPO::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> teacherNameMap = teacherIds.isEmpty() ? Map.of() :
                userService.listByIds(teacherIds).stream()
                        .collect(Collectors.toMap(UserPO::getId, UserPO::getUserName, (a, b) -> a));

        // 批量查询班级的学生，避免N+1
        List<Integer> classIds = classList.stream().map(SchoolClassPO::getId).toList();
        List<StudentPO> allStudents = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                .in(StudentPO::getClassId, classIds));

        // 按班级ID分组学生
        Map<Integer, List<StudentPO>> studentsByClassId = allStudents.stream()
                .collect(Collectors.groupingBy(StudentPO::getClassId));

        // 批量查询所有学生的学习记录，避免N+1
        List<Integer> studentIds = allStudents.stream().map(StudentPO::getId).toList();
        List<StudentCourseNodeStudyRecordPO> allRecords = studentIds.isEmpty() ? List.of() :
                studentCourseNodeStudyRecordService.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds));

        // 按学生ID分组学习记录
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudentId = allRecords.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        // 组装结果
        return page.convert(po -> {
            String teacherName = teacherNameMap.get(po.getTeacherId());
            List<StudentPO> students = studentsByClassId.getOrDefault(po.getId(), List.of());
            int learningProgress = calculateClassLearningProgress(students, recordsByStudentId);
            return ClassDTO.of(po, teacherName, learningProgress);
        });
    }

    /**
     * 获取班级详情
     *
     * @param id 班级ID
     * @return 班级信息
     */
    public ClassDTO getById(Integer id) {
        SchoolClassPO po = schoolClassService.getById(id);
        if (po == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        String teacherName = null;
        if (po.getTeacherId() != null) {
            UserPO teacher = userService.getById(po.getTeacherId());
            teacherName = teacher != null ? teacher.getUserName() : null;
        }

        // 查询班级的学生和学习记录，计算学习进度
        List<StudentPO> students = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                .eq(StudentPO::getClassId, id));
        List<Integer> studentIds = students.stream().map(StudentPO::getId).toList();
        List<StudentCourseNodeStudyRecordPO> records = studentIds.isEmpty() ? List.of() :
                studentCourseNodeStudyRecordService.list(Wrappers.lambdaQuery(StudentCourseNodeStudyRecordPO.class)
                        .in(StudentCourseNodeStudyRecordPO::getStudentId, studentIds));
        Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudentId = records.stream()
                .collect(Collectors.groupingBy(StudentCourseNodeStudyRecordPO::getStudentId));

        int learningProgress = calculateClassLearningProgress(students, recordsByStudentId);
        return ClassDTO.of(po, teacherName, learningProgress);
    }

    /**
     * 创建班级
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    public Boolean create(ClassCmd cmd) {
        // 检查班级名称唯一性
        SchoolClassPO existClass = schoolClassService.getBySchoolIdAndClassName(cmd.getSchoolId(), cmd.getClassName());
        if (existClass != null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NAME_DUPLICATE);
        }

        // 创建班级
        SchoolClassPO po = new SchoolClassPO();
        po.setSchoolId(cmd.getSchoolId());
        po.setClassName(cmd.getClassName());
        po.setTeacherId(cmd.getTeacherId());
        po.setStudentCount(0);
        schoolClassService.save(po);

        return true;
    }

    /**
     * 更新班级
     *
     * @param cmd 更新命令
     * @return 是否成功
     */
    public Boolean update(ClassCmd cmd) {
        if (cmd.getId() == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        SchoolClassPO existPo = schoolClassService.getById(cmd.getId());
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }

        // 检查班级名称唯一性（排除自己）
        SchoolClassPO existClass = schoolClassService.getBySchoolIdAndClassName(existPo.getSchoolId(), cmd.getClassName());
        if (existClass != null && !existClass.getId().equals(cmd.getId())) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NAME_DUPLICATE);
        }

        // 更新班级
        existPo.setClassName(cmd.getClassName());
        existPo.setTeacherId(cmd.getTeacherId());
        schoolClassService.updateById(existPo);

        return true;
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        SchoolClassPO existPo = schoolClassService.getById(id);
        if (existPo == null) {
            throw new BusinessException(ErrorCodeEnums.CLASS_NOT_EXIST);
        }
        schoolClassService.removeById(id);
        studentService.remove(Wrappers.lambdaQuery(StudentPO.class)
                .eq(StudentPO::getClassId, id));
        return true;
    }

    /**
     * 获取当前用户的学校ID
     *
     * @return 学校ID
     */
    private Integer getCurrentSchoolId() {
        Integer userId = StpUtil.getLoginIdAsInt();
        UserPO user = userService.getById(userId);
        if (user != null && user.getRole() != null && user.getRole() == 0) {
            return null;
        }
        return user != null ? user.getSchoolId() : null;
    }

    /**
     * 查询学校下所有班级及关联的学生列表
     *
     * @param schoolId 学校ID
     * @return 班级及学生列表
     */
    public List<ClassWithStudentsDTO> listWithStudents(Integer schoolId) {
        if (schoolId == null) {
            return new ArrayList<>();
        }
        // 查询学校下所有班级
        List<SchoolClassPO> classes = schoolClassService.list(Wrappers.lambdaQuery(SchoolClassPO.class)
                .eq(SchoolClassPO::getSchoolId, schoolId)
                .orderByAsc(SchoolClassPO::getId));

        if (classes.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有班级ID
        List<Integer> classIds = classes.stream().map(SchoolClassPO::getId).toList();

        // 批量查询所有班级的学生
        List<StudentPO> allStudents = studentService.list(Wrappers.lambdaQuery(StudentPO.class)
                .in(StudentPO::getClassId, classIds));

        // 按班级ID分组学生
        Map<Integer, List<StudentPO>> studentsByClassId = allStudents.stream()
                .collect(Collectors.groupingBy(StudentPO::getClassId));

        // 获取所有班主任ID
        List<Integer> teacherIds = classes.stream()
                .map(SchoolClassPO::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 批量查询班主任信息
        Map<Integer, String> teacherNameMap;
        if (!teacherIds.isEmpty()) {
            List<UserPO> teachers = userService.listByIds(teacherIds);
            teacherNameMap = teachers.stream()
                    .collect(Collectors.toMap(UserPO::getId, UserPO::getUserName, (a, b) -> a));
        } else {
            teacherNameMap = Map.of();
        }

        // 组装返回结果
        return classes.stream()
                .map(c -> ClassWithStudentsDTO.of(c, teacherNameMap.get(c.getTeacherId()), studentsByClassId.get(c.getId())))
                .toList();
    }

    /**
     * 计算班级学习进度（百分比）
     * 学习进度 = 班级所有学生完成率的平均值
     *
     * @param students            班级学生列表
     * @param recordsByStudentId  学生ID到学习记录的映射
     * @return 学习进度（0-100）
     */
    private int calculateClassLearningProgress(List<StudentPO> students,
                                                Map<Integer, List<StudentCourseNodeStudyRecordPO>> recordsByStudentId) {
        if (students.isEmpty()) {
            return 0;
        }

        double totalRate = 0;
        for (StudentPO student : students) {
            List<StudentCourseNodeStudyRecordPO> records = recordsByStudentId.getOrDefault(student.getId(), List.of());
            totalRate += calculateStudentCompletionRate(records);
        }

        return (int) Math.round(totalRate / students.size());
    }

    /**
     * 计算学生课程完成率
     *
     * @param records 学习记录列表
     * @return 完成率（0-100）
     */
    private double calculateStudentCompletionRate(List<StudentCourseNodeStudyRecordPO> records) {
        if (records.isEmpty()) {
            return 0.0;
        }
        // 完成的学习记录数/总记录数 * 100，上限100
        long completedCount = records.stream()
                .filter(r -> Boolean.TRUE.equals(r.getCompleted()))
                .count();
        return Math.min(completedCount * 10, 100);
    }
}
