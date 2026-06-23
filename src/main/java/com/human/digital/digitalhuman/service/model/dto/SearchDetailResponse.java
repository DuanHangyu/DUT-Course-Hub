package com.human.digital.digitalhuman.service.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 19:05 2025/6/12
 */
@Data
public class SearchDetailResponse {

    private String uuid;
    private String title;
    private String status;
    private Boolean share;
    private String shareLink;
    private String model;
    private String modelType;
    private List<SearchQuestionResponse> questions;
    private String resources;
    private String createTime;
    private Integer permission;
    private String discipline;
}
