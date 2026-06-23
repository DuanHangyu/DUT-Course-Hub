package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.HomeworkPO;

import java.util.List;

/**
 * 作业服务接口
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
public interface HomeworkService extends IService<HomeworkPO> {

    /**
     * 根据课程ID查询作业列表
     *
     * @param courseId 课程ID
     * @return 作业列表
     */
    List<HomeworkPO> listByCourseId(Integer courseId);
}
