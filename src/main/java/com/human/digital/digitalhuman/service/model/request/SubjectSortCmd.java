package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 学科排序命令
 *
 * @Author taoHouChao
 * @Date 10:25 2026/3/10
 */
@Data
@Schema(description = "学科排序命令")
public class SubjectSortCmd {

    @Schema(description = "学校ID")
    @NotNull(message = "学校ID不能为空")
    private Integer schoolId;

    @Schema(description = "学科ID列表，按顺序排列")
    @NotEmpty(message = "学科ID列表不能为空")
    private List<Integer> subjectIds;
}
