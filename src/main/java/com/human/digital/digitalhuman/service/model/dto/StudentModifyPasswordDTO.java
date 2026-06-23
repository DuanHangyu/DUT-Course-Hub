package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 14:50 2025/9/11
 */
@Data
@Schema(description = "修改学生密码")
public class StudentModifyPasswordDTO {

    @Schema(description = "原先的密码")
    private String originalPassword;

    @Schema(description = "新的密码")
    private String newPassword;
}
