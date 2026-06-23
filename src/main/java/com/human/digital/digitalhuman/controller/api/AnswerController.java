package com.human.digital.digitalhuman.controller.api;

import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.common.annotation.OperateLog;
import com.human.digital.digitalhuman.service.AnswerAppService;
import com.human.digital.digitalhuman.service.model.response.QuestionAnswerLogDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @USER taoHouChao
 * @DATE 09:33 2025/6/19
 */
@RestController
@RequestMapping("/answer")
@Tag(name = "前台问答管理", description = "前台问答管理")
public class AnswerController {

    @Resource
    private AnswerAppService answerAppService;

    @GetMapping("/record")
    @Operation(summary = "获取问题答案记录")
    @OperateLog(value = "获取问题答案记录")
    public List<QuestionAnswerLogDTO> record(){
        return answerAppService.record(StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/boer-question")
    @Operation(summary = "获取问题答案记录")
    @OperateLog(value = "获取问题答案记录")
    public void boErQuestion(){

    }
}
