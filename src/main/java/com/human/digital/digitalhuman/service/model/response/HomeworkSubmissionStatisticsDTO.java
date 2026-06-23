package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 作业提交统计响应
 *
 * @Author taoHouChao
 * @Date 2025/03/12
 */
@Data
@Schema(description = "作业提交统计响应")
public class HomeworkSubmissionStatisticsDTO {

    @Schema(description = "总人数")
    private Integer totalCount;

    @Schema(description = "已提交人数")
    private Integer submittedCount;

    @Schema(description = "未提交人数")
    private Integer unsubmittedCount;

    @Schema(description = "已批改人数")
    private Integer gradedCount;
}
