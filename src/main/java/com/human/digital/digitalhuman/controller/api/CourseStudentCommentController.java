package com.human.digital.digitalhuman.controller.api;

import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.CourseStudentCommentAppService;
import com.human.digital.digitalhuman.service.model.request.CommentDeleteCmd;
import com.human.digital.digitalhuman.service.model.request.CommentReplySaveCmd;
import com.human.digital.digitalhuman.service.model.request.CommentSaveCmd;
import com.human.digital.digitalhuman.service.model.response.CommentResponse;
import com.human.digital.digitalhuman.service.model.response.ReplyMoreDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @USER taoHouChao
 * @DATE 23:23 2025/9/12
 */
@RestController
@RequestMapping("/api/course-student-comment")
@Tag(name = "课程学生提问管理", description = "课程学生提问管理")
public class CourseStudentCommentController {

    @Resource
    private CourseStudentCommentAppService service;

    @GetMapping("/list")
    @Operation(summary = "获取课程学生提问列表")
    @OperateLog("获取课程学生提问列表")
    public CommentResponse commentList(@RequestParam("courseId") Integer courseId,
                                       @RequestParam(value = "courseNodeId", required = false) Integer courseNodeId,
                                       @RequestParam(value = "after", required = false) Integer after){
        return service.commentList(courseId, courseNodeId, after);
    }

    @GetMapping("/more-reply-list")
    @Operation(summary = "获取课程教师回复列表")
    @OperateLog("获取课程教师回复列表")
    public ReplyMoreDTO moreReplyList(@RequestParam("rootId") @Schema(description = "一级评论id") Integer rootId,
                                      @RequestParam(value = "after", required = false) @Schema(description = "在哪个评论id之后") Integer after){
        return service.moreReplyList(rootId, after);
    }

    @PostMapping("/save-comment")
    @Operation(summary = "保存课程学生提问")
    @OperateLog("保存课程学生提问")
    public Boolean saveComment(@RequestBody CommentSaveCmd saveCmd){
        return service.studentSaveComment(saveCmd);
    }

    @PostMapping("/save-reply")
    @Operation(summary = "保存课程学生回复")
    @OperateLog("保存课程学生回复")
    public Boolean saveReply(@RequestBody CommentReplySaveCmd saveCmd){
        return service.studentSaveReply(saveCmd);
    }

    @PostMapping("/delete-comment")
    @Operation(summary = "删除课程学生提问")
    @OperateLog("删除课程学生提问")
    public Boolean deleteComment(@RequestBody CommentDeleteCmd deleteCmd){
        return service.teacherDeleteComment(deleteCmd);
    }
}
