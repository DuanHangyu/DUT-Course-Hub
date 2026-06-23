package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模板课程查询对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "模板课程查询对象")
public class CourseTemplateQuery {

    @Schema(description = "课程名称（模糊搜索）")
    private String courseName;

    @Schema(description = "课程状态")
    private Boolean state;

    @Schema(description = "所属学科")
    private String subject;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;
}
