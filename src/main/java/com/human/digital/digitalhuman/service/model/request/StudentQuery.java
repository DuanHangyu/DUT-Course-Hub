package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 学生查询对象（后台管理）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "学生查询（后台）")
public class StudentQuery {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页数量")
    private Integer size = 10;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "学生ID列表")
    private List<Integer> ids;
}
