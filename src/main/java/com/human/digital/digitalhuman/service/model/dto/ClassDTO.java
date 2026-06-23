package com.human.digital.digitalhuman.service.model.dto;

import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级DTO
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Data
@Schema(description = "班级信息")
public class ClassDTO {

    @Schema(description = "ID")
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

    @Schema(description = "学习进度")
    private Integer learningProgress;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    public static ClassDTO of(SchoolClassPO po, String teacherName, Integer learningProgress) {
        ClassDTO dto = new ClassDTO();
        dto.setId(po.getId());
        dto.setSchoolId(po.getSchoolId());
        dto.setClassName(po.getClassName());
        dto.setTeacherId(po.getTeacherId());
        dto.setTeacherName(teacherName);
        dto.setStudentCount(po.getStudentCount());
        dto.setLearningProgress(learningProgress);
        dto.setCreateTime(po.getCreateTime());
        return dto;
    }

    /**
     * 重载方法，默认学习进度为0
     *
     * @param po          班级实体
     * @param teacherName 班主任姓名
     * @return ClassDTO
     */
    public static ClassDTO of(SchoolClassPO po, String teacherName) {
        return of(po, teacherName, 0);
    }
}
