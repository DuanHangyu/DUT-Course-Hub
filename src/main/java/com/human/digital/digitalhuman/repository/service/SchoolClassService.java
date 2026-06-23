package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.SchoolClassPO;

/**
 * 班级 Service 接口
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
public interface SchoolClassService extends IService<SchoolClassPO> {
    /**
     * 分页查询
     *
     * @param page       页码
     * @param size       每页数量
     * @param schoolId   学校ID
     * @param className  班级名称（模糊查询）
     * @return IPage<SchoolClassPO>
     */
    IPage<SchoolClassPO> pageQuery(Integer page, Integer size, Integer schoolId, String className);

    /**
     * 根据学校ID和班级名称查询
     *
     * @param schoolId  学校ID
     * @param className 班级名称
     * @return SchoolClassPO
     */
    SchoolClassPO getBySchoolIdAndClassName(Integer schoolId, String className);
}
