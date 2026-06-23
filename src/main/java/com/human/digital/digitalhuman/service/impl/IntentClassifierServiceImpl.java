package com.human.digital.digitalhuman.service.impl;

import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.service.IntentClassifierService;
import com.human.digital.digitalhuman.service.model.enums.IntentTypeEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 意图识别服务 LLM 实现
 *
 * @Author taoHouChao
 * @Date 10:00 2025/3/13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentClassifierServiceImpl implements IntentClassifierService {

    private static final String PROMPT = """
            请根据用户问题判断意图类型，只能返回以下四种之一：
            - COURSE_QUERY: 课程相关（课程内容、章节、进度）
            - STUDENT_QUERY: STUDENT_QUERY: 学生相关（学生名单、学习进度、作业提交）
            - DATA_ANALYSIS: 数据分析（学情分析、成绩统计、风险预警）
            - GENERAL: 通用问题（问候、闲聊、系统使用帮助）

            用户问题：%s

            只返回意图类型，不要其他内容。
            """;

    private static final String CACHE_PREFIX = "intent:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    // 课程相关关键词
    private static final Map<String, IntentTypeEnums> KEYWORD_RULES = Map.ofEntries(
            Map.entry("课程", IntentTypeEnums.COURSE_QUERY),
            Map.entry("章节", IntentTypeEnums.COURSE_QUERY),
            Map.entry("课件", IntentTypeEnums.COURSE_QUERY),
            Map.entry("视频", IntentTypeEnums.COURSE_QUERY),
            Map.entry("学习内容", IntentTypeEnums.COURSE_QUERY),
            Map.entry("学生", IntentTypeEnums.STUDENT_QUERY),
            Map.entry("作业", IntentTypeEnums.STUDENT_QUERY),
            Map.entry("提交", IntentTypeEnums.STUDENT_QUERY),
            Map.entry("成绩", IntentTypeEnums.DATA_ANALYSIS),
            Map.entry("分析", IntentTypeEnums.DATA_ANALYSIS),
            Map.entry("统计", IntentTypeEnums.DATA_ANALYSIS),
            Map.entry("风险", IntentTypeEnums.DATA_ANALYSIS),
            Map.entry("预警", IntentTypeEnums.DATA_ANALYSIS),
            Map.entry("进度", IntentTypeEnums.DATA_ANALYSIS)
    );

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;

    @Override
    public IntentTypeEnums classify(String message) {
        // 1. 先尝试从缓存获取
        String cacheKey = CACHE_PREFIX + message.hashCode();
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("意图识别缓存命中: {}", message);
                return IntentTypeEnums.fromCode(cached);
            }
        } catch (Exception e) {
            log.warn("读取意图缓存失败, use LLM instead", e);
        }

        // 2. 基于关键词规则快速判断
        IntentTypeEnums keywordResult = classifyByKeyword(message);
        if (keywordResult != null) {
            // 缓存结果
            cacheResult(cacheKey, keywordResult);
            return keywordResult;
        }

        // 3. 使用 LLM 判断
        try {
            String result = chatClient.prompt()
                    .user(String.format(PROMPT, message))
                    .call()
                    .content();

            if (result == null) {
                throw new BusinessException(ErrorCodeEnums.AI_CALL_ERROR);
            }
            IntentTypeEnums intentType = IntentTypeEnums.fromCode(result.trim());
            cacheResult(cacheKey, intentType);
            return intentType;
        } catch (Exception e) {
            log.warn("意图识别失败，使用默认类型, message:{}", message, e);
            return IntentTypeEnums.GENERAL;
        }
    }

    /**
     * 基于关键词快速分类
     */
    private IntentTypeEnums classifyByKeyword(String message) {
        for (Map.Entry<String, IntentTypeEnums> entry : KEYWORD_RULES.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 缓存结果
     */
    private void cacheResult(String cacheKey, IntentTypeEnums intentType) {
        try {
            redisTemplate.opsForValue().set(cacheKey, intentType.getCode(),
                    CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存意图识别结果失败", e);
        }
    }
}
