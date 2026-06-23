package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交详情响应
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "提交详情响应")
public class HomeworkSubmitDetailDTO {

    @Schema(description = "学生ID")
    private Integer studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "提交状态：0-未提交，1-已提交")
    private Integer submitStatus;

    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime submitTime;

    @Schema(description = "提交文件列表")
    private List<HomeworkFileDTO> files;
}
