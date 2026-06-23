package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生DTO（后台管理）
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "学生信息（后台）")
public class StudentBackendDTO {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "班级ID")
    private Integer classId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "学号")
    private String idNumber;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    public static StudentBackendDTO of(StudentPO po, String className) {
        StudentBackendDTO dto = new StudentBackendDTO();
        dto.setId(po.getId());
        dto.setSchoolId(po.getSchoolId());
        dto.setClassId(po.getClassId());
        dto.setClassName(className);
        dto.setIdNumber(po.getIdNumber());
        dto.setStudentName(po.getStudentName());
        dto.setCreateTime(po.getCreateTime());
        return dto;
    }
}
