package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交列表响应
 *
 * @Author taoHouChao
 * @Date 2025/03/12
 */
@Data
@Schema(description = "作业提交列表响应")
public class HomeworkSubmissionListDTO {

    @Schema(description = "提交记录ID")
    private Integer id;

    @Schema(description = "学生ID")
    private Integer studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "提交状态：UNSUBMITTED-未提交，SUBMITTED-已提交，GRADED-已批改")
    private String status;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime submitTime;

    @Schema(description = "成绩")
    private BigDecimal score;

    @Schema(description = "批改备注")
    private String comment;

    @Schema(description = "提交文件列表")
    private List<HomeworkFileDTO> files;
}
