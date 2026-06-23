package com.human.digital.digitalhuman.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.human.digital.digitalhuman.service.model.dto.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author taoHouChao
 * @Date 20:33 2025/6/7
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private static final String SESSION_URL = "https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/sessions";

    private static final String SEARCH_DETAIL_URL = "https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/sessions/%s";

    private static final String PAPERS_URL = "https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/questions/%s/papers?sort=RelevanceScore";

    private static final String ANSWER_STREAM_URL = "https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/questions/%s/stream";

    private static final String ANSWER_URL = "https://openapi.dp.tech/openapi/v1/sigma-search/api/v1/ai_search/questions/%s";

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private final ChatClient client;

    private final OkHttpClient okHttpClient;

    public String getAnswer(String question){
        String uuid = getSession(question);
        SearchDetailResponse searchDetail = getSearchDetail(uuid);
        List<SearchQuestionResponse> questions = searchDetail.getQuestions();
        if (CollectionUtil.isEmpty(questions)) {
            return "";
        }
        SearchQuestionResponse questionResponse = questions.getFirst();
        while (!Objects.equals("succeeded", questionResponse.getStatus())){
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            questionResponse = getSearchDetail(uuid).getQuestions().getFirst();
        }
        Integer questionId = questions.getFirst().getId();
        return getAnswerByQueryId(questionId);
    }

    /**
     * 获取会话
     * @param question  问题
     * @return 会话id
     */
    public String getSession(String question){
        SearchSessionDTO searchSession = SearchSessionDTO.of(question);
        RequestBody requestBody = RequestBody.create(JSONUtil.toJsonStr(searchSession), MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(SESSION_URL)
                .method("POST", requestBody)
                .addHeader("Content-Type", "application/json")
                .build();
        try(Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String bodyString = response.body().string();
                OkHttpResponse<SearchSessionResponse> result = JSONUtil.toBean(bodyString, new SearchSessionResponseType(), true);
                if (result.success()) {
                    return result.getData().getUuid();
                }
            }
        } catch (IOException e) {
            log.error("getSession error", e);
        }
        return "";
    }

    /**
     * 获取搜索详情
     * @param uuid 会话id
     * @return 搜索详情
     */
    public SearchDetailResponse getSearchDetail(String uuid){
        Request request = new Request.Builder()
                .url(String.format(SEARCH_DETAIL_URL, uuid))
                .get()
                .addHeader("Content-Type", "application/json")
                .build();
        try(Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String bodyString = response.body().string();
                OkHttpResponse<SearchDetailResponse> result = JSONUtil.toBean(bodyString, new SearchDetailResponseType(), true);
                log.info("getSearchDetail result: {}", result);
                if (result.success()) {
                    return result.getData();
                }
            }
        }catch (IOException e){
            log.error("getSearchDetail error", e);
        }
        return null;
    }

    /**
     * 获取问题相关文献
     * @param queryId 问题id
     * @return 文献
     */
    public SearchRelatePapersResponse getPapers(Integer queryId){
        Request request = new Request.Builder()
                .url(String.format(PAPERS_URL, queryId))
                .get()
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String bodyString = response.body().string();
                OkHttpResponse<SearchRelatePapersResponse> result = JSONUtil.toBean(bodyString, new SearchRelatePapersResponseType(), true);
                if (result.success()) {
                    return result.getData();
                }
            }
        } catch (IOException e) {
            log.error("getPapers error", e);
        }
        return null;
    }

    public String getAnswerByQueryId(Integer queryId){
        Request request = new Request.Builder()
                .url(String.format(ANSWER_URL, queryId))
                .get()
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() != null) {
                log.info("getAnswerByQueryId result: {}", response.body());
                String bodyString = response.body().toString();
                AiSummaryDTO aiSummaryDTO = JSONUtil.toBean(bodyString, AiSummaryDTO.class);
                return aiSummaryDTO.summary;
            }
        } catch (IOException e) {
            log.error("getPapers error", e);
        }
        return "";
    }

    @Data
    private static class AiSummaryDTO{

        private Long id;
        private String status;
        private String reason;
        private String message;
        private String type;
        private String query;
        private List<String> keywords;
        private String thinking;
        private Integer paperTotal;
        private Integer knowledgeBaseTotal;
        private String summary;
        private Long lastAnswerID;
    }

    public Flux<String> chatStream(String question){
        return client.prompt(new Prompt("你是人工智能小郭老师，请对学生提出的问题进行解答"))
                .user(question)
                .stream().content();
    }

    public String chat(String question){
        return client.prompt()
                .user(question)
                .call().content();
    }

    private static class SearchSessionResponseType extends TypeReference<OkHttpResponse<SearchSessionResponse>> {
    }

    private static class SearchDetailResponseType extends TypeReference<OkHttpResponse<SearchDetailResponse>> {
    }

    private static class SearchRelatePapersResponseType extends TypeReference<OkHttpResponse<SearchRelatePapersResponse>> {
    }
}
