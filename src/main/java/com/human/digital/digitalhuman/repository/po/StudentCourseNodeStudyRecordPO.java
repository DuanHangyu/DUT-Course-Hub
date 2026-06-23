package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 16:40 2025/9/12
 */
@Data
@TableName("student_course_node_study_record")
@Schema(description = "学生课程节学习记录")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class StudentCourseNodeStudyRecordPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "学生ID")
    private Integer studentId;

    @Schema(description = "课程节ID")
    private Integer courseNodeId;

    @Schema(description = "学习开始时间")
    private LocalDateTime studyStartTime;

    @Schema(description = "学习结束时间")
    private LocalDateTime studyEndTime;

    @Schema(description = "学习完成状态")
    private Boolean completed;

    public Integer getStudyTime(){
        if (studyStartTime != null && studyEndTime != null) {
            Duration duration = Duration.between(studyStartTime, studyEndTime);
            long seconds = duration.getSeconds();
            return (int) (seconds > 0 ? (seconds + 59) / 60 : 0); // 向上取整分钟
        }
        return 0;
    }
}
