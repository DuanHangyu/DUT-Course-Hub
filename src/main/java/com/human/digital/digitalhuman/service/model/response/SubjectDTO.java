package com.human.digital.digitalhuman.service.model.response;

import lombok.Data;

import com.human.digital.digitalhuman.repository.po.SubjectPO;

/**
 * 学科 DTO
 *
 * @Author taoHouChao
 * @Date 10:20 2026/3/10
 */
@Data
public class SubjectDTO {

    /**
     * 学科ID
     */
    private Integer id;

    /**
     * 学科名称
     */
    private String name;

    /**
     * 课程数量
     */
    private Integer courseCount;

    /**
     * 构建 DTO
     *
     * @param po          学科实体
     * @param courseCount 课程数量
     * @return SubjectDTO
     */
    public static SubjectDTO of(SubjectPO po, Integer courseCount) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(po.getId());
        dto.setName(po.getName());
        dto.setCourseCount(courseCount);
        return dto;
    }
}
