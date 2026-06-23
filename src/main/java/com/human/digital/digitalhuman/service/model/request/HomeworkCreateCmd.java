package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.HomeworkPO;
import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建作业请求
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "创建作业请求")
public class HomeworkCreateCmd {

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private Integer courseId;

    @Schema(description = "作业标题")
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 100, message = "作业标题不能超过100字")
    private String title;

    @Schema(description = "作业描述")
    @NotBlank(message = "作业描述不能为空")
    @Size(max = 2000, message = "作业描述不能超过2000字")
    private String description;

    @Schema(description = "截止时间")
    @NotNull(message = "截止时间不能为空")
    private LocalDateTime deadline;

    @Schema(description = "附件文件列表")
    private List<UploadDTO> attachments;

    /**
     * 转换为 PO 对象
     *
     * @param teacherId 教师ID
     * @return HomeworkPO
     */
    public HomeworkPO toPo(Integer teacherId) {
        HomeworkPO po = new HomeworkPO();
        po.setCourseId(courseId);
        po.setTitle(title);
        po.setDescription(description);
        po.setDeadline(deadline);
        po.setTeacherId(teacherId);
        po.setDeleted(0);
        return po;
    }
}
