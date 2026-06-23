package com.human.digital.digitalhuman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseAssessStateEnums {
    ASSESSING(-1),
    HISTORY(0),
    COMPLETE(1),
    ;
    private final Integer code;
}
