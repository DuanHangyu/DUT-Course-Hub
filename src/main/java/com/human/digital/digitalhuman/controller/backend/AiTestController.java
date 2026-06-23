package com.human.digital.digitalhuman.controller.backend;

import com.human.digital.digitalhuman.service.handler.FileIdentifyHandler;
import com.human.digital.digitalhuman.service.model.dto.AiQuestionDTO;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @USER taoHouChao
 * @DATE 16:40 2025/9/15
 */
@RestController
@RequestMapping("/ai-test")
public class AiTestController {

    @Resource
    private ChatClient client;

    @Resource
    private FileIdentifyHandler fileIdentifyHandler;

    @GetMapping("/question")
    public AiQuestionDTO question(String question){
        return client.prompt()
                .user(question)
                .call().entity(AiQuestionDTO.class);
    }

    @GetMapping("/convert-video-to-text")

    public String convertVideoToText(String url){
        return fileIdentifyHandler.convertVideoToText(url,
                taskId -> {},
                error -> {});
    }
}
