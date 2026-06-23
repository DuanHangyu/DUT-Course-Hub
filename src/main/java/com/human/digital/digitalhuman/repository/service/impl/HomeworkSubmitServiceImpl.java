package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.HomeworkSubmitMapper;
import com.human.digital.digitalhuman.repository.po.HomeworkSubmitPO;
import com.human.digital.digitalhuman.repository.service.HomeworkSubmitService;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 作业提交服务实现类
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Repository
public class HomeworkSubmitServiceImpl extends ServiceImpl<HomeworkSubmitMapper, HomeworkSubmitPO> implements HomeworkSubmitService {

    @Override
    public List<HomeworkSubmitPO> listByHomeworkId(Integer homeworkId) {
        return this.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                .eq(HomeworkSubmitPO::getHomeworkId, homeworkId)
                .eq(HomeworkSubmitPO::getDeleted, 0));
    }

    @Override
    public HomeworkSubmitPO getByHomeworkIdAndStudentId(Integer homeworkId, Integer studentId) {
        return this.getOne(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                .eq(HomeworkSubmitPO::getHomeworkId, homeworkId)
                .eq(HomeworkSubmitPO::getStudentId, studentId)
                .eq(HomeworkSubmitPO::getDeleted, 0));
    }

    @Override
    public List<HomeworkSubmitPO> listByStudentIdAndHomeworkIds(Integer studentId, List<Integer> homeworkIds) {
        if (CollectionUtil.isEmpty(homeworkIds)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.lambdaQuery(HomeworkSubmitPO.class)
                .eq(HomeworkSubmitPO::getStudentId, studentId)
                .in(HomeworkSubmitPO::getHomeworkId, homeworkIds)
                .eq(HomeworkSubmitPO::getDeleted, 0));
    }
}
