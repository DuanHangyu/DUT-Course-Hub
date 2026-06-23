package com.human.digital.digitalhuman.controller.api;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.CourseAppService;
import com.human.digital.digitalhuman.service.SchoolCourseAppService;
import com.human.digital.digitalhuman.service.model.request.InviteCodeStudyCmd;
import com.human.digital.digitalhuman.service.model.request.StudentStudyCmd;
import com.human.digital.digitalhuman.service.model.response.CourseDetailDTO;
import com.human.digital.digitalhuman.service.model.response.CourseNodeDetailDTO;
import com.human.digital.digitalhuman.service.model.response.CourseSimpleDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 15:13 2025/6/13
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
@Tag(name = "前台课程管理", description = "前台课程管理")
public class CourseApiController {

    private final CourseAppService courseService;

    private final SchoolCourseAppService schoolCourseAppService;
    /**
     * 学科列表
     *
     * @return 学科列表
     */
    @GetMapping("/subject-list")
    @Operation(summary = "学科列表")
    @OperateLog("学科列表")
    public List<String> subjectList(@RequestParam("schoolId") Integer schoolId) {
        return schoolCourseAppService.subjectList(schoolId);
    }

    @GetMapping("/check-course-permission")
    @Operation(summary = "检查课程权限")
    @OperateLog("检查课程权限")
    public Boolean checkCoursePermission(@RequestParam("courseId") Integer courseId){
        int studentId = StpUtil.getLoginIdAsInt();
        return courseService.checkCoursePermission(courseId, studentId);
    }

    @PostMapping("/invite-code-study")
    @Operation(summary = "通过邀请码学习")
    @OperateLog("通过邀请码学习")
    public Boolean inviteCodeStudy(@RequestBody @Validated InviteCodeStudyCmd inviteCode){
        return courseService.inviteCodeStudy(inviteCode, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/optional-course")
    @Operation(summary = "获取可选课程列表")
    @OperateLog("获取可选课程列表")
    public List<CourseSimpleDTO> optionalCourseList(@RequestParam(value = "subject", required = false) String subject){
        return courseService.optionalCourseList(subject);
    }

    @GetMapping("/featured-course")
    @Operation(summary = "获取精品课程列表")
    @OperateLog("获取精品课程列表")
    public List<CourseSimpleDTO> featuredCourseList(){
        return courseService.featuredCourseList();
    }

    @GetMapping("/course-detail")
    @Operation(summary = "获取课程详情")
    @OperateLog("获取课程详情")
    public CourseDetailDTO courseDetail(@RequestParam("id") Integer id){
        return courseService.courseDetail(id, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/course-node-detail")
    @Operation(summary = "获取课程节详情")
    @OperateLog("获取课程节详情")
    public CourseNodeDetailDTO courseNodeDetail(@RequestParam("nodeId") Integer nodeId){
        return courseService.courseNodeDetail(nodeId, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/start-study")
    @Operation(summary = "开始学习")
    @OperateLog("开始学习")
    public Boolean startStudy(@RequestBody StudentStudyCmd studyCmd){
        return courseService.startStudy(studyCmd, StpUtil.getLoginIdAsInt());
    }

    @PostMapping("/end-study")
    @Operation(summary = "结束学习")
    @OperateLog("结束学习")
    public Boolean endStudy(@RequestParam("courseNodeId") Integer courseNodeId,
                            @RequestParam(value = "studentId", required = false) Integer studentId,
                            @RequestParam(value = "completed", required = false) Boolean completed){
        // 忽略客户端传入的 studentId，强制使用当前登录用户，防止 IDOR
        int currentStudentId = StpUtil.getLoginIdAsInt();
        return courseService.endStudy(courseNodeId, currentStudentId, completed);
    }
}
