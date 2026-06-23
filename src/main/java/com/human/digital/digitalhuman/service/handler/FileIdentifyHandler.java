package com.human.digital.digitalhuman.service.handler;

import cn.hutool.core.annotation.Alias;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nls.client.protocol.NlsClient;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.common.utils.ThreadPoolUtils;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import com.human.digital.digitalhuman.service.model.dto.FileInfoDTO;
import com.human.digital.digitalhuman.service.model.event.NodeModifyEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @USER taoHouChao
 * @DATE 20:54 2025/6/17
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileIdentifyHandler {

    // 地域ID，常量，固定值。
    public static final String REGIONID = "cn-shanghai";
    public static final String ENDPOINTNAME = "cn-shanghai";
    public static final String PRODUCT = "nls-filetrans";
    public static final String DOMAIN = "filetrans.cn-shanghai.aliyuncs.com";
    public static final String API_VERSION = "2018-08-17";  // 中国站版本
    // public static final String API_VERSION = "2019-08-23";  // 国际站版本
    public static final String POST_REQUEST_ACTION = "SubmitTask";
    public static final String GET_REQUEST_ACTION = "GetTaskResult";
    // 请求参数
    public static final String KEY_APP_KEY = "appkey";
    public static final String KEY_FILE_LINK = "file_link";
    public static final String KEY_VERSION = "version";
    public static final String KEY_ENABLE_WORDS = "enable_words";
    // 响应参数
    public static final String KEY_TASK = "Task";
    public static final String KEY_TASK_ID = "TaskId";
    public static final String KEY_STATUS_TEXT = "StatusText";
    public static final String KEY_RESULT = "Result";
    // 状态值
    public static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_QUEUEING = "QUEUEING";

    private static final int BUFFER_SIZE = 8192; // 8KB 缓冲区

    private final OkHttpClient okHttpClient;

    @Value("${aliyun.voice-interaction.appKey}")
    private String appKey;

    @Value("${file.download.path}")
    private String tempDir;

    private final NlsClient nlsClient;

    private final OssClientUtils ossClientUtils;

    private final CourseNodeService courseNodeService;

    private final IAcsClient acsClient;

    public void schedulerTransformVideo() {
        List<CourseNodePO> all = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .ne(CourseNodePO::getNodeName, "开始")
                .ne(CourseNodePO::getNodeName, "结束")
                .in(CourseNodePO::getTaskStatus, 0, 3));
        if (CollectionUtils.isEmpty(all)) {
            return;
        }
        List<Integer> nodeIds = all.stream().map(CourseNodePO::getId).toList();
        courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                .set(CourseNodePO::getTaskStatus, 1)
                .in(CourseNodePO::getId, nodeIds));
        for (CourseNodePO courseNodePO : all) {
            if (StringUtils.isNotBlank(courseNodePO.getContent())) {
                courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                        .set(CourseNodePO::getTaskStatus, 2)
                        .eq(CourseNodePO::getId, courseNodePO.getId()));
                continue;
            }
            try {
                transformNodeVideoToText(courseNodePO);
                courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                        .set(CourseNodePO::getTaskStatus, 2)
                        .eq(CourseNodePO::getId, courseNodePO.getId()));
            } catch (Exception e) {
                log.error("transformNodeVideoToText error", e);
                courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                        .set(CourseNodePO::getTaskStatus, 3)
                        .eq(CourseNodePO::getId, courseNodePO.getId()));
            }
        }
    }

    //    @EventListener(NodeModifyEvent.class)
    public void syncCourseNodeContent(NodeModifyEvent nodeModifyEvent) {
        CourseNodePO courseNode = nodeModifyEvent.getCourseNode();
        if (courseNode.startNode() || courseNode.endNode()) {
            log.info("no need transform content");
            return;
        }
        log.info("syncCourseNodeContent start");
        ThreadPoolUtils.execute(() -> transformNodeVideoToText(courseNode));
    }

    private void transformNodeVideoToText(CourseNodePO courseNode) {
        String videoUrl = courseNode.getVideoUrl();
        Integer id = courseNode.getId();
        FileInfoDTO videoFile = JSONUtil.toBean(videoUrl, FileInfoDTO.class);
        log.info("videoFile:{}", videoFile);
        // 转换文本
        String result = convertVideoToText(videoFile.getUrl(),
                taskId -> courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                        .set(CourseNodePO::getTaskId, taskId)
                        .eq(CourseNodePO::getId, id)),
                error -> courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                        .set(CourseNodePO::getVideoToTextErrorMessage, error)
                        .eq(CourseNodePO::getId, id)));
        if (StrUtil.isNotBlank(result)) {
            courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                    .set(CourseNodePO::getContent, result)
                    .set(CourseNodePO::getVideoToTextErrorMessage, "")
                    .eq(CourseNodePO::getId, id));
        }
    }

    private File downloadWithOkHttp(String url) {
        log.info("开始下载文件：{}", url);
        // 1. 创建临时文件（带 MD5 前缀）
        String md5 = DigestUtils.md5Hex(url);
        File tempFile = new File(tempDir + md5 + ".mp4");
        // 3. 构建请求（支持 HTTPS 和重定向）
        Request request = new Request.Builder()
                .url(url)
                .build();
        // 4. 执行下载
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP 错误：" + response.code());
            }
            // 确保响应内容是视频
            String contentType = response.header("Content-Type");
            if (contentType == null || !contentType.startsWith("video/")) {
                throw new IOException("URL does not point to a video file");
            }
            // 5. 处理响应（支持断点续传）
            return writeFile(response.body().byteStream(), tempFile);
        } catch (IOException e) {
            log.warn("下载异常", e);
        }
        return null;
    }

    private File writeFile(InputStream input, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedInputStream bis = new BufferedInputStream(input, BUFFER_SIZE)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytesRead = 0;
            long startTime = System.currentTimeMillis();
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                // 每秒打印下载进度
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= 1000) {
                    double speed = (totalBytesRead / 1024.0) / ((currentTime - startTime) / 1000.0);
                    log.info("下载进度：{}/s，已下载：{} KB",
                            String.format("%.2f KB", speed),
                            totalBytesRead / 1024);
                    startTime = currentTime;
                }
            }
            log.info("文件下载完成，大小: {} KB", totalBytesRead / 1024);
            return outputFile;
        }
    }

    public String convertVideoToText(String url,
                                     Consumer<String> tasIdConsumer,
                                     Consumer<String> convertErrorConsumer) {
        // 下载文件
        File tempVideo = downloadWithOkHttp(ossClientUtils.getSignedUrl(url));
        if (tempVideo == null) {
            throw new BusinessException(ErrorCodeEnums.FILE_NOT_EXIST);
        }
        File audioFile = null;
        String result = "";
        try {
            // 1. 创建临时文件（优先使用内存文件系统）
            audioFile = File.createTempFile("audio_", ".wav");
            // 2. FFmpeg 音频提取（带超时控制）
            if (!extractAudio(tempVideo, audioFile)) {
                throw new RuntimeException("音频提取失败");
            }
            // 3. 上传阿里云oss
            String audioUrl = ossClientUtils.uploadFile(audioFile);
            // 4. 清理临时文件（守护线程确保删除）
            scheduleFileDeletion(tempVideo, audioFile);
            // 4. 提交录音文件识别
            String taskId = submitFileTransRequest(audioUrl);
            tasIdConsumer.accept(taskId);
            // 6.获取识别结果
            IdentifyResult fileTransResult = getFileTransResult(taskId, convertErrorConsumer);
            if (fileTransResult != null) {
                result = fileTransResult.getSentences().stream().map(Sentence::getText).collect(Collectors.joining());
                log.info("fileTransResult result:{}", result);
            }
        } catch (Exception e) {
            log.error("服务异常：", e);
            throw new RuntimeException("服务异常：" + e.getMessage());
        } finally {
            // 强制清理（防止内存泄漏）
            forceDelete(tempVideo);
            forceDelete(audioFile);
        }
        return result;
    }

    public String submitFileTransRequest(String fileLink) {
        // 1. 创建CommonRequest，设置请求参数。
        CommonRequest postRequest = new CommonRequest();
        // 设置域名
        postRequest.setDomain(DOMAIN);
        // 设置API的版本号，格式为YYYY-MM-DD。
        postRequest.setVersion(API_VERSION);
        // 设置action
        postRequest.setAction(POST_REQUEST_ACTION);
        // 设置产品名称
        postRequest.setProduct(PRODUCT);
        // 2. 设置录音文件识别请求参数，以JSON字符串的格式设置到请求Body中。
        JSONObject taskObject = new JSONObject();
        // 设置appkey
        taskObject.put(KEY_APP_KEY, appKey);
        // 设置音频文件访问链接
        taskObject.put(KEY_FILE_LINK, fileLink);
        // 新接入请使用4.0版本，已接入（默认2.0）如需维持现状，请注释掉该参数设置。
        taskObject.put(KEY_VERSION, "4.0");
        // 设置是否输出词信息，默认为false，开启时需要设置version为4.0及以上。
        taskObject.put(KEY_ENABLE_WORDS, true);
        String task = taskObject.toJSONString();
        log.info("录音文件识别请求参数：{}", task);
        // 设置以上JSON字符串为Body参数。
        postRequest.putBodyParameter(KEY_TASK, task);
        // 设置为POST方式的请求。
        postRequest.setMethod(MethodType.POST);
        // postRequest.setHttpContentType(FormatType.JSON);    //当aliyun-java-sdk-core 版本为4.6.0及以上时，请取消该行注释
        // 3. 提交录音文件识别请求，获取录音文件识别请求任务的ID，以供识别结果查询使用。
        String taskId = null;
        try {
            CommonResponse postResponse = acsClient.getCommonResponse(postRequest);
            log.info("提交录音文件识别请求的响应：{}", postResponse.getData());
            if (postResponse.getHttpStatus() == 200) {
                JSONObject result = JSONObject.parseObject(postResponse.getData());
                String statusText = result.getString(KEY_STATUS_TEXT);
                if (STATUS_SUCCESS.equals(statusText)) {
                    taskId = result.getString(KEY_TASK_ID);
                }
            }
        } catch (ClientException e) {
            log.error("提交录音文件识别请求异常：", e);
        }
        return taskId;
    }

    private IdentifyResult getFileTransResult(String taskId, Consumer<String> convertErrorConsumer) {
        // 1. 创建CommonRequest，设置任务ID。
        CommonRequest getRequest = new CommonRequest();
        // 设置域名
        getRequest.setDomain(DOMAIN);
        // 设置API版本
        getRequest.setVersion(API_VERSION);
        // 设置action
        getRequest.setAction(GET_REQUEST_ACTION);
        // 设置产品名称
        getRequest.setProduct(PRODUCT);
        // 设置任务ID为查询参数
        getRequest.putQueryParameter(KEY_TASK_ID, taskId);
        // 设置为GET方式的请求
        getRequest.setMethod(MethodType.GET);

        // 2. 提交录音文件识别结果查询请求, 以轮询的方式进行识别结果的查询，直到服务端返回的状态描述为“SUCCESS”或错误描述，则结束轮询。
        String result;
        while (true) {
            try {
                CommonResponse getResponse = acsClient.getCommonResponse(getRequest);
                log.info("识别查询结果:{}", getResponse.getData());
                if (getResponse.getHttpStatus() != 200) {
                    convertErrorConsumer.accept("httpStatus is not 200");
                    break;
                }
                JSONObject rootObj = JSONObject.parseObject(getResponse.getData());
                String statusText = rootObj.getString(KEY_STATUS_TEXT);
                if (STATUS_RUNNING.equals(statusText) || STATUS_QUEUEING.equals(statusText)) {
                    // 继续轮询，注意设置轮询时间间隔。
                    Thread.sleep(10000);
                } else {
                    // 状态信息为成功，返回识别结果；状态信息为异常，返回空。
                    if (STATUS_SUCCESS.equals(statusText)) {
                        result = rootObj.getString(KEY_RESULT);
                        // 状态信息为成功，但没有识别结果，则可能是由于文件里全是静音、噪音等导致识别为空。
                        if (result != null) {
                            return JSONUtil.toBean(result, IdentifyResult.class);
                        }
                    }
                    convertErrorConsumer.accept(statusText);
                    break;
                }
            } catch (Exception e) {
                log.info("识别查询结果异常", e);
            }
        }
        return null;
    }

    private void scheduleFileDeletion(File... files) {
        new Thread(() -> {
            try {
                Thread.sleep(60000); // 等待主流程结束
                for (File file : files) {
                    forceDelete(file);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void forceDelete(File file) {
        if (file != null && file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.warn("删除失败：{}", file.getAbsolutePath(), e);
            }
        }
    }

    // ------------------ FFmpeg 工具类 ------------------
    private boolean extractAudio(File videoFile, File audioFile) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y", // 覆盖输出文件
                "-i", videoFile.getAbsolutePath(),
                "-vn", // 禁用视频流
                "-acodec", "pcm_s16le", // 16位PCM编码
                "-ar", "16000", // 采样率
                "-ac", "1", // 单声道
                "-f", "wav", // 强制输出格式
                audioFile.getAbsolutePath()
        );
        processBuilder.redirectErrorStream(true); // 合并错误流
        try {
            Process process = processBuilder.start();
            // 多线程读取流（防止阻塞）
            new StreamGobbler(process.getInputStream()).start();
            // 超时控制（5分钟）
            if (!process.waitFor(300, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg 超时");
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFmpeg 执行失败", e);
        }
    }

    // ------------------ 辅助类 ------------------
    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;

        StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("FFmpeg: {}", line);
                }
            } catch (IOException e) {
                log.error("读取 FFmpeg 输出失败", e);
            }
        }
    }

    @Data
    private static class IdentifyResult {

        @Alias("Words")
        private List<Word> words;

        @Alias("Sentences")
        private List<Sentence> sentences;
    }

    @Data
    private static class Word {

        @Alias("Word")
        private String word;

        @Alias("EndTime")
        private Long endTime;

        @Alias("BeginTime")
        private Long beginTime;
    }

    @Data
    private static class Sentence {

        @Alias("EndTime")
        private Long endTime;

        @Alias("BeginTime")
        private Long beginTime;

        @Alias("SilenceDuration")
        private Integer silenceDuration;

        @Alias("SpeakerId")
        private String speakerId;

        @Alias("Text")
        private String text;

        @Alias("ChannelId")
        private Integer channelId;

        @Alias("SpeechRate")
        private Integer speechRate;

        @Alias("EmotionValue")
        private Double emotionValue;
    }
}
