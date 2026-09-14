package com.photoconnect.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppErrorControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(new AppErrorController())
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("HTML error dispatch for 404 should render error view with 404 status and message")
    void htmlErrorDispatch404() throws Exception {
        mockMvc.perform(get("/error")
                        .accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 404))
                .andExpect(model().attribute("errorTitle", "Page Not Found"))
                .andExpect(model().attribute("errorCode", "BOOKING_001_NOT_FOUND"));
    }

    @Test
    @DisplayName("HTML error dispatch for 403 should render error view with 403 status and message")
    void htmlErrorDispatch403() throws Exception {
        mockMvc.perform(get("/error")
                        .accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 403))
                .andExpect(model().attribute("errorTitle", "Access Denied"))
                .andExpect(model().attribute("errorCode", "AUTH_005_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("HTML error dispatch for 500 should render error view with 500 status and message")
    void htmlErrorDispatch500() throws Exception {
        mockMvc.perform(get("/error")
                        .accept(MediaType.TEXT_HTML)
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("statusCode", 500))
                .andExpect(model().attribute("errorTitle", "Server Error"))
                .andExpect(model().attribute("errorCode", "SYSTEM_002_INTERNAL_ERROR"));
    }

    @Test
    @DisplayName("JSON error dispatch for API forward should return structured ApiResponse JSON")
    void jsonErrorDispatchForApi() throws Exception {
        mockMvc.perform(get("/error")
                        .requestAttr(RequestDispatcher.FORWARD_REQUEST_URI, "/api/unknown-endpoint")
                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("BOOKING_001_NOT_FOUND"));
    }
}
