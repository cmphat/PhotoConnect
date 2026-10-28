package com.photoconnect.controller;

import com.photoconnect.entity.UserRole;
import com.photoconnect.service.PhotographerAiReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ManagerAiReviewControllerTest {
    @Mock private PhotographerAiReviewService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ManagerAiReviewController(service)).build();
    }

    @Test
    void anonymousCannotRequestAiReview() throws Exception {
        mvc.perform(post("/manager/photographers/21/ai-review"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(service);
    }

    @Test
    void customerCannotRequestAiReview() throws Exception {
        mvc.perform(post("/manager/photographers/21/ai-review").session(session(5L, UserRole.CUSTOMER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verifyNoInteractions(service);
    }

    @Test
    void managerRequestsOnlyOwnAuthenticatedSession() throws Exception {
        mvc.perform(post("/manager/photographers/21/ai-review").session(session(7L, UserRole.MANAGER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/photographers/21"));
        verify(service).assess(7L, 21L);
    }

    private MockHttpSession session(Long id, UserRole role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", id);
        session.setAttribute("userRole", role.name());
        return session;
    }
}
