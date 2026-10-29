package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerFeeSummaryDto;
import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.PlatformFeeStatus;
import com.photoconnect.service.PlatformFeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Photographer Finance Controller Unit Tests")
class PhotographerFinanceControllerTest {

    @Mock
    private PlatformFeeService platformFeeService;

    @InjectMocks
    private PhotographerFinanceController controller;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    @DisplayName("Guest cannot access finance page and is redirected to login")
    void viewFinance_guest_redirectsToLogin() {
        Model model = new ConcurrentModel();
        String view = controller.viewFinance(session, model);
        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    @DisplayName("Customer cannot access photographer finance page")
    void viewFinance_customer_redirectsHome() {
        session.setAttribute("userId", 100L);
        session.setAttribute("userRole", "CUSTOMER");

        Model model = new ConcurrentModel();
        String view = controller.viewFinance(session, model);
        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    @DisplayName("Photographer accesses finance page and sees fee summary")
    void viewFinance_photographer_showsView() {
        session.setAttribute("userId", 200L);
        session.setAttribute("userRole", "PHOTOGRAPHER");

        PhotographerFeeSummaryDto summary = new PhotographerFeeSummaryDto();
        summary.setGrossEarnings(new BigDecimal("10000000"));
        summary.setFeesIncurred(new BigDecimal("1000000"));
        when(platformFeeService.getPhotographerSummary(200L)).thenReturn(summary);

        Model model = new ConcurrentModel();
        String view = controller.viewFinance(session, model);

        assertThat(view).isEqualTo("photographer-finance");
        assertThat(model.getAttribute("summary")).isEqualTo(summary);
    }

    @Test
    @DisplayName("Photographer can pay platform fee via Demo Payment")
    void payPlatformFee_success_redirectsWithFlash() {
        session.setAttribute("userId", 200L);
        session.setAttribute("userRole", "PHOTOGRAPHER");

        PlatformFeeDto feeDto = new PlatformFeeDto();
        feeDto.setId(5L);
        feeDto.setTransactionCode("PF-PAY-12345");
        feeDto.setPaidAmount(new BigDecimal("200000"));
        feeDto.setStatus(PlatformFeeStatus.PAID);

        when(platformFeeService.processPhotographerFeePayment(eq(200L), eq(5L), any(BigDecimal.class), eq("DEMO_PAYMENT")))
                .thenReturn(feeDto);

        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.payPlatformFee(5L, new BigDecimal("200000"), "DEMO_PAYMENT", session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/photographer/finance");
        assertThat(redirectAttributes.getFlashAttributes()).containsKey("successMessage");
    }
}
