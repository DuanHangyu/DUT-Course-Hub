package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业提交持久化对象
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@TableName("homework_submit")
public class HomeworkSubmitPO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 作业ID */
    private Integer homeworkId;

    /** 学生ID */
    private Integer studentId;

    /** 提交文件列表（JSON数组） */
    private String files;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 成绩（0-100） */
    private BigDecimal score;

    /** 批改备注 */
    private String comment;

    /** 批改人ID */
    private Integer graderId;

    /** 批改时间 */
    private LocalDateTime gradeTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除（0-否，1-是） */
    private Integer deleted;
}
