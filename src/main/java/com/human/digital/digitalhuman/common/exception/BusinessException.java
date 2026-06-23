package com.human.digital.digitalhuman.common.exception;

import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import lombok.Getter;

/**
 * @Author taoHouChao
 * @Date 12:26 2025/6/7
 */
@Getter
public class BusinessException extends RuntimeException{

    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCodeEnums errorCodeEnums){
        super(errorCodeEnums.getMessage());
        this.code = errorCodeEnums.getCode();
        this.message = errorCodeEnums.getMessage();
    }
}
