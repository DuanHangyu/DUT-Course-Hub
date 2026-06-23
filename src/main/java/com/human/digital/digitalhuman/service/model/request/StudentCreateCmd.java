package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.StudentPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 21:29 2025/6/10
 */
@Data
@Schema(description = "学生创建命令")
public class StudentCreateCmd {

    @Schema(description = "学号")
    @NotEmpty(message = "学号不能为空")
    private String idNumber;

    @Schema(description = "学生名称")
    @NotEmpty(message = "学生名称不能为空")
    private String studentName;

    @Schema(description = "班级")
    @NotEmpty(message = "班级不能为空")
    private String schoolClass;

    @Schema(description = "密码")
    @NotEmpty(message = "密码不能为空")
    private String password;

    public StudentPO toPo(){
        StudentPO po = new StudentPO();
        po.setIdNumber(idNumber);
        po.setSchoolClass(schoolClass);
        po.setStudentName(studentName);
        po.setPassword(password);
        return po;
    }
}
