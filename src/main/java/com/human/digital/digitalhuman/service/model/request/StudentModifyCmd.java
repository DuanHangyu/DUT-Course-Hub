package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 10:37 2025/6/11
 */
@Data
public class StudentModifyCmd {

    @Schema(description = "学生id")
    @NotEmpty(message = "学生id不能为空")
    private Integer id;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "学生名称")
    private String studentName;

    @Schema(description = "班级")
    private String schoolClass;

    @Schema(description = "密码")
    private String password;
}
