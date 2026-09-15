package com.photoconnect.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionBoundaryTest {

    @Test
    void multiWriteBusinessOperationsHaveTransactionBoundaries() throws Exception {
        assertTransactional(BookingServiceImpl.class, "createBooking", Long.class, Long.class,
                com.photoconnect.dto.BookingRequest.class);
        assertTransactional(DepositServiceImpl.class, "getOrCreateDepositForBooking", Long.class, Long.class);
        assertTransactional(DepositServiceImpl.class, "simulateSuccessfulPayment", Long.class, Long.class);
        assertTransactional(ReviewServiceImpl.class, "createReview", Long.class, Long.class,
                com.photoconnect.dto.ReviewRequest.class);
        assertTransactional(AdminReviewServiceImpl.class, "hideReview", Long.class);
        assertTransactional(AdminReviewServiceImpl.class, "unhideReview", Long.class);
        assertTransactional(ScheduleServiceImpl.class, "addUnavailableDate", Long.class,
                java.time.LocalDate.class, String.class);
        assertTransactional(PortfolioServiceImpl.class, "addPortfolioImage", Long.class,
                org.springframework.web.multipart.MultipartFile.class, String.class);
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
