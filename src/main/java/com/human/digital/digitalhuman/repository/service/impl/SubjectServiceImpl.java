package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.SubjectMapper;
import com.human.digital.digitalhuman.repository.po.SubjectPO;
import com.human.digital.digitalhuman.repository.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学科 Service 实现类
 *
 * @Author taoHouChao
 * @Date 10:15 2026/3/10
 */
@Slf4j
@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, SubjectPO> implements SubjectService {

    @Override
    public List<SubjectPO> listBySchoolId(Integer schoolId) {
        return this.list(new LambdaQueryWrapper<SubjectPO>()
                .eq(SubjectPO::getSchoolId, schoolId)
                .orderByAsc(SubjectPO::getSortOrder)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdateSortOrder(List<SubjectPO> subjectList) {
        if (subjectList == null || subjectList.isEmpty()) {
            return true;
        }
        for (int i = 0; i < subjectList.size(); i++) {
            SubjectPO subject = subjectList.get(i);
            subject.setSortOrder(i);
            this.updateById(subject);
        }
        return true;
    }
}
