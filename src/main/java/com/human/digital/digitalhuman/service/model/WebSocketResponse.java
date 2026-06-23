package com.human.digital.digitalhuman.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

/**
 * @Author taoHouChao
 * @Date 21:38 2025/6/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketResponse {

    private String content;

    private String type;

    private Boolean last;


    public static WebSocketResponse of(String content, String type, Boolean last){
        return new WebSocketResponse(content, type, last);
    }
    public static WebSocketResponse ofText(String content,  Boolean last) {
        return new WebSocketResponse(content, "text", last);
    }

    public static WebSocketResponse ofError(String content){
        return new WebSocketResponse(content, "error", true);
    }

    public static WebSocketResponse ofAssessedFinal(String content){
        return new WebSocketResponse(content, "assess-final", true);
    }

    public static WebSocketResponse ofArsText(String content, Boolean last){
        return new WebSocketResponse(content, "ars", last);
    }

    public static WebSocketResponse ofVoice(byte[] content,  Boolean last) {
        return new WebSocketResponse(Base64.getEncoder().encodeToString(content), "voice", last);
    }

    public static WebSocketResponse ofPong(){
        return new WebSocketResponse("pong", "pong", true);
    }
}
