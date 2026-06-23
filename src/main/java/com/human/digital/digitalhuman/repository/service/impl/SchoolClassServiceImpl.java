package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.SchoolClassMapper;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;
import com.human.digital.digitalhuman.repository.service.SchoolClassService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 班级 Service 实现
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Service
public class SchoolClassServiceImpl extends ServiceImpl<SchoolClassMapper, SchoolClassPO> implements SchoolClassService {

    @Override
    public IPage<SchoolClassPO> pageQuery(Integer page, Integer size, Integer schoolId, String className) {
        Page<SchoolClassPO> pageParam = new Page<>(page, size);
        return this.page(pageParam, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchoolClassPO>()
                .eq(SchoolClassPO::getSchoolId, schoolId)
                .like(StringUtils.isNotBlank(className), SchoolClassPO::getClassName, className)
                .orderByDesc(SchoolClassPO::getCreateTime));
    }

    @Override
    public SchoolClassPO getBySchoolIdAndClassName(Integer schoolId, String className) {
        return this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchoolClassPO>()
                .eq(SchoolClassPO::getSchoolId, schoolId)
                .eq(SchoolClassPO::getClassName, className));
    }
}
