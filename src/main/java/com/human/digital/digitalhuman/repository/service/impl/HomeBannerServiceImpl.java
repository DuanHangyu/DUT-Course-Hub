package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.HomeBannerMapper;
import com.human.digital.digitalhuman.repository.po.HomeBannerPO;
import com.human.digital.digitalhuman.repository.service.HomeBannerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页 Banner Service 实现类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Service
public class HomeBannerServiceImpl extends ServiceImpl<HomeBannerMapper, HomeBannerPO> implements HomeBannerService {

    @Override
    public IPage<HomeBannerPO> pageQuery(Integer page, Integer size, String name) {
        Page<HomeBannerPO> pageParam = new Page<>(page, size);
        return this.page(pageParam, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBannerPO>()
                .like(name != null, HomeBannerPO::getName, name)
                .orderByAsc(HomeBannerPO::getSort)
                .orderByDesc(HomeBannerPO::getId));
    }

    @Override
    public List<HomeBannerPO> listOrderBySort() {
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBannerPO>()
                .orderByAsc(HomeBannerPO::getSort)
                .orderByAsc(HomeBannerPO::getId));
    }

    @Override
    public Integer getMaxSort() {
        HomeBannerPO po = this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBannerPO>()
                .orderByDesc(HomeBannerPO::getSort)
                .last("LIMIT 1"));
        return po == null ? null : po.getSort();
    }
}
