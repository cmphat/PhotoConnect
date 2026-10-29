package com.photoconnect.service;

import com.photoconnect.dto.PhotographerFeeSummaryDto;
import com.photoconnect.dto.PlatformFeeDto;
import com.photoconnect.entity.Booking;
import com.photoconnect.entity.PlatformFee;
import com.photoconnect.entity.PlatformFeeStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PlatformFeeService {

    BigDecimal getCurrentFeeRate();

    void updateFeeRate(BigDecimal newRate, Long adminUserId);

    PlatformFee recordFeeForCompletedBooking(Booking booking);

    void cancelFeeForBooking(Booking booking, String reason);

    PhotographerFeeSummaryDto getPhotographerSummary(Long photographerUserId);

    List<PlatformFeeDto> listAllFees(PlatformFeeStatus statusFilter);

    PlatformFeeDto processPhotographerFeePayment(Long photographerUserId, Long feeId, BigDecimal amount, String paymentMethod);

    Map<String, Object> getAdminFeeStats();
}
