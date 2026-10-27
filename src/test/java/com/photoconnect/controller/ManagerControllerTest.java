package com.photoconnect.controller;

import com.photoconnect.dto.ManagerDashboardStatsDto;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.ManagerOperationsService;
import com.photoconnect.service.SupportTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {
    @Mock private ManagerOperationsService operations;
    @Mock private SupportTicketService support;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ManagerController(operations, support)).build();
    }

    @Test
    void anonymousCannotAccessManagerDashboard() throws Exception {
        mvc.perform(get("/manager/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(operations, support);
    }

    @Test
    void customerCannotInvokeManagerMutation() throws Exception {
        mvc.perform(post("/manager/bookings/9/resolve-dispute")
                        .session(session(4L, UserRole.CUSTOMER))
                        .param("resolution", "PAY_PHOTOGRAPHER")
                        .param("notes", "attempt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verifyNoInteractions(operations, support);
    }

    @Test
    void photographerCannotAccessManagerQueue() throws Exception {
        mvc.perform(get("/manager/reports").session(session(5L, UserRole.PHOTOGRAPHER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verifyNoInteractions(operations, support);
    }

    @Test
    void managerDashboardUsesRealServiceQueries() throws Exception {
        ManagerDashboardStatsDto stats = new ManagerDashboardStatsDto(1, 2, 3, 4, 5);
        when(operations.dashboard(7L)).thenReturn(stats);
        when(operations.recentActivity(7L, 10)).thenReturn(List.of());

        mvc.perform(get("/manager/dashboard").session(session(7L, UserRole.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-dashboard"))
                .andExpect(model().attribute("stats", stats))
                .andExpect(model().attributeExists("activity"));
    }

    @Test
    void managerDisputeResolutionDelegatesExactActorAndBooking() throws Exception {
        mvc.perform(post("/manager/bookings/12/resolve-dispute")
                        .session(session(7L, UserRole.MANAGER))
                        .param("resolution", "REFUND_CUSTOMER")
                        .param("notes", "Evidence reviewed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/bookings/12"));
        verify(operations).resolveDispute(7L, 12L, "REFUND_CUSTOMER", "Evidence reviewed");
    }

    @Test
    void managerSuspendPhotographerDelegatesToOperations() throws Exception {
        mvc.perform(post("/manager/photographers/15/suspend")
                        .session(session(7L, UserRole.MANAGER))
                        .param("reason", "Severe conduct infraction")
                        .param("notes", "Investigation verified"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager/photographers/15"));
        verify(operations).suspendPhotographer(7L, 15L, "Severe conduct infraction", "Investigation verified");
    }


    private MockHttpSession session(Long id, UserRole role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", id);
        session.setAttribute("userRole", role.name());
        return session;
    }
}
