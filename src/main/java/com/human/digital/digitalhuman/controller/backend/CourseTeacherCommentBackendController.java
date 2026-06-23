package com.human.digital.digitalhuman.controller.backend;

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
@RequestMapping("/backend/course-student-comment")
@Tag(name = "课程教师提问管理", description = "课程教师提问管理")
public class CourseTeacherCommentBackendController {

    @Resource
    private CourseStudentCommentAppService service;

    @GetMapping("/list")
    @Operation(summary = "获取课程教师提问列表")
    public CommentResponse commentList(@RequestParam("courseId") @Schema(description = "课程id") Integer courseId,
                                       @RequestParam(value = "courseNodeId", required = false) @Schema(description = "课程节点id") Integer courseNodeId,
                                       @RequestParam(value = "after", required = false) @Schema(description = "在哪个评论id之后") Integer after){
        return service.commentList(courseId, courseNodeId, after);
    }

    @GetMapping("/more-reply-list")
    @Operation(summary = "获取课程教师回复列表")
    public ReplyMoreDTO moreReplyList(@RequestParam("rootId") @Schema(description = "一级评论id") Integer rootId,
                                      @RequestParam(value = "after", required = false) @Schema(description = "在哪个评论id之后") Integer after){
        return service.moreReplyList(rootId, after);
    }

    @PostMapping("/save-comment")
    @Operation(summary = "保存课程老师提问")
    public Boolean saveComment(@RequestBody CommentSaveCmd saveCmd){
        return service.teacherSaveComment(saveCmd);
    }

    @PostMapping("/save-reply")
    @Operation(summary = "保存课程老师回复")
    public Boolean saveReply(@RequestBody CommentReplySaveCmd saveCmd){
        return service.teacherSaveReply(saveCmd);
    }

    @PostMapping("/delete-comment")
    @Operation(summary = "删除课程老师提问")
    public Boolean deleteComment(@RequestBody CommentDeleteCmd deleteCmd){
        return service.teacherDeleteComment(deleteCmd);
    }
}
