package com.human.digital.digitalhuman.service.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业列表响应（教师端）
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "作业列表响应（教师端）")
public class HomeworkListDTO {

    @Schema(description = "作业ID")
    private Integer id;

    @Schema(description = "作业标题")
    private String title;

    @Schema(description = "作业描述")
    private String description;

    @Schema(description = "截止时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime deadline;

    @Schema(description = "附件列表")
    private List<HomeworkFileDTO> attachments;

    @Schema(description = "已提交人数")
    private Integer submittedCount;

    @Schema(description = "总人数")
    private Integer totalCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
