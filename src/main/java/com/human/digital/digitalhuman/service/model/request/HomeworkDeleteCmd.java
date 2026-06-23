package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除作业请求
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "删除作业请求")
public class HomeworkDeleteCmd {

    @Schema(description = "作业ID")
    @NotNull(message = "作业ID不能为空")
    private Integer id;
}
