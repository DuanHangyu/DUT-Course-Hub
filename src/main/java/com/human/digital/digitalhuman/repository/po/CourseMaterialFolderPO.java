package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程资料文件夹
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@TableName("course_material_folder")
public class CourseMaterialFolderPO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer courseId;
    private String folderName;
    private Integer sortOrder;
    private Integer creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
