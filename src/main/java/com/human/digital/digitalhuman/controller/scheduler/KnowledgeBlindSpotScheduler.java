package com.human.digital.digitalhuman.controller.scheduler;

import com.human.digital.digitalhuman.service.StudyMonitorAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 知识盲区统计定时任务
 * 每天凌晨统计各课程下各知识节点的未完成学生数和盲区比例
 *
 * @Author taoHouChao
 * @Date 22:43 2026/3/21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBlindSpotScheduler {

    private final StudyMonitorAppService studyMonitorAppService;

    /**
     * 每天凌晨2点执行知识盲区统计
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateKnowledgeBlindSpot() {
        log.info("开始执行知识盲区统计任务");
        try {
            studyMonitorAppService.calculateKnowledgeBlindSpot();
            log.info("知识盲区统计任务执行完成");
        } catch (Exception e) {
            log.error("知识盲区统计任务执行异常", e);
        }
    }
}
