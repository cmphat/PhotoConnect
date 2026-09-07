package com.photoconnect.service;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final PhotographerUnavailableDateRepository scheduleRepository;
    private final PhotographerProfileRepository profileRepository;

    public ScheduleServiceImpl(PhotographerUnavailableDateRepository scheduleRepository, PhotographerProfileRepository profileRepository) {
        this.scheduleRepository = scheduleRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public void addUnavailableDate(Long profileId, LocalDate date, String reason) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot block dates in the past");
        }

        if (scheduleRepository.existsByPhotographerProfileIdAndDate(profileId, date)) {
            throw new IllegalArgumentException("Date is already marked as unavailable");
        }

        PhotographerProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        PhotographerUnavailableDate unavailableDate = new PhotographerUnavailableDate(profile, date, reason);
        scheduleRepository.save(unavailableDate);
    }

    @Override
    @Transactional
    public void removeUnavailableDate(Long profileId, Long dateId) {
        scheduleRepository.deleteByIdAndPhotographerProfileId(dateId, profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerUnavailableDate> getUnavailableDates(Long profileId) {
        return scheduleRepository.findByPhotographerProfileIdOrderByDateAsc(profileId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDateAvailable(Long profileId, LocalDate date) {
        return !scheduleRepository.existsByPhotographerProfileIdAndDate(profileId, date);
    }
}
