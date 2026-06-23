package com.human.digital.digitalhuman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnums {
    SUPER_ADMIN(0),
    SCHOOL_ADMIN(1),
    TEACHER(2),
    ;

    private final int code;
}
