package com.human.digital.digitalhuman.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author taoHouChao
 * @Date 19:22 2025/6/12
 */
@Data
public class SearchRelatePapersResponse {

    private Integer logId;

    private List<SearchRelatePapersDTO> list;

    private List<String> sourceList;

    @Data
    private static class SearchRelatePapersDTO{
        private Integer sequenceId;
        private List<String> author;
        private String link;
        private String source;
        private String sourceZh;
        @JsonProperty("abstract")
        private String summary;
        @JsonProperty("abstractZh")
        private String summaryZh;
        private String title;
        private String titleZh;
        private String doi;
        private String bohriumId;
        /**
         * 出版id
         */
        private Integer publicationId;
        private String publicationCover;
        private String publicationDate;
        private String journal;
        private String arxiv;
        private String aiSummarize;
        private String openAccess;
        private Boolean pdfFlag;
        private String pieces;
        private String sortScore;
        private String relevanceScore;
        private Integer publicationScore;
        private Double impactFactor;
        private String impactFactorScore;
        private Integer citationNums;
        private String fullText;
        private String figures;
    }
}
