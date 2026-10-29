package com.photoconnect.controller;

import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.PlatformFeeStatus;
import com.photoconnect.repository.BookingRepository;
import com.photoconnect.repository.OperationalAuditLogRepository;
import com.photoconnect.repository.UserRepository;
import com.photoconnect.service.PlatformFeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin Platform Fee Controller Unit Tests")
class AdminPlatformFeeControllerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private OperationalAuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PlatformFeeService platformFeeService;

    @InjectMocks
    private AdminPlatformFeeController controller;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    @DisplayName("Non-admin cannot access admin platform fees page")
    void viewPlatformFees_nonAdmin_redirects() {
        session.setAttribute("userId", 10L);
        session.setAttribute("userRole", "CUSTOMER");

        Model model = new ConcurrentModel();
        String view = controller.viewPlatformFees(null, session, model);

        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    @DisplayName("Guest cannot access admin platform fees page and is redirected to login")
    void viewPlatformFees_guest_redirectsToLogin() {
        Model model = new ConcurrentModel();
        String view = controller.viewPlatformFees(null, session, model);

        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    @DisplayName("Admin can view platform fees page with real metrics and ledger records")
    void viewPlatformFees_admin_showsPage() {
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "ADMIN");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIncurred", new BigDecimal("5000000"));
        stats.put("totalPaid", new BigDecimal("2000000"));
        stats.put("totalOutstanding", new BigDecimal("3000000"));
        stats.put("photographersWithDebt", 3L);
        when(platformFeeService.getAdminFeeStats()).thenReturn(stats);
        when(platformFeeService.getCurrentFeeRate()).thenReturn(new BigDecimal("0.10"));
        when(platformFeeService.listAllFees(null)).thenReturn(Collections.emptyList());
        when(bookingRepository.calculateTotalPlatformCompletedRevenue()).thenReturn(new BigDecimal("50000000"));
        when(bookingRepository.summarizePhotographerCompletedRevenue()).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.viewPlatformFees(null, session, model);

        assertThat(view).isEqualTo("admin-platform-fees");
        assertThat(model.getAttribute("platformFeeIncurred")).isEqualTo(new BigDecimal("5000000"));
        assertThat(model.getAttribute("platformFeeCollected")).isEqualTo(new BigDecimal("2000000"));
        assertThat(model.getAttribute("platformFeeOutstanding")).isEqualTo(new BigDecimal("3000000"));
        assertThat(model.getAttribute("photographersWithDebt")).isEqualTo(3L);
    }

    @Test
    @DisplayName("Admin can export platform fee ledger as CSV")
    void exportFeesCsv_streamsCsvDownload() throws IOException {
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "ADMIN");

        PlatformFeeDto fee = new PlatformFeeDto();
        fee.setId(10L);
        fee.setBookingId(101L);
        fee.setPhotographerName("Studio Alpha");
        fee.setBookingAmount(new BigDecimal("2000000"));
        fee.setFeeRatePercent("10%");
        fee.setFeeAmount(new BigDecimal("200000"));
        fee.setPaidAmount(new BigDecimal("200000"));
        fee.setOutstandingAmount(BigDecimal.ZERO);
        fee.setStatus(PlatformFeeStatus.PAID);
        fee.setTransactionCode("PF-PAY-1234");

        when(platformFeeService.listAllFees(null)).thenReturn(List.of(fee));

        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.exportFeesCsv(null, session, response);

        assertThat(response.getContentType()).isEqualTo("text/csv; charset=UTF-8");
        assertThat(response.getHeader("Content-Disposition")).contains("platform-fees.csv");
        String content = response.getContentAsString();
        assertThat(content).contains("ID,Booking ID,Photographer");
        assertThat(content).contains("Studio Alpha");
        assertThat(content).contains("200000");
    }

    @Test
    @DisplayName("Admin updating fee rate with invalid value returns error")
    void updatePlatformFeeRate_invalidRate_returnsError() {
        session.setAttribute("userId", 1L);
        session.setAttribute("userRole", "ADMIN");

        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        String view = controller.updatePlatformFeeRate(new BigDecimal("35"), session, redirect);

        assertThat(view).isEqualTo("redirect:/admin/platform-fees");
        assertThat(redirect.getFlashAttributes()).containsKey("errorMessage");
    }
}
