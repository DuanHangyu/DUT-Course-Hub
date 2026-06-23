package com.human.digital.digitalhuman.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.CourseNodeService;
import com.human.digital.digitalhuman.repository.service.CourseService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.request.CourseNodeCreateCmd;
import com.human.digital.digitalhuman.service.model.request.CourseNodeModifyCmd;
import com.human.digital.digitalhuman.service.model.request.CourseNodeMoveCmd;
import com.human.digital.digitalhuman.service.model.request.CourseTemplateCmd;
import com.human.digital.digitalhuman.service.model.request.CourseTemplateQuery;
import com.human.digital.digitalhuman.service.model.response.CourseNodeDTO;
import com.human.digital.digitalhuman.service.model.response.CourseTemplateDTO;
import com.human.digital.digitalhuman.service.model.event.NodeModifyEvent;
import com.human.digital.digitalhuman.service.model.response.CourseTemplateStatusCountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模板课程应用服务
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseTemplateAppService {

    private final CourseService courseService;
    private final CourseNodeService courseNodeService;
    private final UserService userService;
    private final OssClientUtils ossClientUtils;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 模板课程分页查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public IPage<CourseTemplateDTO> pageQuery(CourseTemplateQuery query) {
        int pageNum = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getSize() == null ? 10 : query.getSize();

        IPage<CoursePO> page = courseService.pageQueryTemplate(
                pageNum,
                pageSize,
                query.getCourseName(),
                query.getState(),
                query.getSubject()
        );

        return page.convert(po -> {
            CourseTemplateDTO dto = CourseTemplateDTO.of(po, ossClientUtils::getSignedUrl);
            // 设置创建人名称
            if (po.getCreatorId() != null) {
                UserPO user = userService.getById(po.getCreatorId());
                dto.setCreateName(user != null ? user.getUserName() : "");
            }
            return dto;
        });
    }

    /**
     * 创建模板课程
     *
     * @param cmd 创建命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(CourseTemplateCmd cmd) {
        CoursePO coursePO = cmd.toPo();
        return courseService.save(coursePO);
    }

    /**
     * 编辑模板课程
     *
     * @param cmd 编辑命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean modify(CourseTemplateCmd cmd) {
        CoursePO existCourse = courseService.getById(cmd.getId());
        if (existCourse == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        CoursePO coursePO = cmd.toPo();
        coursePO.setId(cmd.getId());
        coursePO.setCreateTime(existCourse.getCreateTime());
        coursePO.setCreatorId(existCourse.getCreatorId());
        return courseService.updateById(coursePO);
    }

    /**
     * 删除模板课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        CoursePO existCourse = courseService.getById(id);
        if (existCourse == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }
        // 删除课程关联的节点
        List<CourseNodePO> nodes = courseNodeService.queryByCourseId(id);
        if (CollectionUtil.isNotEmpty(nodes)) {
            courseNodeService.removeByIds(nodes.stream().map(CourseNodePO::getId).collect(Collectors.toList()));
        }
        return courseService.removeById(id);
    }

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @Transactional(readOnly = true)
    public CourseTemplateDTO detail(Integer id) {
        CoursePO coursePO = courseService.getById(id);
        if (coursePO == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NOT_EXIST);
        }

        CourseTemplateDTO dto = CourseTemplateDTO.of(coursePO, ossClientUtils::getSignedUrl);
        if (coursePO.getCreatorId() != null) {
            UserPO user = userService.getById(coursePO.getCreatorId());
            dto.setCreateName(user != null ? user.getUserName() : "");
        }
        return dto;
    }

    /**
     * 获取课程节点列表
     *
     * @param courseId 课程ID
     * @return 节点列表
     */
    @Transactional(readOnly = true)
    public List<CourseNodeDTO> listNodes(Integer courseId) {
        List<CourseNodePO> nodes = courseNodeService.queryByCourseId(courseId);
        if (CollectionUtil.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream()
                .map(CourseNodeDTO::of)
                .collect(Collectors.toList());
    }

    /**
     * 创建课程节点
     *
     * @param createCmd 创建命令
     * @return 节点ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createNode(CourseNodeCreateCmd createCmd) {
        CourseNodePO courseNode = createCmd.toPo();
        courseNodeService.save(courseNode);
        // 处理双关问题 新节点关联的节点，也需要关联新节点
        List<Integer> relateNode = createCmd.getRelateNode();
        if (CollectionUtil.isNotEmpty(relateNode)) {
            addRelateNode(relateNode, courseNode.getId());
        }
        applicationEventPublisher.publishEvent(new NodeModifyEvent(this, courseNode));
        return courseNode.getId();
    }

    /**
     * 编辑课程节点
     *
     * @param modifyCmd 编辑命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean modifyNode(CourseNodeModifyCmd modifyCmd) {
        CourseNodePO courseNode = courseNodeService.getById(modifyCmd.getId());
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }
        // 只更新有值的字段
        var updateWrapper = Wrappers.lambdaUpdate(CourseNodePO.class)
                .eq(CourseNodePO::getId, modifyCmd.getId());

        if (modifyCmd.getNodeName() != null) {
            updateWrapper.set(CourseNodePO::getNodeName, modifyCmd.getNodeName());
        }
        if (modifyCmd.getNodeIntroduce() != null) {
            updateWrapper.set(CourseNodePO::getNodeIntroduce, modifyCmd.getNodeIntroduce());
        }
        if (modifyCmd.getRelateNode() != null) {
            updateWrapper.set(CourseNodePO::getRelateNode, JSONUtil.toJsonStr(modifyCmd.getRelateNode()));
        }
        if (modifyCmd.getNodeSize() != null) {
            updateWrapper.set(CourseNodePO::getNodeSize, modifyCmd.getNodeSize());
        }
        if (modifyCmd.getNodeColour() != null) {
            updateWrapper.set(CourseNodePO::getNodeColour, modifyCmd.getNodeColour());
        }
        if (modifyCmd.getVideo() != null) {
            updateWrapper.set(CourseNodePO::getVideoUrl, JSONUtil.toJsonStr(modifyCmd.getVideo()));
        }
        if (modifyCmd.getVideoDuration() != null) {
            updateWrapper.set(CourseNodePO::getVideoDuration, modifyCmd.getVideoDuration());
        }
        if (modifyCmd.getFiles() != null) {
            updateWrapper.set(CourseNodePO::getFilesUrl, JSONUtil.toJsonStr(modifyCmd.getFiles()));
        }

        courseNodeService.update(updateWrapper);
        // 修改了关联节点,1.原先没有，修改后新增， 2.原先有，修改后没有，3.修改了关联节点
        String newRelateNode = modifyCmd.getRelateNode() != null ? JSONUtil.toJsonStr(modifyCmd.getRelateNode()) : null;
        if (!Objects.equals(courseNode.getRelateNode(), newRelateNode)) {
            // 原先是空的，新增关联节点
            if (StrUtil.isBlank(courseNode.getRelateNode()) && CollectionUtil.isNotEmpty(modifyCmd.getRelateNode())) {
                addRelateNode(modifyCmd.getRelateNode(), courseNode.getId());
            }
            // 删除了关联节点
            else if (StrUtil.isNotBlank(courseNode.getRelateNode()) && CollectionUtil.isEmpty(modifyCmd.getRelateNode())) {
                deleteRelateNode(JSONUtil.toList(courseNode.getRelateNode(), Integer.class), courseNode.getId());
            }
            // 修改了关联节点
            else if (StrUtil.isNotBlank(courseNode.getRelateNode()) && CollectionUtil.isNotEmpty(modifyCmd.getRelateNode())) {
                List<Integer> originRelateList = JSONUtil.toList(courseNode.getRelateNode(), Integer.class);
                List<Integer> modifyRelateList = modifyCmd.getRelateNode();

                List<Integer> deleteNodeIds = originRelateList.stream().filter(origin -> !modifyRelateList.contains(origin)).toList();
                deleteRelateNode(deleteNodeIds, courseNode.getId());

                List<Integer> addNodeIds = modifyRelateList.stream().filter(modify -> !originRelateList.contains(modify)).toList();
                addRelateNode(addNodeIds, courseNode.getId());
            }
        }
        // 修改了视频，需要重新生成课程内容
        String newVideoUrl = modifyCmd.getVideo() != null ? JSONUtil.toJsonStr(modifyCmd.getVideo()) : null;
        if (!Objects.equals(courseNode.getVideoUrl(), newVideoUrl)) {
            courseNode = courseNodeService.getById(courseNode.getId());
            applicationEventPublisher.publishEvent(new NodeModifyEvent(this, courseNode));
        }
        return true;
    }

    /**
     * 删除课程节点
     *
     * @param modifyCmd 删除命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteNode(CourseNodeModifyCmd modifyCmd) {
        CourseNodePO courseNode = courseNodeService.getById(modifyCmd.getId());
        String relateNode = courseNode.getRelateNode();
        if (StrUtil.isNotBlank(relateNode)) {
            List<Integer> relateNodeIds = JSONUtil.toList(relateNode, Integer.class);
            deleteRelateNode(relateNodeIds, modifyCmd.getId());
        }
        return courseNodeService.removeById(modifyCmd.getId());
    }

    /**
     * 移动课程节点
     *
     * @param moveCmd 移动命令
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean moveNode(CourseNodeMoveCmd moveCmd) {
        CourseNodePO courseNode = courseNodeService.getById(moveCmd.getCourseNodeId());
        if (courseNode == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
        }
        return courseNodeService.update(Wrappers.lambdaUpdate(CourseNodePO.class)
                .set(StrUtil.isNotBlank(moveCmd.getXAxis()), CourseNodePO::getXAxis, moveCmd.getXAxis())
                .set(StrUtil.isNotBlank(moveCmd.getYAxis()), CourseNodePO::getYAxis, moveCmd.getYAxis())
                .eq(CourseNodePO::getId, moveCmd.getCourseNodeId()));
    }

    /**
     * 删除关联节点
     */
    private void deleteRelateNode(List<Integer> deleteNodeIds, Integer id) {
        if (CollectionUtil.isEmpty(deleteNodeIds)) {
            return;
        }
        List<CourseNodePO> courseNodes = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .in(CourseNodePO::getId, deleteNodeIds));
        if (CollectionUtil.isNotEmpty(courseNodes)) {
            for (CourseNodePO node : courseNodes) {
                if (node.startNode() || node.endNode()) {
                    continue;
                }
                List<Integer> nodeRelates = JSONUtil.toList(node.getRelateNode(), Integer.class);
                nodeRelates.remove(id);
                node.setRelateNode(JSONUtil.toJsonStr(nodeRelates));
            }
            courseNodeService.updateBatchById(courseNodes);
        }
    }

    /**
     * 添加关联节点
     */
    private void addRelateNode(List<Integer> relateNodeIds, Integer id) {
        if (CollectionUtil.isEmpty(relateNodeIds)) {
            return;
        }
        List<CourseNodePO> courseNodes = courseNodeService.list(Wrappers.lambdaQuery(CourseNodePO.class)
                .in(CourseNodePO::getId, relateNodeIds));
        if (CollectionUtil.isNotEmpty(courseNodes)) {
            for (CourseNodePO node : courseNodes) {
                if (node.startNode() || node.endNode()) {
                    continue;
                }
                List<Integer> nodeRelates = JSONUtil.toList(node.getRelateNode(), Integer.class);
                nodeRelates.add(id);
                node.setRelateNode(JSONUtil.toJsonStr(nodeRelates));
            }
            courseNodeService.updateBatchById(courseNodes);
        }
    }

    public List<String> subjectList() {
        List<CoursePO> coursePOS = courseService.queryAll();
        return coursePOS.stream()
                .map(CoursePO::getSubject)
                .distinct()
                .toList();
    }

    public CourseTemplateStatusCountDTO statusCount() {
        List<CoursePO> coursePOS = courseService.queryAll();
        return new CourseTemplateStatusCountDTO(
                coursePOS.stream().filter(course -> course.getState() == true).count(),
                coursePOS.stream().filter(course -> course.getState() == false).count()
        );
    }
}
