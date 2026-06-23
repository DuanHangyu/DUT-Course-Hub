package com.human.digital.digitalhuman.service.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程统计数据响应对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "课程统计数据")
public class CourseStatsDTO {

    @Schema(description = "已上架课程数量")
    private Long publishedCount;

    @Schema(description = "已下架课程数量")
    private Long unpublishedCount;
}
