package com.human.digital.digitalhuman.service.handler;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * @Author taoHouChao
 * @Date 22:23 2025/6/6
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArsHandler {

    private final NlsClient nlsClient;

    @Value("${aliyun.voice-interaction.appKey}")
    private String appKey;

    private static final Map<WebSocketSession, SpeechTranscriberWrap> SESSION_ID_MAP = new ConcurrentHashMap<>(16);

    private static final Semaphore SEMAPHORE = new Semaphore(200);

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

//    @Scheduled(cron = "0/2 * * * * ?")
//    public void releaseSemaphore() {
//        EXECUTOR.execute(() -> {
//            Iterator<Map.Entry<WebSocketSession, SpeechTranscriberWrap>> iterator = SESSION_ID_MAP.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry<WebSocketSession, SpeechTranscriberWrap> entry = iterator.next();
//                SpeechTranscriberWrap value = entry.getValue();
//                if (System.currentTimeMillis() - value.getTime() > 1000) {
//                    SpeechTranscriber speechTranscriber = value.getSpeechTranscriber();
//                    if (null != speechTranscriber) {
//                        try {
//                            speechTranscriber.stop();
//                        } catch (Exception e) {
//                            log.error("stop speechTranscriber error", e);
//                        }
//                        value.semaphore.release();
//                    }
//                    iterator.remove();
//                }
//            }
//        });
//    }

    @Scheduled(cron = "0 * * * * ?")
    public void releaseSpeechTranscriber() {
        EXECUTOR.execute(() -> {
            Iterator<Map.Entry<WebSocketSession, SpeechTranscriberWrap>> iterator = SESSION_ID_MAP.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<WebSocketSession, SpeechTranscriberWrap> entry = iterator.next();
                WebSocketSession key = entry.getKey();
                SpeechTranscriberWrap value = entry.getValue();
                if (!key.isOpen()) {
                    iterator.remove();
                    Optional.ofNullable(value)
                            .map(SpeechTranscriberWrap::getSpeechTranscriber)
                            .ifPresent(SpeechTranscriber::close);
                }
            }
        });
    }

    public void process(WebSocketSession session,
                        byte[] voice,
                        Consumer<String> consumer,
                        Consumer<String> errorConsumer,
                        Consumer<String> finishConsumer) {

        SpeechTranscriberWrap transcriberWrap = SESSION_ID_MAP.computeIfAbsent(session, s -> {
            log.info("ars start, sessionId:{}", session.getId());
            return SpeechTranscriberWrap.create(SEMAPHORE, generateSpeechTranscriber(consumer, finishConsumer), System.currentTimeMillis());
        });
        log.info("ars time:{}", System.currentTimeMillis());
        transcriberWrap.setTime(System.currentTimeMillis());
        SpeechTranscriber transcriber = transcriberWrap.getSpeechTranscriber();
        log.info("transcriberId:{}", transcriberWrap.getUuid());
        if (null == transcriber) {
            errorConsumer.accept("识别异常，请重新输入");
            return;
        }
        try {
            transcriber.send(voice);
        } catch (Exception e) {
            log.error("send voice error", e);
        }
    }

    public void stop(WebSocketSession session){
        log.info("stop ars sessionId:{}", session.getId());
        SpeechTranscriberWrap transcriberWrap = SESSION_ID_MAP.remove(session);
        if (null == transcriberWrap) {
            log.warn("sessionId:{} transcriberWrap is null", session.getId());
            return;
        }
        Semaphore semaphore = transcriberWrap.getSemaphore();
        SpeechTranscriber synthesizer = transcriberWrap.getSpeechTranscriber();
        try {
            synthesizer.stop();
        } catch (Exception e) {
            log.error("关闭流式文本语音合成失败", e);
        } finally {
            synthesizer.close();
            semaphore.release();
        }
    }

    private SpeechTranscriber generateSpeechTranscriber(Consumer<String> consumer, Consumer<String> finishConsumer) {
        SpeechTranscriber transcriber;
        try {
            SEMAPHORE.acquire();
            //创建实例、建立连接。
            transcriber = new SpeechTranscriber(nlsClient, new SpeechTranscriberListener() {

                @Override
                public void onTranscriberStart(SpeechTranscriberResponse speechTranscriberResponse) {
                    log.info("task_id:{}, name:{}, status:{}", speechTranscriberResponse.getTaskId(), speechTranscriberResponse.getName(), speechTranscriberResponse.getStatus());
                }

                @Override
                public void onSentenceBegin(SpeechTranscriberResponse speechTranscriberResponse) {
                    log.info("task_id:{}, name:{}, status:{}", speechTranscriberResponse.getTaskId(), speechTranscriberResponse.getName(), speechTranscriberResponse.getStatus());
                }

                @Override
                public void onSentenceEnd(SpeechTranscriberResponse speechTranscriberResponse) {
                    log.info("task_id:{}, name:{}, status:{}, index:{}, result:{}, time:{}",
                            speechTranscriberResponse.getTaskId(),
                            speechTranscriberResponse.getName(),
                            speechTranscriberResponse.getStatus(),
                            speechTranscriberResponse.getTransSentenceIndex(),
                            speechTranscriberResponse.getTransSentenceText(),
                            speechTranscriberResponse.getTransSentenceTime());
                    consumer.accept(speechTranscriberResponse.getTransSentenceText());
                }

                @Override
                public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                    log.info("task_id:{}, name:{}, status:{}, index:{}, result:{}, time:{}",
                            response.getTaskId(),
                            response.getName(),
                            response.getStatus(),
                            response.getTransSentenceIndex(),
                            response.getTransSentenceText(),
                            response.getTransSentenceTime());
                }

                @Override
                public void onTranscriptionComplete(SpeechTranscriberResponse speechTranscriberResponse) {
                    log.info("task_id:{}, name:{}, status:{}", speechTranscriberResponse.getTaskId(), speechTranscriberResponse.getName(), speechTranscriberResponse.getStatus());
                    finishConsumer.accept("识别完成");
                }

                @Override
                public void onFail(SpeechTranscriberResponse speechTranscriberResponse) {
                    log.info("task_id:{}, status:{}, status_text:{}", speechTranscriberResponse.getTaskId(), speechTranscriberResponse.getStatus(), speechTranscriberResponse.getStatusText());
                }
            });
            transcriber.setAppKey(appKey);
            //输入音频编码方式。
            transcriber.setFormat(InputFormatEnum.PCM);
            //输入音频采样率。
            transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);

            //是否生成并返回标点符号。
            transcriber.setEnablePunctuation(true);
            //是否将返回结果规整化，比如将一百返回为100。
            transcriber.setEnableITN(false);
            //是否返回中间识别结果。
            transcriber.addCustomedParam("enable_intermediate_result", false);
            //设置vad断句参数。默认值：800ms，有效值：200ms～6000ms。
//            transcriber.addCustomedParam("max_sentence_silence", 600);
            //设置是否语义断句。
            transcriber.addCustomedParam("enable_semantic_sentence_detection", true);
            //设置是否开启过滤语气词，即声音顺滑。
//            transcriber.addCustomedParam("disfluency", true);
            //设置是否开启词模式。
            transcriber.addCustomedParam("enable_words",true);
            //设置vad噪音阈值参数，参数取值为-1～+1，如-0.9、-0.8、0.2、0.9。
            //取值越趋于-1，判定为语音的概率越大，亦即有可能更多噪声被当成语音被误识别。
            //取值越趋于+1，判定为噪音的越多，亦即有可能更多语音段被当成噪音被拒绝识别。
            //该参数属高级参数，调整需慎重和重点测试。
            transcriber.addCustomedParam("speech_noise_threshold", -0.8);
            //设置训练后的定制语言模型id。
            //transcriber.addCustomedParam("customization_id","你的定制语言模型id");
            //设置训练后的定制热词id。
            //transcriber.addCustomedParam("vocabulary_id","你的定制热词id");

            //此方法将以上参数设置序列化为JSON发送给服务端，并等待服务端确认。
            transcriber.start();
        } catch (Exception e) {
            log.info("init error", e);
            SEMAPHORE.release();
            return null;
        }
        return transcriber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SpeechTranscriberWrap {

        private String uuid;

        private SpeechTranscriber speechTranscriber;

        private Long time;

        private Semaphore semaphore;

        public static SpeechTranscriberWrap create(Semaphore semaphore, SpeechTranscriber speechTranscriber, Long time) {
            return new SpeechTranscriberWrap(UUID.randomUUID().toString(), speechTranscriber, time, semaphore);
        }
    }
}
