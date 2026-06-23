package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门课程DTO
 *
 * @Author taoHouChao
 * @Date 10:25 2026/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "热门课程")
public class PopularCourseDTO {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "学习人数")
    private Integer studentCount;
}
