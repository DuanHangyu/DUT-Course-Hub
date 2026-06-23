package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业详情响应
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "作业详情响应")
public class HomeworkDetailDTO {

    @Schema(description = "作业ID")
    private Integer id;

    @Schema(description = "提交ID")
    private Integer submitId;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "作业描述")
    private String description;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime deadline;

    @Schema(description = "附件列表")
    private List<HomeworkFileDTO> attachments;

    @Schema(description = "提交状态：0-未提交，1-已提交，2-已截止")
    private Integer submitStatus;

    @Schema(description = "批阅状态：0-未批阅，1-已批阅")
    private Integer reviewStatus;

    /** 成绩（0-100） */
    @Schema(description = "成绩（0-100）")
    private BigDecimal score;

    @Schema(description = "我的提交文件列表")
    private List<HomeworkFileDTO> myFiles;

    @Schema(description = "我的提交时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime mySubmitTime;
}
