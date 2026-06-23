package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author taoHouChao
 * @Date 22:29 2025/6/11
 */
@Data
public class CourseSummaryDTO {

    private Integer id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程介绍")
    private String courseIntroduce;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "课程图片")
    private String pictureUrl;

    @Schema(description = "教师名称")
    private String teacherName;

    @Schema(description = "教师头像")
    private String headUrl;

    @Schema(description = "创建人")
    private String createName;

    @Schema(description = "创建人id")
    private Integer creatorId;

    @Schema(description = "终极答辩题目数量")
    private Integer lastDefenseQuestionsCount;

    @Schema(description = "关系类型 0关联学生 1关联班级 2都关联")
    private Integer relationType;

    @Schema(description = "邀请教师id集合")
    private List<UserSummaryDTO> inviteTeachers;

    @Schema(description = "班级名称集合")
    private List<String> schoolClassList;

    @Schema(description = "学生id集合")
    private List<StudentSummaryDTO> students;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
