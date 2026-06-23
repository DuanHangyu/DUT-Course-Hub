package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 添加班级到课程命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "添加班级到课程命令对象")
public class AddClassesCmd {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID")
    private Integer courseId;

    @NotEmpty(message = "班级ID列表不能为空")
    @Schema(description = "班级ID列表")
    private List<Integer> classIds;
}
