package com.choi.springmall2.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorController.class)
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거
class ErrorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("커스텀 에러 페이지가 올바르게 렌더링되는지 확인")
    void handleCustomError() throws Exception {
        // given
        String url = "/error/custom";

        String errorMessage = "Test error message";
        String errorException = "Test exception";
        String errorStatus = "404";
        String errorPath = "/invalid-path";

        // when
        ResultActions result = mockMvc.perform(get(url)
                        .requestAttr("message", errorMessage)  // request 속성 설정
                        .requestAttr("exception", errorException)
                        .requestAttr("status", errorStatus)
                        .requestAttr("path", errorPath));

        // then
        result.andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("message", errorMessage))
                .andExpect(model().attribute("exception", errorException))
                .andExpect(model().attribute("status", errorStatus))
                .andExpect(model().attribute("path", errorPath));
    }
}