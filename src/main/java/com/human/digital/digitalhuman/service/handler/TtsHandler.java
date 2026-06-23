package com.human.digital.digitalhuman.service.handler;

import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.*;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * &#064;Author  taoHouChao
 * &#064;Date  13:37 2025/6/7
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TtsHandler {

    private static final Map<WebSocketSession, FlowingSpeechSynthesizerWrapper> SESSION_MAP = new ConcurrentHashMap<>(16);

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final Semaphore SEMAPHORE = new Semaphore(200);


    private final NlsClient nlsClient;

    @Value("${aliyun.voice-interaction.appKey}")
    private String appKey;

    public void process(String text, Consumer<byte[]> consumer) {
        SpeechSynthesizer synthesizer = null;
        try {
            //创建实例，建立连接。
            synthesizer = new SpeechSynthesizer(nlsClient, new SpeechSynthesizerListener() {
                private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

                //语音合成结束
                @Override
                public void onComplete(SpeechSynthesizerResponse response) {
                    //调用onComplete时表示所有TTS数据已接收完成，因此为整个合成数据的延迟。该延迟可能较大，不一定满足实时场景。
                    log.info("name:{}, status:{}", response.getName(), response.getStatus());
                    consumer.accept(bos.toByteArray());
                }

                //语音合成的语音二进制数据
                @Override
                public void onMessage(ByteBuffer message) {
                    byte[] bytesArray = new byte[message.remaining()];
                    message.get(bytesArray, 0, bytesArray.length);
                    try {
                        bos.write(bytesArray);
                    } catch (IOException e) {
                        log.error("write error", e);
                    }
                }

                @Override
                public void onFail(SpeechSynthesizerResponse response) {
                    //task_id是调用方和服务端通信的唯一标识，当遇到问题时需要提供task_id以便排查。
                    log.info("task_id: {}, status: {}, status_text: {}", response.getTaskId(), response.getStatus(), response.getStatusText());
                }
            });
            synthesizer.setAppKey(appKey);
            //设置返回音频的编码格式
            synthesizer.setFormat(OutputFormatEnum.WAV);
            //设置返回音频的采样率
            synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
            //发音人
            synthesizer.setVoice("zhijia");
            //语调，范围是-500~500，可选，默认是0。
            synthesizer.setPitchRate(0);
            //语速，范围是-500~500，默认是0。
            synthesizer.setSpeechRate(100);
            //设置用于语音合成的文本
            synthesizer.setText(text);
            // 是否开启字幕功能（返回相应文本的时间戳），默认不开启，需要注意并非所有发音人都支持该参数。
            synthesizer.addCustomedParam("enable_subtitle", false);
            //此方法将以上参数设置序列化为JSON格式发送给服务端，并等待服务端确认。
            long start = System.currentTimeMillis();
            synthesizer.start();
            log.info("tts start latency:{}ms", (System.currentTimeMillis() - start));
            //等待语音合成结束
            synthesizer.waitForComplete();
            log.info("tts stop latency:{}ms", (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("ttsService process fail", e);
        } finally {
            //关闭连接
            if (null != synthesizer) {
                synthesizer.close();
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    public void releaseSpeechTranscriber() {
        EXECUTOR.execute(() -> {
            Iterator<Map.Entry<WebSocketSession, FlowingSpeechSynthesizerWrapper>> iterator = SESSION_MAP.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<WebSocketSession, FlowingSpeechSynthesizerWrapper> entry = iterator.next();
                WebSocketSession key = entry.getKey();
                FlowingSpeechSynthesizerWrapper value = entry.getValue();
                if (!key.isOpen()) {
                    iterator.remove();
                    Optional.ofNullable(value)
                            .map(FlowingSpeechSynthesizerWrapper::getSynthesizer)
                            .ifPresent(FlowingSpeechSynthesizer::close);
                }
            }
        });
    }

    public void start(WebSocketSession session,
                      Consumer<byte[]> consumer,
                      Consumer<String> finishConsumer,
                      Consumer<String> errorConsumer){
        log.info("ttsHandler start");
        FlowingSpeechSynthesizerWrapper wrapper = SESSION_MAP.computeIfAbsent(session,
                s -> FlowingSpeechSynthesizerWrapper.create(SEMAPHORE, generateFlowingSpeechSynthesizer(consumer, finishConsumer), System.currentTimeMillis()));
        FlowingSpeechSynthesizer synthesizer = wrapper.getSynthesizer();
        if (null == synthesizer) {
            errorConsumer.accept("翻译失败，请稍后再试");
            throw new BusinessException(ErrorCodeEnums.TRANSLATE_ERROR);
        }
    }

    public void streamProcess(WebSocketSession session,
                              String textArray) {
        FlowingSpeechSynthesizerWrapper wrapper = SESSION_MAP.get(session);
        log.info("streamProcess textArray:{}", textArray);
        wrapper.setTime(System.currentTimeMillis());
        FlowingSpeechSynthesizer synthesizer = wrapper.getSynthesizer();
        try {
            synthesizer.send(textArray);
        } catch (Exception e) {
            log.error("streamProcess error", e);
        }
    }

    public void stop(WebSocketSession session) {
        FlowingSpeechSynthesizerWrapper synthesizerWrapper = SESSION_MAP.get(session);
        if (null != synthesizerWrapper) {
            Semaphore semaphore = synthesizerWrapper.getSemaphore();
            FlowingSpeechSynthesizer synthesizer = synthesizerWrapper.getSynthesizer();
            try {
                synthesizer.stop();
            } catch (Exception e) {
                log.error("关闭流式文本语音合成失败", e);
            } finally {
                synthesizer.close();
                semaphore.release();
                SESSION_MAP.remove(session);
            }
        }
    }

    public FlowingSpeechSynthesizer generateFlowingSpeechSynthesizer(Consumer<byte[]> consumer, Consumer<String> finishConsumer) {
        FlowingSpeechSynthesizer synthesizer = null;
        try {
            if (SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS)) {
                //创建实例，建立连接。
                synthesizer = new FlowingSpeechSynthesizer(nlsClient, new FlowingSpeechSynthesizerListener() {
                    private boolean isFirst = true;
                    @Override
                    public void onSentenceBegin(FlowingSpeechSynthesizerResponse response) {
                        log.info("Sentence Begin name:{}, status:{}",
                                response.getName(),
                                response.getStatus());
                    }

                    @Override
                    public void onAudioData(ByteBuffer byteBuffer) {
                        byte[] bytesArray = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytesArray, 0, bytesArray.length);
                        consumer.accept(addWavHeader(bytesArray));
                    }

                    /**
                     * 句子结束。 服务端检测到了一句话的结束，获得这句话的起止位置和所有时间戳
                     * @param response 结果
                     */
                    @Override
                    public void onSentenceEnd(FlowingSpeechSynthesizerResponse response) {
                        log.info("Sentence End name:{}, status:{}, subtitles:{}", response.getName(), response.getStatus(), response.getObject("subtitles"));
                    }

                    @Override
                    public void onSynthesisComplete(FlowingSpeechSynthesizerResponse response) {
                        log.info("synthesisComplete name:{}, status:{}", response.getName(), response.getStatus());
                        finishConsumer.accept("合成完成");
                    }

                    @Override
                    public void onFail(FlowingSpeechSynthesizerResponse response) {
                        log.info("onFail, session_id:{}, task_id:{}, status:{}, status_text:{}",
                                getFlowingSpeechSynthesizer().getCurrentSessionId(),
                                response.getTaskId(),
                                response.getStatus(),
                                response.getStatusText());
                    }

                    /**
                     * 收到语音合成的增量音频时间戳
                     * @param response 结果
                     */
                    @Override
                    public void onSentenceSynthesis(FlowingSpeechSynthesizerResponse response) {
                        log.info("sentenceSynthesis name:{}, status:{}, subtitles:{}", response.getName(), response.getStatus(), response.getObject("subtitles"));
                    }
                });
                synthesizer.setAppKey(appKey);
                //设置返回音频的编码格式。
                synthesizer.setFormat(OutputFormatEnum.WAV);
                //设置返回音频的采样率。
                synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
                //发音人。
                synthesizer.setVoice("zhijia");
                //音量，范围是0~100，可选，默认50。
                synthesizer.setVolume(50);
                //语调，范围是-500~500，可选，默认是0。
                synthesizer.setPitchRate(0);
                //语速，范围是-500~500，默认是0。
                synthesizer.setSpeechRate(150);
                //此方法将以上参数设置序列化为JSON发送给服务端，并等待服务端确认。
                long start = System.currentTimeMillis();
                synthesizer.start();
                log.info("tts start latency:{}", (System.currentTimeMillis() - start) + " ms");
                //设置连续两次发送文本的最小时间间隔（毫秒），如果当前调用send时距离上次调用时间小于此值，则会阻塞并等待直到满足条件再发送文本
                synthesizer.setMinSendIntervalMS(10L);
            }
        } catch (Exception e) {
            log.error("ttsService process fail", e);
            SEMAPHORE.release();
        }
        return synthesizer;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class FlowingSpeechSynthesizerWrapper {

        private FlowingSpeechSynthesizer synthesizer;

        private Long time;

        private Semaphore semaphore;

        public static FlowingSpeechSynthesizerWrapper create(Semaphore semaphore, FlowingSpeechSynthesizer synthesizer, Long time) {
            return new FlowingSpeechSynthesizerWrapper(synthesizer, time, semaphore);
        }
    }

    private byte[] addWavHeader(byte[] pcmData) {
        ByteBuffer buffer = ByteBuffer.allocate(44 + pcmData.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // WAV头（16kHz, 16bit, 单声道）
        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + pcmData.length);
        buffer.put("WAVE".getBytes());
        buffer.put("fmt ".getBytes());
        buffer.putInt(16);
        buffer.putShort((short) 1);
        buffer.putShort((short) 1);
        buffer.putInt(16000);
        buffer.putInt(32000);
        buffer.putShort((short) 1);
        buffer.putShort((short) 16);
        buffer.put("data".getBytes());
        buffer.putInt(pcmData.length);
        buffer.put(pcmData);

        return buffer.array();
    }
}
