package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 22:30 2025/6/11
 */
@Data
@Schema(description = "课程列表查询参数")
public class CourseListQuery {

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
