package com.human.digital.digitalhuman.service.model.request;

import com.human.digital.digitalhuman.repository.po.UserPO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author taoHouChao
 * @Date 14:29 2025/6/11
 */
@Data
@Schema(description = "用户创建命令")
public class UserCreateCmd {

    @Schema(description = "账户")
    @NotEmpty(message = "账户不能为空")
    private String account;

    @Schema(description = "学生名称")
    @NotEmpty(message = "学生名称不能为空")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "密码")
    @NotEmpty(message = "密码不能为空")
    private String password;

    @Schema(description = "状态")
    @NotNull(message = "状态不能为空")
    private Boolean state;

    @Schema(description = "角色（0=超管,1=学校管理员,2=教师）；超管创建时必填，学校管理员创建时忽略")
    private Integer role;

    @Schema(description = "所属学校ID；超管创建时必填，学校管理员创建时忽略（强制为本校）")
    private Integer schoolId;

    public UserPO toPo(){
        UserPO userPO = new UserPO();
        userPO.setAccount(account);
        userPO.setUserName(userName);
        userPO.setPhone(phone);
        userPO.setPassword(password);
        userPO.setState(state);
        userPO.setRole(role);
        userPO.setSchoolId(schoolId);
        return userPO;
    }
}
