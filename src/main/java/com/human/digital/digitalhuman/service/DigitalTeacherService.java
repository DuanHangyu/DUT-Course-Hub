package com.human.digital.digitalhuman.service;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.repository.po.StudentQuestionAnswerRecordPO;
import com.human.digital.digitalhuman.repository.service.StudentQuestionAnswerRecordService;
import com.human.digital.digitalhuman.service.model.dto.BoerResult;
import com.human.digital.digitalhuman.service.model.dto.PapersDTO;
import com.human.digital.digitalhuman.service.model.dto.QuestionPaperDTO;
import com.human.digital.digitalhuman.service.model.dto.QuestionSummaryDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @USER taoHouChao
 * @DATE 16:15 2025/6/28
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTeacherService {

    private final OkHttpClient okHttpClient;

    private final StudentQuestionAnswerRecordService studentQuestionAnswerRecordService;

    public QuestionSummaryDTO queryQuestion(Integer studentId, String question) {
        // 生成会话
        Request request = getSessionRequest(question);
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("get session error, response:{}",  response);
                throw new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR);
            }
            String responseBody = response.body().string();
            BoerResult<BoerSessionDTO> result = JSONUtil.toBean(responseBody, new BoerSessionReference(), true);
            String uuid = Optional.ofNullable(result)
                    .map(BoerResult::getData)
                    .map(BoerSessionDTO::getUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR));
            int retryCount = 0;
            final int maxRetries = 30;
            while (retryCount++ < maxRetries){
                sleep(3000L);
                // 获取会话详情
                try (Response detailResponse = okHttpClient.newCall(getSessionDetailRequest(uuid)).execute()) {
                    if (!detailResponse.isSuccessful()) {
                        log.error("get session detail error");
                        throw new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR);
                    }
                    String detailResponseBody = detailResponse.body().string();
                    BoerResult<BoerSessionDetailDTO> detailResult = JSONUtil.toBean(detailResponseBody, new BoerSessionDetailReference(), true);
                    QuestionDTO questionDTO = Optional.ofNullable(detailResult)
                            .map(BoerResult::getData)
                            .map(BoerSessionDetailDTO::getQuestions)
                            .map(List::getFirst)
                            .orElseThrow(() -> new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR));
                    log.info("questionDTO:{}", questionDTO);
                    if (questionDTO.succeeded()) {
                        try (Response summaryResponse = okHttpClient.newCall(getQuestionSummaryRequest(questionDTO.getId())).execute()) {
                            if (!summaryResponse.isSuccessful()) {
                                log.error("get query ai summary error");
                                throw new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR);
                            }
                            String summaryResponseBody = summaryResponse.body().string();
                            BoerResult<QuestionSummaryDTO> summaryResult = JSONUtil.toBean(summaryResponseBody, new QuestionSummaryReference(), true);
                            QuestionSummaryDTO questionSummaryDTO = Optional.ofNullable(summaryResult)
                                    .map(BoerResult::getData)
                                    .orElseThrow(() -> new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR));
                            QuestionPaperDTO questionPaperDTO = queryPapers(questionSummaryDTO.getId());
                            if (questionPaperDTO != null) {
                                List<PapersDTO> papers = questionPaperDTO.getList().stream()
                                        .filter(item -> StrUtil.isNotBlank(item.getTitleZh()) && StrUtil.isNotBlank(item.getAbstractZh()))
                                        .toList();
                                questionSummaryDTO.setPapers(papers);
                                questionSummaryDTO.setPaperTotal(papers.size());
                            }
                            studentQuestionAnswerRecordService.update(Wrappers.lambdaUpdate(StudentQuestionAnswerRecordPO.class)
                                    .set(StudentQuestionAnswerRecordPO::getPapers, JSONUtil.toJsonStr(questionSummaryDTO))
                                    .eq(StudentQuestionAnswerRecordPO::getStudentId, studentId)
                                    .eq(StudentQuestionAnswerRecordPO::getQuestion, question));
                            return questionSummaryDTO;
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("queryQuestion error", e);
        }
        throw new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR);
    }

    public QuestionPaperDTO queryPapers(Long id) {
        try(Response response = okHttpClient.newCall(getQustionPapersRequest(id)).execute()) {
            if (!response.isSuccessful()) {
                log.error("query papers error");
                throw new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR);
            }
            String responseBody = response.body().string();
            BoerResult<QuestionPaperDTO> result = JSONUtil.toBean(responseBody, new QuestionPaperReference(), true);
            return Optional.ofNullable(result)
                    .map(BoerResult::getData)
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnums.BO_ER_QUESTION_ERROR));
        }catch (IOException e){
            log.error("queryPapers error", e);
        }
        return null;
    }

    private static void sleep(Long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            log.error("sleep error", e);
        }
    }

    public Request getSessionRequest(String question) {
        QuestionQuery questionBody = QuestionQuery.of(question);
        MediaType mediaType = MediaType.parse("application/json");
        String jsonStr = JSONUtil.toJsonStr(questionBody);
        log.info("questionBody:{}", jsonStr);
        RequestBody requestBody = RequestBody.create(jsonStr, mediaType);
        return new Request.Builder()
                .url("https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/sessions")
                .method("POST", requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private static Request getSessionDetailRequest(String uuid) {
        return new Request.Builder()
                .url("https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/sessions/" + uuid)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private static Request getQuestionSummaryRequest(Long queryId) {
        return new Request.Builder()
                .url("https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/questions/" + queryId)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private Request getQustionPapersRequest(Long queryId){
        return new Request.Builder()
                .url("https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/questions/" + queryId + "/papers?sort=RelevanceScore")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private static class BoerSessionReference extends TypeReference<BoerResult<BoerSessionDTO>> {
    }

    private static class BoerSessionDetailReference extends TypeReference<BoerResult<BoerSessionDetailDTO>> {
    }

    private static class QuestionSummaryReference extends TypeReference<BoerResult<QuestionSummaryDTO>> {
    }

    private static class QuestionPaperReference extends TypeReference<BoerResult<QuestionPaperDTO>> {
    }

    @Data
    private static class QuestionQuery {

        private String query;

        private String model;

        private String discipline;

        @Alias("resource_id_list")
        private String[] resourceIdList;

        public static QuestionQuery of(String query) {
            QuestionQuery question = new QuestionQuery();
            question.query = query;
            question.model = "qwen";
            question.discipline = "All";
            question.resourceIdList = new String[0];
            return question;
        }
    }

    @Data
    private static class BoerSessionDTO {

        private String uuid;

        private String title;

        private Boolean share;
    }

    @Data
    private static class BoerSessionDetailDTO {

        private String uuid;

        private String title;

        private String status;

        private List<QuestionDTO> questions;
    }

    @Data
    private static class QuestionDTO {

        private Long id;

        private String query;

        private String status;

        private Long lastAnswerID;

        public Boolean succeeded() {
            return "succeeded".equals(status);
        }
    }
}
