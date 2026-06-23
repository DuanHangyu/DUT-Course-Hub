package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 22:36 2025/6/10
 */
@Data
@Schema(description = "学生列表查询参数")
public class StudentListQuery {

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "班级")
    private String schoolClass;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
