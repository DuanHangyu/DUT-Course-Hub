package com.human.digital.digitalhuman.service.model.request;

import cn.hutool.core.annotation.Alias;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 11:02 2025/6/11
 */
@Data
public class StudentImportCmd {

    @Schema(description = "学号")
    @NotEmpty(message = "学号不能为空")
    @Alias("学号")
    private String idNumber;

    @Schema(description = "学生名称")
    @NotEmpty(message = "学生名称不能为空")
    @Alias("学生姓名")
    private String studentName;

    @Schema(description = "班级")
    @NotEmpty(message = "班级不能为空")
    @Alias("班级")
    private String schoolClass;

    @Schema(description = "密码")
    @NotEmpty(message = "密码不能为空")
    @Alias("密码")
    private String password;

    public StudentPO toPo() {
        StudentPO po = new StudentPO();
        po.setIdNumber(idNumber);
        po.setSchoolClass(schoolClass);
        po.setStudentName(studentName);
        po.setPassword(password);
        return po;
    }
}
