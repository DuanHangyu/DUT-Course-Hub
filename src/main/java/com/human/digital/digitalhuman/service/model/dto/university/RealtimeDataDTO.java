package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 实时数据传输对象
 * 用于展示当前系统的实时活跃数据
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "实时数据信息")
public class RealtimeDataDTO {

    @Schema(description = "当前活跃学生数")
    private Integer activeStudents;

    @Schema(description = "当前活跃学校数")
    private Integer activeSchools;

    @Schema(description = "当前覆盖课程数")
    private Integer activeCourses;
}
