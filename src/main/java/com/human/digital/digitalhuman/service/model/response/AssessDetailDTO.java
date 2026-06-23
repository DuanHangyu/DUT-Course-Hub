package com.human.digital.digitalhuman.service.model.response;

import com.human.digital.digitalhuman.service.model.dto.AnalysisDTO;
import com.human.digital.digitalhuman.service.model.dto.AnswerEvaluationDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 20:56 2025/6/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核详情")
public class AssessDetailDTO {

    @Schema(description = "学习记录ID")
    private Integer id;

    @Schema(description = "得分")
    private Integer score;

    @Schema(description = "及格线")
    private Integer passLine;

    @Schema(description = "问题答案评估结果")
    private List<AnswerEvaluation> questionAndAnswers;

    @Schema(description = "分析结果")
    private List<AssessAnalysis> analysis;

    public static AssessDetailDTO of(AssessmentResultDetailDTO detailDTO){
        List<AnswerEvaluation> questionAndAnswers = detailDTO.getQuestionAndAnswers()
                .stream()
                .map(AnswerEvaluation::of)
                .toList();
        AssessmentResultDetailDTO.AssessAnalysis analysisOrigin = detailDTO.getAnalysis();
        List<AssessAnalysis> analysis = new ArrayList<>();
        analysis.add(new AssessAnalysis("数学理解", analysisOrigin.getMathematicalUnderstandingScore()));
        analysis.add(new AssessAnalysis("知识应用", analysisOrigin.getKnowledgeApplicationScore()));
        analysis.add(new AssessAnalysis("问题分析", analysisOrigin.getProblemAnalysisScore()));
        analysis.add(new AssessAnalysis("工程能力", analysisOrigin.getEngineeringAbilityScore()));
        analysis.add(new AssessAnalysis("拓展能力", analysisOrigin.getInnovationScore()));
        return AssessDetailDTO.builder()
                .id(detailDTO.getId())
                .score(detailDTO.getScore())
                .passLine(detailDTO.getPassLine())
                .questionAndAnswers(questionAndAnswers)
                .analysis(analysis)
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AnswerEvaluation{

        @Schema(description = "问题")
        private String question;

        @Schema(description = "分数")
        private Integer score;

        @Schema(description = "学生答案")
        private String studentAnswer;

        @Schema(description = "分析结果")
        private List<String> analysis;

        @Schema(description = "建议")
        private List<String> suggestions;

        @Schema(description = "亮点")
        private List<String> strengths;

        @Schema(description = "不足")
        private List<String> weaknesses;

        public static AnswerEvaluation of(AnswerEvaluationDTO answerEvaluation){
            AnalysisDTO analysisDTO = answerEvaluation.getAnalysis();
            List<String> analysis = getAnalysis(analysisDTO);
            return AssessDetailDTO.AnswerEvaluation.builder()
                    .question(answerEvaluation.getQuestion())
                    .score(answerEvaluation.getScore())
                    .studentAnswer(answerEvaluation.getStudentAnswer())
                    .analysis(analysis)
                    .suggestions(answerEvaluation.getSuggestions())
                    .strengths(answerEvaluation.getStrengths())
                    .weaknesses(answerEvaluation.getWeaknesses())
                    .build();
        }

        @NotNull
        private static List<String> getAnalysis(AnalysisDTO analysisDTO) {
            List<String> analysis = new ArrayList<>(5);
            analysis.add(AssessAnalysis.MATHEMATICAL_UNDERSTANDING + analysisDTO.getMathematicalUnderstanding());
            analysis.add(AssessAnalysis.KNOWLEDGE_APPLICATION + analysisDTO.getKnowledgeApplication());
            analysis.add(AssessAnalysis.PROBLEM_ANALYSIS + analysisDTO.getProblemAnalysis());
            analysis.add(AssessAnalysis.ENGINEERING_ABILITY + analysisDTO.getEngineeringAbility());
            analysis.add(AssessAnalysis.INNOVATION + analysisDTO.getInnovation());
            return analysis;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Analysis{

        @Schema(description = "数学原理")
        private String mathematicalUnderstanding;

        @Schema(description = "数学理解分数")
        private Integer mathematicalUnderstandingScore;

        @Schema(description = "知识应用")
        private String knowledgeApplication;

        @Schema(description = "知识应用分数")
        private Integer knowledgeApplicationScore;

        @Schema(description = "问题分析")
        private String problemAnalysis;

        @Schema(description = "问题分析分数")
        private Integer problemAnalysisScore;

        @Schema(description = "工程能力")
        private String engineeringAbility;

        @Schema(description = "工程能力分数")
        private Integer engineeringAbilityScore;

        @Schema(description = "拓展能力")
        private String innovation;

        @Schema(description = "拓展能力分数")
        private Integer innovationScore;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "考核评估结果")
    public static class AssessAnalysis{

        public static final String MATHEMATICAL_UNDERSTANDING = "数学理解（20%）：";

        public static final String KNOWLEDGE_APPLICATION = "知识应用（25%）：";

        public static final String PROBLEM_ANALYSIS = "问题分析（20%）：";

        public static final String ENGINEERING_ABILITY = "工程能力（20%）：";

        public static final String INNOVATION = "拓展能力（15%）：";

        @Schema(description = "名称")
        private String name;

        @Schema(description = "分数")
        private Integer value;
    }
}
