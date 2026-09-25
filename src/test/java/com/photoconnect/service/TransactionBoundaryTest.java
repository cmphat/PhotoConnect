package com.photoconnect.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionBoundaryTest {

    @Test
    void customerDashboardAggregationIsReadOnlyForOpenInViewFalseCompatibility() throws Exception {
        Method method = CustomerDashboardServiceImpl.class.getMethod("getDashboard", Long.class);
        Transactional transaction = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (transaction == null) {
            transaction = AnnotatedElementUtils.findMergedAnnotation(CustomerDashboardServiceImpl.class, Transactional.class);
        }
        assertThat(transaction).isNotNull();
        assertThat(transaction.readOnly()).isTrue();
    }

    @Test
    void photographerDashboardAggregationIsReadOnlyForOpenInViewFalseCompatibility() throws Exception {
        Method method = PhotographerDashboardServiceImpl.class.getMethod("getDashboard", Long.class);
        Transactional transaction = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (transaction == null) {
            transaction = AnnotatedElementUtils.findMergedAnnotation(PhotographerDashboardServiceImpl.class, Transactional.class);
        }
        assertThat(transaction).isNotNull();
        assertThat(transaction.readOnly()).isTrue();
    }

    @Test
    void multiWriteBusinessOperationsHaveTransactionBoundaries() throws Exception {
        assertTransactional(BookingServiceImpl.class, "createBooking", Long.class, Long.class,
                com.photoconnect.dto.BookingRequest.class);
        assertTransactional(DepositServiceImpl.class, "getOrCreateDepositForBooking", Long.class, Long.class);
        assertTransactional(DepositServiceImpl.class, "simulateSuccessfulPayment", Long.class, Long.class);
        assertTransactional(DepositServiceImpl.class, "processDemoPayment", Long.class, Long.class,
                com.photoconnect.dto.DemoPaymentRequest.class);
        assertTransactional(DepositServiceImpl.class, "cancelDemoPayment", Long.class, Long.class);
        assertTransactional(ReviewServiceImpl.class, "createReview", Long.class, Long.class,
                com.photoconnect.dto.ReviewRequest.class);
        assertTransactional(AdminReviewServiceImpl.class, "hideReview", Long.class);
        assertTransactional(AdminReviewServiceImpl.class, "unhideReview", Long.class);
        assertTransactional(ScheduleServiceImpl.class, "addUnavailableDate", Long.class,
                java.time.LocalDate.class, String.class);
        assertTransactional(PortfolioServiceImpl.class, "addPortfolioImage", Long.class,
                org.springframework.web.multipart.MultipartFile.class, String.class);
        assertTransactional(PhotographerProfileServiceImpl.class, "updateProfile", Long.class,
                com.photoconnect.dto.PhotographerProfileEditRequest.class);
        assertTransactional(SavedPhotographerServiceImpl.class, "savePhotographer", Long.class, Long.class);
        assertTransactional(SavedPhotographerServiceImpl.class, "removeSavedPhotographer", Long.class, Long.class);
    }

    private static void assertTransactional(Class<?> serviceType, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = serviceType.getMethod(methodName, parameterTypes);
        Transactional transaction = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (transaction == null) {
            transaction = AnnotatedElementUtils.findMergedAnnotation(serviceType, Transactional.class);
        }
        assertThat(transaction)
                .as("%s#%s must be transactional", serviceType.getSimpleName(), methodName)
                .isNotNull();
        assertThat(transaction.readOnly()).isFalse();
    }
}
