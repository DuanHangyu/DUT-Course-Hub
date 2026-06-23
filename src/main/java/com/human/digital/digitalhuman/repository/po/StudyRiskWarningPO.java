package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学业风险预警实体类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@TableName("study_risk_warning")
public class StudyRiskWarningPO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 学生ID */
    private Long studentId;

    /** 课程ID */
    private Long courseId;

    /** 风险等级：high/medium/low */
    private String riskLevel;

    /** 风险类型：stuck(卡关)/lagging(进度滞后)/inactive(不活跃) */
    private String riskType;

    /** 风险原因描述 */
    private String riskReason;

    /** 关联节点ID（卡关时） */
    private Long nodeId;

    /** 状态：0-待处理，1-已处理 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除：0-否，1-是 */
    @TableLogic
    private Integer deleted;
}
