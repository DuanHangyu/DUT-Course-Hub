package com.human.digital.digitalhuman.service.model.dto;

import lombok.Data;

/**
 * @USER taoHouChao
 * @DATE 22:58 2025/6/28
 */
@Data
public class BoerResult<T> {

    private int code;

    private T data;
}
