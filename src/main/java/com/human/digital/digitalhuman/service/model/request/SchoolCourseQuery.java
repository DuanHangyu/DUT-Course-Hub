package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 校本课程查询对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "校本课程查询对象")
public class SchoolCourseQuery {

    @Schema(description = "课程名称（模糊搜索）")
    private String courseName;

    @Schema(description = "课程状态（true-上架，false-下架）")
    private Boolean state;

    @Schema(description = "所属学科")
    private String subject;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "页码")
    private Integer page;

    @Schema(description = "每页数量")
    private Integer size;

    @Schema(description = "是否是管理员（管理员可查看所有课程，非管理员只查看自己创建或关联的课程）")
    private Boolean isAdmin;
}
