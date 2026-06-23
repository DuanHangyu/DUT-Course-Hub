package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * @Author taoHouChao
 * @Date 19:01 2025/6/12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchSessionDTO {

    private String query;

    private String model;

    private String discipline;

    @JsonProperty("resource_id_list")
    private List<String> resourceIdList;

    public static SearchSessionDTO of(String query){
        return SearchSessionDTO.builder()
                .query(query)
                .model("qwen")
                .discipline("All")
                .resourceIdList(Collections.emptyList())
                .build();
    }
}
