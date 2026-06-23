package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @USER taoHouChao
 * @DATE 19:54 2025/9/11
 */
@Data
@TableName("course_student_relation")
@Schema(description = "课程学生关系")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseStudentRelationPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "课程id")
    private Integer courseId;

    @Schema(description = "学生id")
    private Integer studentId;

    @Schema(description = "关系类型 0学生，1学校")
    private Integer relationType;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CourseStudentRelationPO that = (CourseStudentRelationPO) o;
        return Objects.equals(courseId, that.courseId) && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, studentId);
    }
}
