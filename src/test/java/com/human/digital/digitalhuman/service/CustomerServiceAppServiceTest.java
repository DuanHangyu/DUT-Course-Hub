package com.human.digital.digitalhuman.service;

import com.human.digital.digitalhuman.service.model.dto.CustomerServiceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerServiceAppServiceTest {

    @Autowired
    private CustomerServiceAppService customerServiceAppService;

    @Test
    void testChat() {
        CustomerServiceDTO dto = new CustomerServiceDTO();
        dto.setMessage("我们班学生作业提交情况如何？");

        CustomerServiceDTO result = customerServiceAppService.chat(dto);

        assertNotNull(result);
        assertNotNull(result.getAnswer());
        assertNotNull(result.getSessionId());
    }

    @Test
    void testMultiTurnChat() {
        String sessionId = "test-session-001";

        // 第一轮
        CustomerServiceDTO dto1 = new CustomerServiceDTO();
        dto1.setMessage("我们班学生作业提交情况如何？");
        dto1.setSessionId(sessionId);
        customerServiceAppService.chat(dto1);

        // 第二轮（追问）
        CustomerServiceDTO dto2 = new CustomerServiceDTO();
        dto2.setMessage("具体是哪些学生没有提交？");
        dto2.setSessionId(sessionId);
        CustomerServiceDTO result = customerServiceAppService.chat(dto2);

        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
    }
}
