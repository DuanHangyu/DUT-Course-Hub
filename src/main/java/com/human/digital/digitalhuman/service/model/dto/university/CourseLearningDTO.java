package com.human.digital.digitalhuman.service.model.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 课程学习统计数据传输对象
 * 用于展示各课程的学习人数统计
 *
 * @Author taoHouChao
 * @Date 22:40 2026/3/13
 */
@Data
@Schema(description = "课程学习统计信息")
public class CourseLearningDTO {

    @Schema(description = "课程统计列表")
    private List<CourseStat> courses;

    /**
     * 课程统计内部类
     * 表示单个课程的学习统计信息
     */
    @Data
    @Schema(description = "课程统计")
    public static class CourseStat {

        @Schema(description = "课程ID")
        private Long courseId;

        @Schema(description = "课程名称")
        private String courseName;

        @Schema(description = "学习人数")
        private Integer studentCount;
    }
}
