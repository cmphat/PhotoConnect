package com.photoconnect.controller;

import com.photoconnect.entity.Deposit;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.repository.DepositRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminPaymentController.class)
public class AdminPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositRepository depositRepository;

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 999L);
        session.setAttribute("userRole", "ADMIN");
        return session;
    }

    @Test
    void nonAdmin_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/payments"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminGetPayments_shouldReturn200AndModelAttributes() throws Exception {
        when(depositRepository.findAllWithDetails()).thenReturn(List.of());
        when(depositRepository.sumAmountByStatus(DepositStatus.PAID)).thenReturn(BigDecimal.valueOf(1000000));
        when(depositRepository.sumAmountByStatus(DepositStatus.REFUNDED)).thenReturn(BigDecimal.ZERO);
        when(depositRepository.sumAmountByStatus(DepositStatus.FORFEITED)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/admin/payments").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-payments"))
                .andExpect(model().attributeExists("deposits", "selectedStatus", "paidAmount", "refundedAmount", "forfeitedAmount", "totalCount"))
                .andExpect(model().attribute("selectedStatus", "ALL"))
                .andExpect(model().attribute("paidAmount", BigDecimal.valueOf(1000000)));
    }

    @Test
    void adminGetPayments_withStatusPaid_shouldFilterByStatus() throws Exception {
        when(depositRepository.findByStatusWithDetails(DepositStatus.PAID)).thenReturn(List.of());

        mockMvc.perform(get("/admin/payments").param("status", "PAID").session(adminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-payments"))
                .andExpect(model().attribute("selectedStatus", "PAID"));
    }
}
