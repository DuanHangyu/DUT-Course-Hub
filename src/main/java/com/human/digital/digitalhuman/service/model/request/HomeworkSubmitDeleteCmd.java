package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 19:37 2026/3/21
 */
@Data
@Schema(description = "删除作业提交请求参数")
public class HomeworkSubmitDeleteCmd {

    @Schema(description = "作业提交ID")
    private Integer id;
}
