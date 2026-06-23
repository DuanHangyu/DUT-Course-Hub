package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;

import java.util.List;
import java.util.Map;

public interface CourseNodeService extends IService<CourseNodePO> {
    /**
     * 根据课程id获取课程节
     *
     * @param id 课程id
     * @return 课程节
     */
    List<CourseNodePO> queryByCourseId(Integer id);

    /**
     * 根据id批量查询
     *
     * @param courseNodeIds 课程节id
     * @return 课程节
     */
    Map<Integer, CourseNodePO> queryByIds(List<Integer> courseNodeIds);

    /**
     * 根据名称批量查询
     *
     * @param courseNodeName 课程节名称
     * @return 课程节
     */
    List<Integer> queryIdByName(String courseNodeName);

    /**
     * 根据课程id批量查询
     *
     * @param courseId 课程id
     * @return 课程节
     */
    List<CourseNodePO> queryByCourseIdWithoutContent(Integer courseId);
}
