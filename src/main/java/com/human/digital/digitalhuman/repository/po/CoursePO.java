package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author taoHouChao
 * @Date 19:24 2025/6/11
 */
@Data
@TableName("course")
public class CoursePO {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 课程名称 */
    private String courseName;

    /** 课程介绍 */
    private String courseIntroduce;

    /** 授课老师名称 */
    private String teacherName;

    /** 老师头像URL */
    private String headUrl;

    /** 课程状态（true-上架，false-下架） */
    private Boolean state;

    /** 课程封面图片URL */
    private String pictureUrl;

    /** 创建人ID */
    private Integer creatorId;

    /** 考核题目数量 */
    private Integer lastDefenseQuestionsCount;

    /** 关联类型 */
    private Integer relationType;

    /**
     * 课程类型：1-模板课程，2-学习课程
     */
    private Integer courseType;

    /**
     * 所属学科
     */
    private String subject;

    /**
     * 是否精选（0-否，1-是）
     */
    private Integer isFeatured;

    /**
     * 课程邀请码
     */
    private String inviteCode;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
