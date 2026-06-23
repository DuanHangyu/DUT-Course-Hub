package com.human.digital.digitalhuman.common.exception;

import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import lombok.Getter;

/**
 * @USER taoHouChao
 * @DATE 21:34 2025/6/22
 */
@Getter
public class AICallException extends RuntimeException{

    private final Integer code;
    private final String message;

    public AICallException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public AICallException(ErrorCodeEnums errorCodeEnums){
        super(errorCodeEnums.getMessage());
        this.code = errorCodeEnums.getCode();
        this.message = errorCodeEnums.getMessage();
    }
}
