package com.human.digital.digitalhuman.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.repository.po.CourseStudentRelationPO;

import java.util.List;
import java.util.Map;

/**
 * 课程服务接口
 *
 * @Author taoHouChao
 * @Date 19:24 2025/6/11
 */
public interface CourseService extends IService<CoursePO> {
    /**
     * 分页查询
     *
     * @param teacherId  教师ID
     * @param page       页码
     * @param size       每页数量
     * @param courseName 课程名称
     * @param state      课程状态
     * @return IPage<CoursePO>
     */
    IPage<CoursePO> pageQuery(Integer teacherId, Integer page, Integer size, String courseName, Boolean state);

    /**
     * 获取所有启用的课程
     *
     * @return 课程列表
     */
    List<CoursePO> enableList();

    /**
     * 根据学校ID获取启用的课程列表
     *
     * @param schoolId 学校ID
     * @return 课程列表
     */
    List<CoursePO> enableListBySchoolId(Integer schoolId, String subject);

    /**
     * 根据ID列表查询课程
     *
     * @param courseIds 课程ID列表
     * @return 课程映射表
     */
    Map<Integer, CoursePO> queryByIds(List<Integer> courseIds);

    /**
     * 根据课程名称查询课程ID列表
     *
     * @param courseName 课程名称
     * @return 课程ID列表
     */
    List<Integer> queryIdByName(String courseName);

    /**
     * 模板课程分页查询
     *
     * @param page       页码
     * @param size       每页数量
     * @param courseName 课程名称
     * @param state      课程状态
     * @param subject    所属学科
     * @return IPage<CoursePO>
     */
    IPage<CoursePO> pageQueryTemplate(Integer page, Integer size, String courseName, Boolean state, String subject);

    /**
     * 获取所有课程
     *
     * @return 课程列表
     */
    List<CoursePO> queryAll();

    /**
     * 校本课程分页查询
     *
     * @param pageNum   页码
     * @param pageSize 每页数量
     * @param schoolId 学校ID
     * @param name     课程名称
     * @param state    课程状态
     * @param subject  所属学科
     * @return 分页结果
     */
    IPage<CoursePO> pageQuerySchool(int pageNum, int pageSize, Integer schoolId, String name, Boolean state, String subject);

    /**
     * 校本课程分页查询（我的课程：创建人或关联老师）
     *
     * @param pageNum    页码
     * @param pageSize  每页数量
     * @param schoolId  学校ID
     * @param name      课程名称
     * @param state     课程状态
     * @param subject   所属学科
     * @param creatorId 创建人ID
     * @return 分页结果
     */
    IPage<CoursePO> pageQuerySchoolMyCourse(int pageNum, int pageSize, Integer schoolId, String name, Boolean state, String subject, Integer creatorId);

    /**
     * 根据邀请码查询课程
     *
     * @param inviteCode 邀请码
     * @return 课程
     */
    CoursePO getByInviteCode(String inviteCode);

    /**
     * 获取课程学生关联
     *
     * @param courseId  课程ID
     * @param studentId 学生ID
     * @return 关联
     */
    CourseStudentRelationPO getCourseStudentRelation(Integer courseId, Integer studentId);

    /**
     * 批量保存课程学生关联
     *
     * @param relations 关联列表
     */
    void saveBatchCourseStudentRelation(List<CourseStudentRelationPO> relations);

    /**
     * 统计课程学生人数
     *
     * @param courseId 课程ID
     * @return 学生人数
     */
    Long countCourseStudent(Integer courseId);
}
