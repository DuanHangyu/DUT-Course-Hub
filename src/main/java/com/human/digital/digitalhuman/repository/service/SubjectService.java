package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.SubjectPO;

import java.util.List;

/**
 * 学科 Service 接口
 *
 * @Author taoHouChao
 * @Date 10:10 2026/3/10
 */
public interface SubjectService extends IService<SubjectPO> {

    /**
     * 根据学校ID查询学科列表（按排序）
     *
     * @param schoolId 学校ID
     * @return List<SubjectPO>
     */
    List<SubjectPO> listBySchoolId(Integer schoolId);

    /**
     * 批量更新排序
     *
     * @param subjectList 学科列表
     * @return Boolean
     */
    Boolean batchUpdateSortOrder(List<SubjectPO> subjectList);
}
