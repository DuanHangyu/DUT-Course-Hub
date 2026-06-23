package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 班级命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "班级命令")
public class ClassCmd {

    @Schema(description = "ID（修改时使用）")
    private Integer id;

    @Schema(description = "学校ID")
    @NotNull(message = "学校ID不能为空")
    private Integer schoolId;

    @Schema(description = "班级名称")
    @NotBlank(message = "班级名称不能为空")
    private String className;

    @Schema(description = "班主任ID")
    @NotNull(message = "班主任不能为空")
    private Integer teacherId;
}
