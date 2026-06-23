package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 10:29 2025/6/15
 */
@Data
@TableName("student_course_assess_detail")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentCourseAssessDetailPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer studentId;

    private Integer courseId;

    private Integer courseNodeId;

    private String content;

    private String question;

    private String answer;

    private Integer score;

    private Integer state;

    private Integer analysisState;

    private String analysis;

    private String suggestion;

    private String highlights;

    private String shortcomings;
}
