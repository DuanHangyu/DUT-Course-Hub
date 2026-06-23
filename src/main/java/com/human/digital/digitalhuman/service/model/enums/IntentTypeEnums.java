package com.human.digital.digitalhuman.service.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 意图类型枚举
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Getter
@AllArgsConstructor
public enum IntentTypeEnums {
    COURSE_QUERY("COURSE_QUERY", "课程查询"),
    STUDENT_QUERY("STUDENT_QUERY", "学生查询"),
    DATA_ANALYSIS("DATA_ANALYSIS", "数据分析"),
    GENERAL("GENERAL", "通用问题");

    private final String code;
    private final String description;

    /**
     * 根据 code 获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static IntentTypeEnums fromCode(String code) {
        for (IntentTypeEnums enums : values()) {
            if (enums.getCode().equalsIgnoreCase(code)) {
                return enums;
            }
        }
        return GENERAL;
    }
}
