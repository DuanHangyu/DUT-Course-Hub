package com.human.digital.digitalhuman.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学生导入结果 DTO
 *
 * @Author taoHouChao
 * @Date 14:05 2026/3/9
 */
@Data
@Schema(description = "学生导入结果")
public class StudentImportResultDTO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 全部成功
     *
     * @param count 成功数量
     * @return 导入结果
     */
    public static StudentImportResultDTO success(Integer count) {
        StudentImportResultDTO result = new StudentImportResultDTO();
        result.setSuccess(true);
        result.setSuccessCount(count);
        result.setFailCount(0);
        result.setErrorMessage(null);
        return result;
    }

    /**
     * 通用失败
     *
     * @param message 错误信息
     * @return 导入结果
     */
    public static StudentImportResultDTO fail(String message) {
        StudentImportResultDTO result = new StudentImportResultDTO();
        result.setSuccess(false);
        result.setSuccessCount(0);
        result.setFailCount(0);
        result.setErrorMessage(message);
        return result;
    }

    /**
     * 部分失败
     *
     * @param successCount 成功数量
     * @param failCount    失败数量
     * @param message      错误信息
     * @return 导入结果
     */
    public static StudentImportResultDTO fail(Integer successCount, Integer failCount, String message) {
        StudentImportResultDTO result = new StudentImportResultDTO();
        result.setSuccess(false);
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setErrorMessage(message);
        return result;
    }
}
