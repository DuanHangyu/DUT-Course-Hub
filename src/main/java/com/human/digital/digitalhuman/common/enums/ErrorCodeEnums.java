package com.human.digital.digitalhuman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeEnums {

    SUCCESS(0, "成功"),
    GET_RESPONSE_ERROR(99, "获取响应失败"),
    STUDENT_EXIST(101, "学生已存在"),
    STUDENT_SAVE_ERROR(102, "学生保存失败"),
    STUDENT_EXPORT_ERROR(103,  "学生导出失败"),
    STUDENT_NOT_EXIST(104, "学生不存在"),
    USER_EXIST(201, "用户已存在"),
    USER_SAVE_ERROR(202, "用户保存失败"),
    USER_NOT_EXIST(203, "用户不存在"),
    USER_PASSWORD_ERROR(204, "用户密码错误"),
    COURSE_NOT_EXIST(300, "课程不存在"),
    COURSE_NODE_NOT_EXIST(301, "课程节不存在"),
    LAST_NODE_NOT_EXIST(302, "结束节点不存在"),
    FIRST_NODE_NOT_EXIST(303, "开始节点不存在"),
    COURSE_NOT_START(304, "课程未开始"),
    COURSE_ASSESS_NOT_EXIST(305, "课程考核不存在"),
    COURSE_NODE_NOT_ANSWER(306, "课程节点未回答"),
    COURSE_NODE_NOT_ANALYSIS(307, "存在解析中的视频，请稍后再试"),
    LEARNING_RECORD_NOT_EXIST(401, "学习记录不存在"),
    LEARNING_RECORD_NOT_LAST_NOE(402, "考核不是终极考核，无法修改"),
    FILE_NOT_EXIST(502, "文件不存在"),
    ASSESS_ALREADY_FINISH(601, "考核已结束，生成分析中，请稍后刷新再试"),
    AI_CALL_ERROR(701, "AI调用错误"),
    ANALYSIS_FAILED(801, "AI解析失败"),
    BANNER_NOT_EXIST(810, "Banner 不存在"),
    BACKGROUND_NOT_EXIST(811, "底图不存在"),
    TRANSLATE_ERROR(901, "翻译错误"),
    BO_ER_QUESTION_ERROR(902, "boer问题错误"),
    STUDY_RECORD_NOT_FOUND(903, "学习记录不存在"),
    COURSE_STUDENT_COMMENT_NOT_EXIST(904, "课程学生提问不存在"),
    USER_NOT_AUTHORIZED(1001, "不是该用户的评论，无法删除"),
    ASSESS_DETAIL_NOT_EXIST(1002, "考核详情不存在"),
    SCHOOL_EXIST(1101, "学校已存在"),
    SCHOOL_NOT_EXIST(1102, "学校不存在"),
    SCHOOL_NAME_DUPLICATE(1103, "学校名称重复"),
    ACCOUNT_EXIST(1104, "账号已存在"),
    INVALID_IP_WHITELIST(1105, "IP白名单格式错误"),
    CLASS_NOT_EXIST(1201, "班级不存在"),
    CLASS_NAME_DUPLICATE(1202, "班级名称已存在"),
    STUDENT_NO_DUPLICATE(1203, "学号已存在"),
    SCHOOL_STUDENT_LIMIT(1204, "学校学生数量已达上限"),
    NO_PERMISSION(1403, "没有操作权限"),
    SCHOOL_ID_NOT_FOUND(1106, "未获取到学校ID"),
    // 作业相关 13xx
    HOMEWORK_NOT_EXIST(1301, "作业不存在"),
    HOMEWORK_DEADLINE_PASSED(1302, "作业已截止"),
    HOMEWORK_NO_PERMISSION(1303, "无权限操作该作业"),
    HOMEWORK_SUBMIT_NOT_EXIST(1304, "提交记录不存在"),
    HOMEWORK_NOT_SUBMITTED(1305, "该作业未提交，无法批改"),
    HOMEWORK_SCORE_OUT_OF_RANGE(1306, "成绩超出范围（0-100）"),
    // 关联关系已存在
    RELATION_EXIST(1401, "关联关系已存在"),
    INVITE_CODE_ERROR(1402, "邀请码错误"),
    // 15xx - 课程资料相关
    COURSE_MATERIAL_FOLDER_NOT_EXIST(1501, "文件夹不存在"),
    COURSE_MATERIAL_FOLDER_NAME_DUPLICATE(1502, "文件夹名称重复"),
    COURSE_MATERIAL_FILE_NOT_EXIST(1503, "文件不存在"),
    COURSE_MATERIAL_FILE_LIMIT_EXCEEDED(1504, "课程资料文件数量已达上限"),
    COURSE_MATERIAL_FOLDER_NOT_BELONG_COURSE(1505, "文件夹不属于该课程")
    ;

    private final int code;

    private final String message;
}
