package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 课程精选命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "课程精选命令对象")
public class CourseFeaturedCmd {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Integer courseId;

    @NotNull(message = "精选状态不能为空")
    @Schema(description = "是否精选（0-否，1-是）")
    private Integer isFeatured;
}
