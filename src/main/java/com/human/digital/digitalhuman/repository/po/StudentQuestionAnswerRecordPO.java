package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @USER taoHouChao
 * @DATE 09:27 2025/6/19
 */
@Data
@TableName("student_question_answer_record")
public class StudentQuestionAnswerRecordPO {

    private static final int DAYS_7 = 7;
    private static final int DAYS_30 = 30;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer courseId;

    private Integer courseNodeId;

    private Integer studentId;

    private String question;

    private String answer;

    private String uuid;

    private String papers;

    private LocalDateTime createTime;

    public String getDay(LocalDate now){
        if (createTime == null) {
            return "未知时间";
        }

        LocalDate createDate = createTime.toLocalDate();

        if (createDate.equals(now)) {
            return "今天"; // 创建日期是今天
        } else if (now.minusDays(1).equals(createDate)) {
            return "昨天"; // 创建日期是昨天
        } else if (createDate.isAfter(now.minusDays(DAYS_7)) && createDate.isBefore(now)) {
            return "7天内"; // 创建日期在过去7天内（不含今天和昨天）
        } else if (createDate.isAfter(now.minusDays(DAYS_30)) && createDate.isBefore(now.minusDays(DAYS_7))) {
            return "30天内"; // 创建日期在过去30天内，但不在7天内
        } else {
            return "30天前"; // 创建日期超过30天
        }
    }
}
