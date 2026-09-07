package com.photoconnect.service;

import com.photoconnect.entity.PhotographerUnavailableDate;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    void addUnavailableDate(Long profileId, LocalDate date, String reason);

    void removeUnavailableDate(Long profileId, Long dateId);

    List<PhotographerUnavailableDate> getUnavailableDates(Long profileId);

    boolean isDateAvailable(Long profileId, LocalDate date);
}
