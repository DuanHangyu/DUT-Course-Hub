package com.human.digital.digitalhuman.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程资料文件
 *
 * @Author taoHouChao
 * @Date 2026/4/11
 */
@Data
@TableName("course_material_file")
public class CourseMaterialFilePO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer courseId;
    private Integer folderId;
    private String fileName;
    private String ossKey;
    private Long fileSize;
    private String fileType;
    private Integer sortOrder;
    private Integer creatorId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
