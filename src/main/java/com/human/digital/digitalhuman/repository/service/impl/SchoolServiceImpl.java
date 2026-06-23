package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.SchoolMapper;
import com.human.digital.digitalhuman.repository.po.SchoolPO;
import com.human.digital.digitalhuman.repository.service.SchoolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 学校 Service 实现
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, SchoolPO> implements SchoolService {

    @Override
    public IPage<SchoolPO> pageQuery(Integer page, Integer size, String schoolName) {
        Page<SchoolPO> pageParam = new Page<>(page, size);
        return this.page(pageParam, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchoolPO>()
                .like(StringUtils.isNotBlank(schoolName), SchoolPO::getSchoolName, schoolName)
                .orderByDesc(SchoolPO::getCreateTime));
    }

    @Override
    public SchoolPO getByDomain(String domain) {
        return this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchoolPO>()
                .eq(SchoolPO::getDomain, domain));
    }

    @Override
    public SchoolPO getBySchoolName(String schoolName) {
        return this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchoolPO>()
                .eq(SchoolPO::getSchoolName, schoolName));
    }
}
