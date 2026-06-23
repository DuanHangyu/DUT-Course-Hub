package com.human.digital.digitalhuman.controller.scheduler;

import com.human.digital.digitalhuman.service.StudyMonitorAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学业风险预警统计定时任务
 * 每天凌晨统计各课程学生的卡关、进度滞后、不活跃等风险并写入预警表
 *
 * @Author taoHouChao
 * @Date 23:25 2026/3/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskWarningScheduler {

    private final StudyMonitorAppService studyMonitorAppService;

    /**
     * 每天凌晨3点执行学业风险预警统计
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void calculateRiskWarning() {
        log.info("开始执行学业风险预警统计任务");
        try {
            studyMonitorAppService.calculateRiskWarning();
            log.info("学业风险预警统计任务执行完成");
        } catch (Exception e) {
            log.error("学业风险预警统计任务执行异常", e);
        }
    }
}
