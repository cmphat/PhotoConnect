package com.photoconnect.service;

import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerUnavailableDate;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PhotographerUnavailableDateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private PhotographerUnavailableDateRepository scheduleRepository;

    @Mock
    private PhotographerProfileRepository profileRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private PhotographerProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PhotographerProfile();
        profile.setId(1L);
    }

    @Test
    void addUnavailableDate_ValidDate_Success() {
        LocalDate date = LocalDate.now().plusDays(1);
        
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(false);
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        scheduleService.addUnavailableDate(1L, date, "Vacation");

        verify(scheduleRepository, times(1)).save(any(PhotographerUnavailableDate.class));
    }

    @Test
    void addUnavailableDate_PastDate_ThrowsException() {
        LocalDate date = LocalDate.now().minusDays(1);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.addUnavailableDate(1L, date, "Vacation");
        });
        
        assertEquals("Cannot block dates in the past", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void addUnavailableDate_DuplicateDate_ThrowsException() {
        LocalDate date = LocalDate.now().plusDays(1);
        
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.addUnavailableDate(1L, date, "Vacation");
        });
        
        assertEquals("Date is already marked as unavailable", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void isDateAvailable_Available_ReturnsTrue() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(false);

        boolean available = scheduleService.isDateAvailable(1L, date);

        assertTrue(available);
    }

    @Test
    void isDateAvailable_Unavailable_ReturnsFalse() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(scheduleRepository.existsByPhotographerProfileIdAndDate(1L, date)).thenReturn(true);

        boolean available = scheduleService.isDateAvailable(1L, date);

        assertFalse(available);
    }
}
