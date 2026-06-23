package com.human.digital.digitalhuman.controller.api;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.human.digital.digitalhuman.common.utils.ThreadPoolUtils;
import com.human.digital.digitalhuman.service.DigitalTeacherService;
import com.human.digital.digitalhuman.service.handler.TtsHandler;
import com.human.digital.digitalhuman.service.model.dto.QuestionPaperDTO;
import com.human.digital.digitalhuman.service.model.dto.QuestionSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Base64;

/**
 * @Author taoHouChao
 * @Date 12:23 2025/6/7
 */
@RestController
@RequestMapping("/digital")
@Slf4j
@Tag(name = "数字导师", description = "数字导师")
public class DigitalTeacherController {

    @Resource
    private TtsHandler ttsHandler;

    @Resource
    private DigitalTeacherService digitalTeacherService;

    @GetMapping("/question")
    @Operation(summary = "查询问题", description = "查询波尔问题")
    public QuestionSummaryDTO question(@RequestParam("question") String question) {
        return digitalTeacherService.queryQuestion(StpUtil.getLoginIdAsInt(), question);
    }

    @GetMapping("/papers")
    @Operation(summary = "查询论文", description = "查询波尔论文")
    public QuestionPaperDTO papers(@RequestParam("id") Long id){
        return digitalTeacherService.queryPapers(id);
    }

    @GetMapping("/test7")
    public void test7() {
        ttsHandler.process("你好我是人工智能小米我可以回答你关于在生活文化代码数学等方方面面的问题可以随时告诉我哦", (speech) -> {
            log.info("content:{}", Base64.getEncoder().encodeToString(speech));
        });
    }

    @GetMapping("/test8")
    public DeferredResult<String> test8(){
        DeferredResult<String> deferredResult = new DeferredResult<>(10000L);
        // 获取当前SaToken上下文
        String tokenValue = StpUtil.getTokenValue();
        // 提取当前上下文信息
        ThreadPoolUtils.execute(() -> {
            try {
                SaTokenContextMockUtil.setMockContext(()->{
                    StpUtil.setTokenValueToStorage(tokenValue);
                });
                Thread.sleep(5000);
                deferredResult.setResult("success");
            } catch (Exception e) {
                deferredResult.setErrorResult("error");
            }
        });
        return deferredResult;
    }
}
