package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.HomeBannerPO;

import java.util.List;

/**
 * 首页 Banner Service 接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/3
 */
public interface HomeBannerService extends IService<HomeBannerPO> {
    /**
     * 分页查询
     *
     * @param page     页码
     * @param size     每页数量
     * @param name     图片名称（模糊查询）
     * @return IPage<HomeBannerPO>
     */
    IPage<HomeBannerPO> pageQuery(Integer page, Integer size, String name);

    /**
     * 获取所有 Banner（按 sort 排序）
     *
     * @return List<HomeBannerPO>
     */
    List<HomeBannerPO> listOrderBySort();

    /**
     * 获取最大 sort 值
     *
     * @return Integer
     */
    Integer getMaxSort();
}
