package com.human.digital.digitalhuman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 功能模块枚举
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Getter
@AllArgsConstructor
public enum AuthModuleEnums {

    DATA_OVERVIEW("data_overview", "数据概览"),
    SCHOOL_COURSE("school_course", "校本课程库"),
    CLASS_STRUCTURE("class_structure", "班级架构"),
    STUDENT_MANAGE("student_manage", "学生管理"),
    TEACHER_MANAGE("teacher_manage", "教师管理");

    private final String code;
    private final String desc;
}
