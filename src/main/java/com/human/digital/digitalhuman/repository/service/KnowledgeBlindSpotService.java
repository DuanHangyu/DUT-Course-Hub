package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.KnowledgeBlindSpotPO;

import java.time.LocalDate;
import java.util.List;

/**
 * 知识盲区统计服务接口
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
public interface KnowledgeBlindSpotService extends IService<KnowledgeBlindSpotPO> {

    /**
     * 根据课程ID和日期查询盲区列表
     *
     * @param courseId       课程ID
     * @param statisticsDate 统计日期
     * @return 盲区列表
     */
    List<KnowledgeBlindSpotPO> listByCourseIdAndDate(Long courseId, LocalDate statisticsDate);

    /**
     * 批量保存盲区统计
     *
     * @param blindSpotList 盲区列表
     */
    void saveBatch(List<KnowledgeBlindSpotPO> blindSpotList);
}
