package com.human.digital.digitalhuman.repository.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.human.digital.digitalhuman.repository.mapper.CourseNodeMapper;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author taoHouChao
 * @Date 19:31 2025/6/11
 */
@Repository
public class CourseNodeServiceImpl extends ServiceImpl<CourseNodeMapper, CourseNodePO> implements CourseNodeService {

    @Override
    public List<CourseNodePO> queryByCourseId(Integer id) {
        return this.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .eq(CourseNodePO::getCourseId, id));
    }

    @Override
    public Map<Integer, CourseNodePO> queryByIds(List<Integer> courseNodeIds) {
        if (CollectionUtil.isEmpty(courseNodeIds)) {
            return Collections.emptyMap();
        }
        return this.list(Wrappers.lambdaQuery(CourseNodePO.class)
                        .in(CourseNodePO::getId, courseNodeIds))
                .stream()
                .collect(Collectors.toMap(CourseNodePO::getId, courseNodePO -> courseNodePO));
    }

    @Override
    public List<Integer> queryIdByName(String courseNodeName) {
        if (StrUtil.isBlank(courseNodeName)) {
            return null;
        }
        return this.list(Wrappers.lambdaQuery(CourseNodePO.class)
                        .select(CourseNodePO::getId)
                        .like(CourseNodePO::getNodeName, courseNodeName))
                .stream()
                .map(CourseNodePO::getId)
                .toList();
    }

    @Override
    public List<CourseNodePO> queryByCourseIdWithoutContent(Integer courseId) {
        return this.list(Wrappers.lambdaQuery(CourseNodePO.class)
                        .select(CourseNodePO::getId, CourseNodePO::getNodeName, CourseNodePO::getNodeIntroduce, CourseNodePO::getRelateNode, CourseNodePO::getNodeSize, CourseNodePO::getNodeColour, CourseNodePO::getVideoUrl, CourseNodePO::getTaskId, CourseNodePO::getXAxis, CourseNodePO::getYAxis, CourseNodePO::getQuestionCount, CourseNodePO::getCreatorId, CourseNodePO::getCreateTime, CourseNodePO::getUpdateTime)
                .eq(CourseNodePO::getCourseId, courseId));
    }
}
