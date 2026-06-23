package com.human.digital.digitalhuman.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.human.digital.digitalhuman.repository.po.CoursePO;
import com.human.digital.digitalhuman.service.model.dto.CourseCompletionItemDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CourseMapper extends BaseMapper<CoursePO> {

    /**
     * 查询学校所有课程的完成度统计
     * 通过一条SQL在数据库层面完成聚合计算，避免大量数据加载到内存
     *
     * @param schoolId 学校ID
     * @return 课程完成度列表
     */
    @Select("""
            SELECT
                                            c.id AS courseId,
                                            c.course_name AS courseName,
                                            -- 1. 计算课程完成率 (已完成人数 / 总人数)
                                            -- 使用 COALESCE 和 NULLIF 防止除以0，保留1位小数逻辑通常在Java层处理，这里返回小数
                                            COALESCE(
                                                CAST(COUNT(DISTINCT CASE WHEN comp.is_completed = 1 THEN csr.student_id END) AS DECIMAL(10,2)) /\s
                                                NULLIF(COUNT(DISTINCT csr.student_id), 0),\s
                                                0
                                            ) AS completionRate,
            
                                            -- 2. 总选课人数
                                            COUNT(DISTINCT csr.student_id) AS totalCount,
            
                                            -- 3. 已完成人数
                                            COUNT(DISTINCT CASE WHEN comp.is_completed = 1 THEN csr.student_id END) AS completedCount,
            
                                            -- 4. 未完成人数
                                            COUNT(DISTINCT CASE WHEN comp.is_completed = 0 THEN csr.student_id END) AS uncompletedCount
            
                                        FROM course c
                                        -- 关联选课关系表
                                        LEFT JOIN course_student_relation csr ON c.id = csr.course_id
            
                                        -- 5. 统计课程的“有效”总节点数 (排除开始/结束)
                                        LEFT JOIN (
                                            SELECT course_id, COUNT(*) AS total_nodes
                                            FROM course_node
                                            WHERE node_name NOT IN ('开始', '结束')
                                            GROUP BY course_id
                                        ) cn_cnt ON c.id = cn_cnt.course_id
            
                                        -- 6. 统计学生完成的“有效”节点数
                                        LEFT JOIN (
                                            SELECT
                                                sr.student_id,
                                                cn.course_id,
                                                COUNT(DISTINCT sr.course_node_id) AS completed_nodes,
                                                -- 判断是否完成：如果完成的节点数 >= 课程有效总节点数，则视为完成
                                                CASE\s
                                                    WHEN COUNT(DISTINCT sr.course_node_id) >= cn_cnt_inner.total_nodes THEN 1\s
                                                    ELSE 0\s
                                                END AS is_completed
                                            FROM student_course_node_study_record sr
                                            JOIN course_node cn ON sr.course_node_id = cn.id
                                                -- 【关键点】：这里也必须排除开始/结束节点，保证分子分母口径一致
                                                AND cn.node_name NOT IN ('开始', '结束')
                                            -- 再次关联子查询以获取该课程的总节点数用于比较
                                            JOIN (
                                                SELECT course_id, COUNT(*) AS total_nodes
                                                FROM course_node
                                                WHERE node_name NOT IN ('开始', '结束')
                                                GROUP BY course_id
                                            ) cn_cnt_inner ON cn.course_id = cn_cnt_inner.course_id
                                            WHERE sr.completed = 1
                                            GROUP BY sr.student_id, cn.course_id
                                        ) comp ON csr.student_id = comp.student_id AND c.id = comp.course_id
            
                                        WHERE c.school_id = #{schoolId}
                                        GROUP BY c.id, c.course_name
            """)
    List<CourseCompletionItemDTO> selectCourseCompletionStats(@Param("schoolId") Integer schoolId);
}
