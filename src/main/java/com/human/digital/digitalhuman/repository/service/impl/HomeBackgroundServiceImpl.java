package com.human.digital.digitalhuman.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.HomeBackgroundMapper;
import com.human.digital.digitalhuman.repository.po.HomeBackgroundPO;
import com.human.digital.digitalhuman.repository.service.HomeBackgroundService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页底图 Service 实现类
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
@Service
public class HomeBackgroundServiceImpl extends ServiceImpl<HomeBackgroundMapper, HomeBackgroundPO> implements HomeBackgroundService {

    @Override
    public IPage<HomeBackgroundPO> pageQuery(Integer page, Integer size, String name) {
        Page<HomeBackgroundPO> pageParam = new Page<>(page, size);
        return this.page(pageParam, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBackgroundPO>()
                .like(name != null, HomeBackgroundPO::getName, name)
                .orderByAsc(HomeBackgroundPO::getSort)
                .orderByDesc(HomeBackgroundPO::getId));
    }

    @Override
    public List<HomeBackgroundPO> listOrderBySort() {
        return this.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBackgroundPO>()
                .orderByAsc(HomeBackgroundPO::getSort)
                .orderByAsc(HomeBackgroundPO::getId));
    }

    @Override
    public Integer getMaxSort() {
        HomeBackgroundPO po = this.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<HomeBackgroundPO>()
                .orderByDesc(HomeBackgroundPO::getSort)
                .last("LIMIT 1"));
        return po == null ? null : po.getSort();
    }
}
