package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 11:24 2025/9/12
 */
@Data
@Schema(description = "学习记录列表查询参数")
public class LearnRecordListQuery {

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
