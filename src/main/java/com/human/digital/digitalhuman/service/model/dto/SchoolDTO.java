package com.human.digital.digitalhuman.service.model.dto;

import com.human.digital.digitalhuman.repository.po.SchoolPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 学校数据传输对象
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Data
@Schema(description = "学校信息")
public class SchoolDTO {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "校长姓名")
    private String principalName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "地理位置")
    private String location;

    @Schema(description = "状态（1=启用，0=禁用）")
    private Integer state;

    @Schema(description = "专属域名")
    private String domain;

    @Schema(description = "访问策略（1=公网，2=IP白名单）")
    private Integer accessPolicy;

    @Schema(description = "IP白名单")
    private String ipWhitelist;

    @Schema(description = "学校logo URL")
    private String logoUrl;

    @Schema(description = "功能模块授权")
    private List<String> authModules;

    @Schema(description = "最大注册学生数")
    private Integer maxStudents;

    @Schema(description = "AI调用额度（万Tokens/月）")
    private Integer aiTokens;

    @Schema(description = "学生人数（默认0）")
    private Integer studentCount = 0;

    @Schema(description = "教师人数（默认0）")
    private Integer teacherCount = 0;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "管理员名称")
    private String adminName;

    @Schema(description = "管理员账号")
    private String adminAccount;

    @Schema(description = "管理员密码")
    private String adminPassword;

    public static SchoolDTO of(SchoolPO po, String adminName, String adminAccount, String adminPassword, Integer studentCount, Integer teacherCount) {
        SchoolDTO dto = new SchoolDTO();
        dto.setId(po.getId());
        dto.setSchoolName(po.getSchoolName());
        dto.setPrincipalName(po.getPrincipalName());
        dto.setPhone(po.getPhone());
        dto.setLocation(po.getLocation());
        dto.setState(po.getState());
        dto.setDomain(po.getDomain());
        dto.setAccessPolicy(po.getAccessPolicy());
        dto.setIpWhitelist(po.getIpWhitelist());
        dto.setLogoUrl(po.getLogoUrl());
        dto.setMaxStudents(po.getMaxStudents());
        dto.setAiTokens(po.getAiTokens());
        dto.setStudentCount(studentCount != null ? studentCount : 0);
        dto.setTeacherCount(teacherCount != null ? teacherCount : 0);
        dto.setCreateTime(po.getCreateTime());
        dto.setAdminName(adminName);
        dto.setAdminAccount(adminAccount);
        dto.setAdminPassword(adminPassword);
        // 解析 authModules
        if (po.getAuthModules() != null && !po.getAuthModules().isEmpty()) {
            dto.setAuthModules(Arrays.asList(po.getAuthModules().split(",")));
        }
        return dto;
    }
}
