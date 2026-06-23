package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改作业请求
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "修改作业请求")
public class HomeworkModifyCmd {

    @Schema(description = "作业ID")
    @NotNull(message = "作业ID不能为空")
    private Integer id;

    @Schema(description = "作业标题")
    @Size(max = 100, message = "作业标题不能超过100字")
    private String title;

    @Schema(description = "作业描述")
    @Size(max = 2000, message = "作业描述不能超过2000字")
    private String description;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;

    @Schema(description = "附件文件列表")
    private List<UploadDTO> attachments;
}
