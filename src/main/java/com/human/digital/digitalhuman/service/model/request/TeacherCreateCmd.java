package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.UserPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教师创建命令
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/6
 */
@Data
@Schema(description = "教师创建命令")
public class TeacherCreateCmd {

    @Schema(description = "教师姓名")
    @NotBlank(message = "教师姓名不能为空")
    private String userName;

    @Schema(description = "登录账号")
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @Schema(description = "登录密码")
    @NotBlank(message = "登录密码不能为空")
    private String password;

    @Schema(description = "所属学校ID")
    @NotNull(message = "所属学校ID不能为空")
    private Integer schoolId;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Boolean state;

    public UserPO toPo() {
        UserPO userPO = new UserPO();
        userPO.setUserName(userName);
        userPO.setAccount(account);
        userPO.setPassword(password);
        userPO.setSchoolId(schoolId);
        userPO.setAvatar(avatar);
        userPO.setState(state);
        userPO.setRole(2); // 教师角色
        return userPO;
    }
}
