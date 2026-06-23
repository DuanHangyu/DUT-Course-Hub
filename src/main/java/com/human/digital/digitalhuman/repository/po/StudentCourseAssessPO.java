package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 10:23 2025/6/15
 */
@Data
@TableName("student_course_assess")
public class StudentCourseAssessPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer studentId;

    private Integer courseId;

    private Integer courseNodeId;

    private Integer assessScore;

    private Integer teacherCheckScore;

    private Integer assessMode;

    private Boolean noQuestion;

    private LocalDateTime createTime;;

    private LocalDateTime updateTime;

    public boolean passed(Integer passLine) {
        return assessScore >= passLine;
    }
}
