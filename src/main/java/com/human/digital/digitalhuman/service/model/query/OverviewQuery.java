package com.human.digital.digitalhuman.service.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 概览查询参数
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/13
 */
@Data
@Schema(description = "概览查询参数")
public class OverviewQuery {

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "开始日期（格式：yyyy-MM-dd）")
    private String startDate;

    @Schema(description = "结束日期（格式：yyyy-MM-dd）")
    private String endDate;
}
