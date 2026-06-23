package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习进度快照实体类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@TableName("study_progress_snapshot")
public class StudyProgressSnapshotPO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 学生ID */
    private Long studentId;

    /** 完成进度(%) */
    private BigDecimal progress;

    /** 快照日期 */
    private LocalDate snapshotDate;

    /** 创建时间 */
    private LocalDateTime createTime;
}
