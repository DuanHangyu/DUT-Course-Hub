package com.human.digital.digitalhuman.service.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学生命令对象（后台管理）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "学生命令（后台）")
public class StudentCmd {

    @Schema(description = "ID（修改时使用）")
    private Integer id;

    @Schema(description = "学校ID")
    @NotNull(message = "学校ID不能为空")
    private Integer schoolId;

    @Schema(description = "班级ID")
    @NotNull(message = "班级不能为空")
    private Integer classId;

    @Schema(description = "学号")
    @NotBlank(message = "学号不能为空")
    private String idNumber;

    @Schema(description = "学生姓名")
    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    @Schema(description = "登录密码")
    @NotBlank(message = "登录密码不能为空")
    private String password;
}
