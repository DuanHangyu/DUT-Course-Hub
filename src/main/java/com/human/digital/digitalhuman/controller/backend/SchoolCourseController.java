package com.human.digital.digitalhuman.controller.backend;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.human.digital.digitalhuman.repository.mapper.CourseNodeMapper;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.service.SchoolClassAppService;
import com.human.digital.digitalhuman.service.SchoolCourseAppService;
import com.human.digital.digitalhuman.service.SchoolStudentAppService;
import com.human.digital.digitalhuman.service.SchoolTeacherAppService;
import com.human.digital.digitalhuman.service.handler.FileIdentifyHandler;
import com.human.digital.digitalhuman.service.model.dto.ClassDTO;
import com.human.digital.digitalhuman.service.model.dto.ClassWithStudentsDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentBackendDTO;
import com.human.digital.digitalhuman.service.model.dto.StudentImportResultDTO;
import com.human.digital.digitalhuman.service.model.event.NodeModifyEvent;
import com.human.digital.digitalhuman.service.model.request.*;
import com.human.digital.digitalhuman.service.model.response.CourseStatsDTO;
import com.human.digital.digitalhuman.service.model.response.SchoolCourseDTO;
import com.human.digital.digitalhuman.service.model.response.TeacherSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 校本课程控制器
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@RestController
@RequestMapping("/api/school")
@Tag(name = "校本课程管理", description = "校本课程管理接口")
@Validated
public class SchoolCourseController {

    @Resource
    private SchoolCourseAppService schoolCourseAppService;

    @Resource
    private CourseNodeMapper courseNodeMapper;

    // 班级管理服务
    @Resource
    private SchoolClassAppService schoolClassAppService;

    // 学生管理服务
    @Resource
    private SchoolStudentAppService schoolStudentAppService;

    // 教师管理服务
    @Resource
    private SchoolTeacherAppService schoolTeacherAppService;

    @Resource
    private FileIdentifyHandler fileIdentifyHandler;

    // ==================== 课程管理接口 ====================

    /**
     * 课程列表
     *
     * @param query 查询条件
     * @return 课程分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "课程列表")
    public IPage<SchoolCourseDTO> list(SchoolCourseQuery query) {
        int userId = StpUtil.getLoginIdAsInt();
        return schoolCourseAppService.pageQuery(userId, query);
    }

    /**
     * 创建课程
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @PostMapping("/create")
    @Operation(summary = "创建课程")
    public Boolean create(@RequestBody @Valid SchoolCourseCmd cmd) {
        return schoolCourseAppService.create(cmd);
    }

    /**
     * 编辑课程
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @PutMapping("/update")
    @Operation(summary = "编辑课程")
    public Boolean update(@RequestBody @Valid SchoolCourseCmd cmd) {
        return schoolCourseAppService.update(cmd);
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除课程")
    public Boolean delete(@PathVariable("id") Integer id) {
        return schoolCourseAppService.delete(id);
    }

    /**
     * 课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "课程详情")
    public SchoolCourseDTO detail(@PathVariable("id") Integer id) {
        return schoolCourseAppService.detail(id);
    }

    /**
     * 设置/取消精选
     *
     * @param cmd 精选命令
     * @return 是否成功
     */
    @PutMapping("/featured")
    @Operation(summary = "设置/取消精选")
    public Boolean setFeatured(@RequestBody @Valid CourseFeaturedCmd cmd) {
        return schoolCourseAppService.setFeatured(cmd);
    }

    @PostMapping("/addRelationStudent")
    @Operation(summary = "添加关联学生")
    public Boolean addRelationStudent(@RequestBody @Valid AddRelationStudentCmd cmd) {
        return schoolCourseAppService.addRelationStudent(cmd);
    }

    /**
     * 添加学生
     *
     * @param cmd 添加学生命令
     * @return 是否成功
     */
    @PostMapping("/addStudents")
    @Operation(summary = "添加学生")
    @Deprecated
    public Boolean addStudents(@RequestBody @Valid AddStudentsCmd cmd) {
        return schoolCourseAppService.addStudents(cmd);
    }

    /**
     * 添加班级
     *
     * @param cmd 添加班级命令
     * @return 是否成功
     */
    @PostMapping("/addClasses")
    @Operation(summary = "添加班级")
    @Deprecated
    public Boolean addClasses(@RequestBody @Valid AddClassesCmd cmd) {
        return schoolCourseAppService.addClasses(cmd);
    }

    /**
     * 生成邀请码
     *
     * @return 邀请码
     */
    @PostMapping("/generateCode")
    @Operation(summary = "生成邀请码")
    public String generateCode() {
        return schoolCourseAppService.generateInviteCode();
    }

    /**
     * 课程统计
     *
     * @param schoolId 学校ID
     * @return 统计结果
     */
    @GetMapping("/stats")
    @Operation(summary = "课程统计")
    public CourseStatsDTO stats(@RequestParam(value = "schoolId", required = false) Integer schoolId) {
        return schoolCourseAppService.getStats(schoolId);
    }

    /**
     * 学科列表
     *
     * @return 学科列表
     */
    @GetMapping("/subject-list")
    @Operation(summary = "学科列表")
    public List<String> subjectList(@RequestParam("schoolId") Integer schoolId) {
        return schoolCourseAppService.subjectList(schoolId);
    }

    // ==================== 班级管理接口 ====================

    /**
     * 班级列表
     *
     * @param query 查询条件
     * @return 班级分页列表
     */
    @PostMapping("/class/list")
    @Operation(summary = "班级列表")
    public IPage<ClassDTO> classList(@RequestBody ClassQuery query) {
        return schoolClassAppService.pageQuery(query);
    }

    /**
     * 创建班级
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @PostMapping("/class/create")
    @Operation(summary = "创建班级")
    public Boolean createClass(@RequestBody @Valid ClassCmd cmd) {
        return schoolClassAppService.create(cmd);
    }

    /**
     * 编辑班级
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @PutMapping("/class/update")
    @Operation(summary = "编辑班级")
    public Boolean updateClass(@RequestBody @Valid ClassCmd cmd) {
        return schoolClassAppService.update(cmd);
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return 是否成功
     */
    @DeleteMapping("/class/{id}")
    @Operation(summary = "删除班级")
    public Boolean deleteClass(@PathVariable("id") Integer id) {
        return schoolClassAppService.delete(id);
    }

    /**
     * 查询学校下所有班级及关联学生
     *
     * @param schoolId 学校ID
     * @return 班级及学生列表
     */
    @GetMapping("/class/all-with-students")
    @Operation(summary = "查询班级及学生列表")
    public List<ClassWithStudentsDTO> listClassWithStudents(@RequestParam("schoolId") Integer schoolId) {
        return schoolClassAppService.listWithStudents(schoolId);
    }

    // ==================== 学生管理接口 ====================

    /**
     * 学生列表
     *
     * @param query 查询条件
     * @return 学生分页列表
     */
    @PostMapping("/student/list")
    @Operation(summary = "学生列表")
    public IPage<StudentBackendDTO> studentList(@RequestBody StudentQuery query) {
        return schoolStudentAppService.pageQuery(query);
    }

    /**
     * 创建学生
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @PostMapping("/student/create")
    @Operation(summary = "创建学生")
    public Boolean createStudent(@RequestBody @Valid StudentCmd cmd) {
        return schoolStudentAppService.create(cmd);
    }

    /**
     * 编辑学生
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @PutMapping("/student/update")
    @Operation(summary = "编辑学生")
    public Boolean updateStudent(@RequestBody @Valid StudentCmd cmd) {
        return schoolStudentAppService.update(cmd);
    }

    /**
     * 删除学生
     *
     * @param id 学生ID
     * @return 是否成功
     */
    @DeleteMapping("/student/{id}")
    @Operation(summary = "删除学生")
    public Boolean deleteStudent(@PathVariable("id") Integer id) {
        return schoolStudentAppService.delete(id);
    }

    /**
     * 导出学生导入模板
     *
     * @param schoolId 学校ID
     * @throws IOException IO异常
     */
    @GetMapping("/student/template")
    @Operation(summary = "导出学生导入模板")
    public void exportStudentTemplate(@RequestParam("schoolId") Integer schoolId) throws IOException {
        schoolStudentAppService.exportTemplate(schoolId);
    }

    /**
     * 导入学生
     *
     * @param schoolId 学校ID
     * @param file     Excel文件
     * @return 导入结果
     * @throws IOException IO异常
     */
    @PostMapping("/student/import")
    @Operation(summary = "导入学生")
    public StudentImportResultDTO importStudents(
            @RequestParam("schoolId") Integer schoolId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return schoolStudentAppService.importStudents(schoolId, file);
    }

    /**
     * 导出学生
     *
     * @param query 查询条件
     * @throws IOException IO异常
     */
    @PostMapping("/student/export")
    @Operation(summary = "导出学生")
    public void exportStudents(@RequestBody StudentQuery query) throws IOException {
        schoolStudentAppService.exportStudents(query);
    }

    // ==================== 教师管理接口 ====================

    /**
     * 教师列表
     *
     * @param query 查询条件
     * @return 教师分页列表
     */
    @PostMapping("/teacher/list")
    @Operation(summary = "教师列表")
    public IPage<TeacherSummaryDTO> teacherList(@RequestBody TeacherListQuery query) {
        return schoolTeacherAppService.pageQuery(query);
    }

    /**
     * 创建教师
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @PostMapping("/teacher/create")
    @Operation(summary = "创建教师")
    public Boolean createTeacher(@RequestBody @Valid TeacherCreateCmd cmd) {
        return schoolTeacherAppService.create(cmd);
    }

    /**
     * 编辑教师
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @PutMapping("/teacher/update")
    @Operation(summary = "编辑教师")
    public Boolean updateTeacher(@RequestBody @Valid TeacherModifyCmd cmd) {
        return schoolTeacherAppService.update(cmd);
    }

    /**
     * 修改教师状态
     *
     * @param cmd 修改命令
     * @return 是否成功
     */
    @PutMapping("/teacher/changeState")
    @Operation(summary = "修改教师状态")
    public Boolean changeState(@RequestBody @Valid TeacherStateChangeCmd cmd) {
        return schoolTeacherAppService.changeState(cmd);
    }

    /**
     * 删除教师
     *
     * @param id 教师ID
     * @return 是否成功
     */
    @DeleteMapping("/teacher/{id}")
    @Operation(summary = "删除教师")
    public Boolean deleteTeacher(@PathVariable("id") Integer id) {
        return schoolTeacherAppService.delete(id);
    }

    @GetMapping("/syncCourseNodeContent")
    @Operation(summary = "同步课程节点内容")
    public void syncCourseNodeContent(@RequestParam("courseNodeId") Integer courseNodeId){
        CourseNodePO courseNodePO = courseNodeMapper.selectById(courseNodeId);
        NodeModifyEvent nodeModifyEvent = new NodeModifyEvent(this, courseNodePO);
        fileIdentifyHandler.syncCourseNodeContent(nodeModifyEvent);
    }
}
