package com.human.digital.digitalhuman.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @USER taoHouChao
 * @DATE 14:27 2025/6/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDTO {

    private Integer index;

    private Integer total;

    private String title;

    private Boolean last;

    public String indexString() {
        return switch (index) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "七";
            case 8 -> "八";
            case 9 -> "九";
            default -> "十";
        };
    }
}
