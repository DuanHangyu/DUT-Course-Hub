package com.human.digital.digitalhuman.controller.scheduler;

import com.human.digital.digitalhuman.service.handler.FileIdentifyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @USER taoHouChao
 * @DATE 00:05 2025/9/16
 */
@Component
@RequiredArgsConstructor
public class NodePickVideoContentScheduler {

    private final FileIdentifyHandler fileIdentifyHandler;

    @Value("${pick.video.content.enable:stop}")
    private String start;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void schedulerPickVideoContent() {
        if ("stop".equals(start)) {
            return;
        }
        fileIdentifyHandler.schedulerTransformVideo();
    }
}
