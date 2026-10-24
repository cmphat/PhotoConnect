package com.photoconnect.service;

import com.photoconnect.entity.PhotographerUnavailableDate;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    void addUnavailableDate(Long profileId, LocalDate date, String reason);

    void removeUnavailableDate(Long profileId, Long dateId);

    List<PhotographerUnavailableDate> getUnavailableDates(Long profileId);

    boolean isDateAvailable(Long profileId, LocalDate date);

    boolean isSlotAvailable(Long profileId, LocalDate date, java.time.LocalTime time, int durationMinutes);

    boolean isSlotAvailable(Long profileId, LocalDate date, java.time.LocalTime time, int durationMinutes, Long excludeBookingId);

    void validateSlotAvailability(Long profileId, LocalDate date, java.time.LocalTime time, int durationMinutes, Long excludeBookingId);

    List<com.photoconnect.dto.TimeSlotDto> getAvailableTimeSlots(Long profileId, LocalDate date, int durationMinutes);
}
