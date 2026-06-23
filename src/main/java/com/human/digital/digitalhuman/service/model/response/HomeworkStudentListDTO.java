package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业列表响应（学生端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "作业列表响应（学生端）")
public class HomeworkStudentListDTO {

    @Schema(description = "作业ID")
    private Integer id;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "作业描述")
    private String description;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime deadline;

    @Schema(description = "提交状态：0-未提交，1-已提交，2-已截止")
    private Integer submitStatus;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime submitTime;
}
