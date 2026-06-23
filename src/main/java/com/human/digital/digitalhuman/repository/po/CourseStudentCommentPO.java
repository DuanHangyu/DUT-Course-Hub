package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 23:16 2025/9/12
 */
@Data
@TableName("course_student_comment")
@Schema(description = "课程学生评论表")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseStudentCommentPO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "课程id")
    private Integer courseId;

    @Schema(description = "课程节点id")
    private Integer courseNodeId;

    @Schema(description = "用户类型 0学生 1老师")
    private Integer userType;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    private Integer parentId;

    @Schema(description = "根评论ID")
    private Integer rootId;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "文件")
    private String files;

    @Schema(description = "图片")
    private String pictures;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
