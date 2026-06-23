package com.human.digital.digitalhuman.service.model.dto;

import lombok.Data;

import java.util.Objects;

/**
 * @Author taoHouChao
 * @Date 17:10 2025/6/12
 */
@Data
public class OkHttpResponse<T> {

    private Integer code;

    private T data;

    public boolean success(){
        return Objects.equals(0, code);
    }
}
