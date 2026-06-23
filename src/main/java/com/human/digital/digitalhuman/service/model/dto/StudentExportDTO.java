package com.human.digital.digitalhuman.service.model.dto;

import cn.hutool.core.annotation.Alias;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author taoHouChao
 * @Date 13:45 2025/6/11
 */
@Data
@NoArgsConstructor
public class StudentExportDTO {

    public static final Map<Integer, Integer> COLUMN_WIDTH = Map.of(
            0, 20,
            1, 20,
            2, 20,
            3, 20
    );

    @Schema(description = "学号")
    @Alias("学号")
    private String idNumber;

    @Schema(description = "学生名称")
    @Alias("学生姓名")
    private String studentName;

    @Schema(description = "班级")
    @Alias("班级")
    private String schoolClass;

    @Schema(description = "密码")
    @Alias("密码")
    private String password;

    public StudentExportDTO(StudentPO studentPO){
        this.idNumber = studentPO.getIdNumber();
        this.studentName = studentPO.getStudentName();
        this.schoolClass = studentPO.getSchoolClass();
        this.password = studentPO.getPassword();
    }
}
