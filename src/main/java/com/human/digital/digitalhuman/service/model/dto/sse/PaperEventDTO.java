package com.human.digital.digitalhuman.service.model.dto.sse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 16:34 2025/6/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PaperEventDTO extends SseEventDTO{

    @JsonProperty("content")
    private Paper content;

    @Data
    public static class Paper {
        private String abstractText;
        private String abstractZh;
        private String aiSummarize;
        private String arxiv;
        private List<String> author;
        private String bohriumId;
        private int citationNums;
        private String doi;
        private Object figures; // 可能是List或Map
        private String fullText;
        private int impactFactor;
        private double impactFactorScore;
        private String journal;
        private String link;
        private String openAccess;
        private boolean pdfFlag;
        private String pieces;
        private String publicationCover;
        private String publicationDate;
        private int publicationId;
        private int publicationScore;
        private double relevanceScore;
        private String seoTitle;
        private int sequenceId;
        private double sortScore;
        private String source;
        private String sourceZh;
        private String title;
        private String titleZh;
    }
}
