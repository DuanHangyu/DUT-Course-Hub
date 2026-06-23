package com.human.digital.digitalhuman.common.exception;

import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;

/**
 * @USER taoHouChao
 * @DATE 13:44 2025/6/23
 */
public class AssessedException extends RuntimeException{

    private final Integer code;
    private final String message;

    public AssessedException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AssessedException(ErrorCodeEnums errorCodeEnums){
        super(errorCodeEnums.getMessage());
        this.code = errorCodeEnums.getCode();
        this.message = errorCodeEnums.getMessage();
    }
}
