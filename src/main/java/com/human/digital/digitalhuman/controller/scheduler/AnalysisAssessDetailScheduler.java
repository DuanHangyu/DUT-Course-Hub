package com.human.digital.digitalhuman.controller.scheduler;

import com.human.digital.digitalhuman.service.AssessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @USER taoHouChao
 * @DATE 00:07 2025/9/16
 */
@Component
@RequiredArgsConstructor
public class AnalysisAssessDetailScheduler {

    private final AssessService assessService;

//    @Scheduled(cron = "0 0/10 * * * ?")
    public void analysisAssessDetail() {
        assessService.analysisAssessDetail();
    }
}
