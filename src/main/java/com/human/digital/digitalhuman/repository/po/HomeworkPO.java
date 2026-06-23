package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作业持久化对象
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@TableName("homework")
public class HomeworkPO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 课程ID */
    private Integer courseId;

    /** 作业标题 */
    private String title;

    /** 作业描述 */
    private String description;

    /** 截止时间 */
    private LocalDateTime deadline;

    /** 附件列表（JSON数组） */
    private String attachments;

    /** 创建教师ID */
    private Integer teacherId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除（0-否，1-是） */
    private Integer deleted;
}
