package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.HomeworkMapper;
import com.human.digital.digitalhuman.repository.po.HomeworkPO;
import com.human.digital.digitalhuman.repository.service.HomeworkService;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作业服务实现类
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Repository
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, HomeworkPO> implements HomeworkService {

    @Override
    public List<HomeworkPO> listByCourseId(Integer courseId) {
        return this.list(Wrappers.lambdaQuery(HomeworkPO.class)
                .eq(HomeworkPO::getCourseId, courseId)
                .eq(HomeworkPO::getDeleted, 0)
                .orderByDesc(HomeworkPO::getDeadline));
    }
}
