package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 知识盲区统计实体类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Data
@TableName("knowledge_blind_spot")
public class KnowledgeBlindSpotPO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 知识节点ID */
    private Long nodeId;

    /** 节点名称 */
    private String nodeName;

    /** 盲区人数（未完成人数） */
    private Integer blindCount;

    /** 盲区比例(%) */
    private BigDecimal blindRate;

    /** 统计日期 */
    private LocalDate statisticsDate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除：0-否，1-是 */
    @TableLogic
    private Integer deleted;
}
