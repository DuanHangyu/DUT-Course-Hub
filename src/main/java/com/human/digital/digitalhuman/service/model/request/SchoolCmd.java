package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.SchoolPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 学校命令对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Data
@Schema(description = "学校命令")
public class SchoolCmd {

    @Schema(description = "ID（修改时使用）")
    private Integer id;

    @Schema(description = "学校名称")
    @NotBlank(message = "学校名称不能为空")
    private String schoolName;

    @Schema(description = "校长姓名")
    @NotBlank(message = "校长姓名不能为空")
    private String principalName;

    @Schema(description = "联系电话")
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @Schema(description = "地理位置")
    @NotBlank(message = "地理位置不能为空")
    private String location;

    @Schema(description = "状态（1=启用，0=禁用）")
    @NotNull(message = "状态不能为空")
    private Integer state;

    @Schema(description = "专属域名")
    @NotBlank(message = "专属域名不能为空")
    private String domain;

    @Schema(description = "访问策略（1=公网，2=IP白名单）")
    @NotNull(message = "访问策略不能为空")
    private Integer accessPolicy;

    @Schema(description = "IP白名单，多个用逗号分隔")
    private String ipWhitelist;

    @Schema(description = "学校logo URL")
    @NotBlank(message = "学校logo不能为空")
    private String logoUrl;

    @Schema(description = "功能模块授权")
    @NotNull(message = "功能模块授权不能为空")
    private List<String> authModules;

    @Schema(description = "最大注册学生数")
    @NotNull(message = "最大注册学生数不能为空")
    private Integer maxStudents;

    @Schema(description = "AI调用额度（万Tokens/月）")
    @NotNull(message = "AI调用额度不能为空")
    private Integer aiTokens;

    @Schema(description = "管理员名称")
    @NotBlank(message = "管理员名称不能为空")
    private String adminName;

    @Schema(description = "管理员账号")
    @NotBlank(message = "管理员账号不能为空")
    private String adminAccount;

    @Schema(description = "管理员密码")
    @NotBlank(message = "管理员密码不能为空")
    private String adminPassword;

    public SchoolPO toPo() {
        SchoolPO po = new SchoolPO();
        po.setSchoolName(schoolName);
        po.setPrincipalName(principalName);
        po.setPhone(phone);
        po.setLocation(location);
        po.setState(state);
        po.setDomain(domain);
        po.setAccessPolicy(accessPolicy);
        po.setIpWhitelist(ipWhitelist);
        po.setLogoUrl(logoUrl);
        // 将 authModules 列表转为 JSON 字符串存储
        if (authModules != null && !authModules.isEmpty()) {
            po.setAuthModules(String.join(",", authModules));
        }
        po.setMaxStudents(maxStudents);
        po.setAiTokens(aiTokens);
        return po;
    }
}
