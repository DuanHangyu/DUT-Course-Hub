package com.human.digital.digitalhuman.service.model.dto;

import lombok.Data;

/**
 * @author taoHouChao
 * @date 19:06 2025/6/12
 */
@Data
public class SearchQuestionResponse {

    private Integer id;

    private String query;

    private String status;

    private Integer lastAnswerID;
}
