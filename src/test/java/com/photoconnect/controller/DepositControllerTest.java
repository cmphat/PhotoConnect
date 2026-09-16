package com.photoconnect.controller;

import com.photoconnect.dto.DemoPaymentRequest;
import com.photoconnect.dto.DepositViewDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.BookingStatus;
import com.photoconnect.entity.DemoPaymentMethod;
import com.photoconnect.entity.DepositStatus;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.exception.UnauthorizedException;
import com.photoconnect.service.BookingService;
import com.photoconnect.service.DemoQrCodeService;
import com.photoconnect.service.DepositService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DepositController.class)
@Import(DemoQrCodeService.class)
class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositService depositService;

    @MockBean
    private BookingService bookingService;

    private Booking booking;
    private DepositViewDto deposit;
    private MockHttpSession customerSession;

    @BeforeEach
    void setUp() {
        User customer = new User();
        customer.setId(10L);
        customer.setRole(UserRole.CUSTOMER);
        customer.setFullName("Customer Name");

        User photographer = new User();
        photographer.setFullName("Photographer Account");
        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(20L);
        profile.setDisplayName("North Light Studio");
        profile.setUser(photographer);

        booking = new Booking(customer, profile, LocalDate.of(2026, 10, 10),
                LocalTime.of(9, 30), "Studio One", "Portrait", new BigDecimal("2000000.00"));
        booking.setId(100L);
        booking.setStatus(BookingStatus.ACCEPTED);

        deposit = new DepositViewDto();
        deposit.setId(1L);
        deposit.setBookingId(100L);
        deposit.setAmount(new BigDecimal("600000.00"));
        deposit.setStatus(DepositStatus.PENDING);
        deposit.setPaymentReference("PC-20260915-ABCDEF12");

        customerSession = new MockHttpSession();
        customerSession.setAttribute("userId", 10L);
        customerSession.setAttribute("userRole", "CUSTOMER");
    }

    @Test
    void legacyDepositRouteRedirectsToCanonicalCheckout() throws Exception {
        mockMvc.perform(get("/bookings/100/deposit").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100/deposit/checkout"));
    }

    @Test
    void checkoutRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/bookings/100/deposit/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(depositService, never()).getOrCreateDepositForBooking(anyLong(), anyLong());
    }

    @Test
    void photographerCannotOpenOrProcessCustomerCheckout() throws Exception {
        customerSession.setAttribute("userRole", "PHOTOGRAPHER");

        mockMvc.perform(get("/bookings/100/deposit/checkout").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        mockMvc.perform(post("/bookings/100/deposit/process").session(customerSession)
                        .param("paymentMethod", "DEMO_QR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
        verify(depositService, never()).processDemoPayment(anyLong(), anyLong(), any());
    }

    @Test
    void customerCheckoutShowsServerValuesAndLocalQr() throws Exception {
        stubCheckout();

        mockMvc.perform(get("/bookings/100/deposit/checkout").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit"))
                .andExpect(model().attribute("booking", org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(100L))))
                .andExpect(model().attribute("deposit", deposit))
                .andExpect(model().attribute("remainingBalance", new BigDecimal("1400000.00")))
                .andExpect(model().attribute("demoQrPayload", org.hamcrest.Matchers.containsString("BOOKING:100")))
                .andExpect(forwardedUrl("/WEB-INF/views/deposit.jsp"));
    }

    @Test
    void alreadyPaidCheckoutRedirectsToReceipt() throws Exception {
        deposit.setStatus(DepositStatus.PAID);
        when(bookingService.getBookingForCustomer(100L, 10L)).thenReturn(booking);
        when(depositService.getOrCreateDepositForBooking(100L, 10L)).thenReturn(deposit);

        mockMvc.perform(get("/bookings/100/deposit/checkout").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100/deposit/receipt"));
    }

    @Test
    void processIgnoresBrowserAmountAndPassesOnlyEphemeralPaymentInput() throws Exception {
        when(depositService.processDemoPayment(eq(100L), eq(10L), any(DemoPaymentRequest.class)))
                .thenReturn(deposit);

        mockMvc.perform(post("/bookings/100/deposit/process").session(customerSession)
                        .param("paymentMethod", "DEMO_CARD")
                        .param("cardholderName", "Demo Customer")
                        .param("cardNumber", "4242 4242 4242 4242")
                        .param("expiry", "12/40")
                        .param("cvv", "123")
                        .param("amount", "1.00")
                        .param("transactionReference", "ATTACKER-REF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100/deposit/result"));

        ArgumentCaptor<DemoPaymentRequest> captor = ArgumentCaptor.forClass(DemoPaymentRequest.class);
        verify(depositService).processDemoPayment(eq(100L), eq(10L), captor.capture());
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo("DEMO_CARD");
        assertThat(ArraysOfFields.names(DemoPaymentRequest.class)).doesNotContain("amount", "transactionReference");
    }

    @Test
    void cancelUsesSessionCustomerAndRedirectsToResult() throws Exception {
        when(depositService.cancelDemoPayment(100L, 10L)).thenReturn(deposit);

        mockMvc.perform(post("/bookings/100/deposit/cancel").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100/deposit/result"));
        verify(depositService).cancelDemoPayment(100L, 10L);
    }

    @Test
    void receiptRequiresOwnership() throws Exception {
        when(bookingService.getBookingForCustomer(100L, 10L))
                .thenThrow(new UnauthorizedException("You do not own this booking."));

        mockMvc.perform(get("/bookings/100/deposit/receipt").session(customerSession))
                .andExpect(status().isForbidden());
        verify(depositService, never()).getCustomerDeposit(anyLong(), anyLong());
    }

    @Test
    void successfulReceiptDisplaysAuthoritativeServerModel() throws Exception {
        deposit.setStatus(DepositStatus.PAID);
        deposit.setPaymentMethod(DemoPaymentMethod.DEMO_CARD);
        deposit.setPaidAt(LocalDateTime.of(2026, 9, 15, 14, 30));
        when(bookingService.getBookingForCustomer(100L, 10L)).thenReturn(booking);
        when(depositService.getCustomerDeposit(100L, 10L)).thenReturn(deposit);

        mockMvc.perform(get("/bookings/100/deposit/receipt").session(customerSession))
                .andExpect(status().isOk())
                .andExpect(view().name("deposit-receipt"))
                .andExpect(model().attribute("deposit", deposit))
                .andExpect(model().attribute("remainingBalance", new BigDecimal("1400000.00")))
                .andExpect(forwardedUrl("/WEB-INF/views/deposit-receipt.jsp"));
    }

    @Test
    void unpaidReceiptRedirectsToResult() throws Exception {
        when(bookingService.getBookingForCustomer(100L, 10L)).thenReturn(booking);
        when(depositService.getCustomerDeposit(100L, 10L)).thenReturn(deposit);

        mockMvc.perform(get("/bookings/100/deposit/receipt").session(customerSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/100/deposit/result"));
    }

    private void stubCheckout() {
        when(bookingService.getBookingForCustomer(100L, 10L)).thenReturn(booking);
        when(depositService.getOrCreateDepositForBooking(100L, 10L)).thenReturn(deposit);
    }

    private static final class ArraysOfFields {
        private static java.util.List<String> names(Class<?> type) {
            return java.util.Arrays.stream(type.getDeclaredFields())
                    .map(java.lang.reflect.Field::getName)
                    .toList();
        }
    }
}
