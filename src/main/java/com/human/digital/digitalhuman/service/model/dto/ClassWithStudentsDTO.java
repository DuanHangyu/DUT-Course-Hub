package com.human.digital.digitalhuman.service.model.dto;

import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 班级及学生列表DTO
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/9
 */
@Data
@Schema(description = "班级及学生列表")
public class ClassWithStudentsDTO {

    @Schema(description = "班级ID")
    private Integer id;

    @Schema(description = "学校ID")
    private Integer schoolId;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "班主任ID")
    private Integer teacherId;

    @Schema(description = "班主任姓名")
    private String teacherName;

    @Schema(description = "学生人数")
    private Integer studentCount;

    @Schema(description = "学生列表")
    private List<StudentInfo> students;

    @Data
    @Schema(description = "学生信息")
    public static class StudentInfo {
        @Schema(description = "学生ID")
        private Integer id;

        @Schema(description = "学号")
        private String idNumber;

        @Schema(description = "学生姓名")
        private String studentName;

        @Schema(description = "班级")
        private String schoolClass;

        public static StudentInfo of(StudentPO po) {
            StudentInfo info = new StudentInfo();
            info.setId(po.getId());
            info.setIdNumber(po.getIdNumber());
            info.setStudentName(po.getStudentName());
            info.setSchoolClass(po.getSchoolClass());
            return info;
        }
    }

    public static ClassWithStudentsDTO of(SchoolClassPO po, String teacherName, List<StudentPO> students) {
        ClassWithStudentsDTO dto = new ClassWithStudentsDTO();
        dto.setId(po.getId());
        dto.setSchoolId(po.getSchoolId());
        dto.setClassName(po.getClassName());
        dto.setTeacherId(po.getTeacherId());
        dto.setTeacherName(teacherName);
        dto.setStudentCount(po.getStudentCount());
        dto.setStudents(students != null ? students.stream().map(StudentInfo::of).toList() : List.of());
        return dto;
    }
}
