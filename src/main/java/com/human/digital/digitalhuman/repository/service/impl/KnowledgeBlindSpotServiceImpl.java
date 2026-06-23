package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.KnowledgeBlindSpotMapper;
import com.human.digital.digitalhuman.repository.po.KnowledgeBlindSpotPO;
import com.human.digital.digitalhuman.repository.service.KnowledgeBlindSpotService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 知识盲区统计服务实现类
 *
 * @Author taoHouChao
 * @Date 16:52 2026/3/12
 */
@Service
public class KnowledgeBlindSpotServiceImpl extends ServiceImpl<KnowledgeBlindSpotMapper, KnowledgeBlindSpotPO>
        implements KnowledgeBlindSpotService {

    @Override
    public List<KnowledgeBlindSpotPO> listByCourseIdAndDate(Long courseId, LocalDate statisticsDate) {
        return this.list(Wrappers.lambdaQuery(KnowledgeBlindSpotPO.class)
                .eq(KnowledgeBlindSpotPO::getCourseId, courseId)
                .eq(KnowledgeBlindSpotPO::getStatisticsDate, statisticsDate)
                .orderByDesc(KnowledgeBlindSpotPO::getBlindRate));
    }

    @Override
    public void saveBatch(List<KnowledgeBlindSpotPO> blindSpotList) {
        this.saveBatch(blindSpotList);
    }
}
