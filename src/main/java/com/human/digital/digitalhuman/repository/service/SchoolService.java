package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.SchoolPO;

/**
 * 学校 Service 接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
public interface SchoolService extends IService<SchoolPO> {
    /**
     * 分页查询
     *
     * @param page       页码
     * @param size       每页数量
     * @param schoolName 学校名称（模糊查询）
     * @return IPage<SchoolPO>
     */
    IPage<SchoolPO> pageQuery(Integer page, Integer size, String schoolName);

    /**
     * 根据域名查询
     *
     * @param domain 域名
     * @return SchoolPO
     */
    SchoolPO getByDomain(String domain);

    /**
     * 根据学校名称查询
     *
     * @param schoolName 学校名称
     * @return SchoolPO
     */
    SchoolPO getBySchoolName(String schoolName);
}
