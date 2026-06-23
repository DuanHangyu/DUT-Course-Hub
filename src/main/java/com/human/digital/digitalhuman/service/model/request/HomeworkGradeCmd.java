package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 作业批改请求
 *
 * @Author taoHouChao
 * @Date 2025/03/12
 */
@Data
@Schema(description = "作业批改请求")
public class HomeworkGradeCmd {

    @NotNull(message = "提交记录ID不能为空")
    @Schema(description = "提交记录ID")
    private Integer submissionId;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于0")
    @DecimalMax(value = "100", message = "成绩不能大于100")
    @Schema(description = "成绩（0-100）")
    private BigDecimal score;

    @Size(max = 500, message = "批改备注不能超过500字")
    @Schema(description = "批改备注")
    private String comment;
}
