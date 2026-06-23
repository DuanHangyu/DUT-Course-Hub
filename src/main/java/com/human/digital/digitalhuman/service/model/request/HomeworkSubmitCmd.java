package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.service.model.dto.UploadDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交作业请求
 *
 * @Author taoHouChao
 * @Date 2026/03/11
 */
@Data
@Schema(description = "提交作业请求")
public class HomeworkSubmitCmd {

    @Schema(description = "作业ID")
    @NotNull(message = "作业ID不能为空")
    private Integer homeworkId;

    @Schema(description = "提交文件列表")
    @NotEmpty(message = "提交文件不能为空")
    private List<UploadDTO> files;
}
