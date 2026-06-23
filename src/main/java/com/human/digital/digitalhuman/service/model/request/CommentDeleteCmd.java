package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 16:34 2025/9/14
 */
@Data
@Schema(description = "课程学生提问删除命令")
public class CommentDeleteCmd {

    @Schema(description = "id")
    private Integer id;
}
