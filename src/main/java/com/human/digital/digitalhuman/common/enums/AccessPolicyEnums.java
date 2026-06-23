package com.human.digital.digitalhuman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 访问策略枚举
 *
 * @Author taoHouChao
 * @Date 10:00 2026/3/4
 */
@Getter
@AllArgsConstructor
public enum AccessPolicyEnums {

    PUBLIC(1, "公网访问"),
    IP_WHITELIST(2, "IP白名单");

    private final int code;
    private final String desc;
}
