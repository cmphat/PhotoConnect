package com.photoconnect.controller;

import com.photoconnect.dto.CalendarEventDto;
import com.photoconnect.dto.CalendarEventRequest;
import com.photoconnect.dto.UserProfileDto;
import com.photoconnect.dto.UserProfileUpdateRequest;
import com.photoconnect.service.CalendarService;
import com.photoconnect.service.UserProfileService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CalendarApiController {

    private final CalendarService calendarService;
    private final UserProfileService userProfileService;
    private final com.photoconnect.service.ScheduleService scheduleService;

    public CalendarApiController(CalendarService calendarService,
                                 UserProfileService userProfileService,
                                 @org.springframework.beans.factory.annotation.Autowired(required = false) com.photoconnect.service.ScheduleService scheduleService) {
        this.calendarService = calendarService;
        this.userProfileService = userProfileService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/me/calendar")
    public ResponseEntity<List<CalendarEventDto>> getMyCalendar(HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<CalendarEventDto> events = calendarService.getCalendarEvents(currentUserId, currentUserId);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/users/{userId}/calendar")
    public ResponseEntity<List<CalendarEventDto>> getUserCalendar(@PathVariable("userId") Long targetUserId,
                                                                  HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        try {
            List<CalendarEventDto> events = calendarService.getCalendarEvents(targetUserId, currentUserId);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profile/{username}/calendar")
    public ResponseEntity<List<CalendarEventDto>> getProfileCalendarByUsername(@PathVariable("username") String username,
                                                                               HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        try {
            UserProfileDto profile = userProfileService.getProfileByUsername(username, currentUserId);
            List<CalendarEventDto> events = calendarService.getCalendarEvents(profile.getUserId(), currentUserId);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/me/calendar/events")
    public ResponseEntity<?> createEvent(@Valid @RequestBody CalendarEventRequest request,
                                         HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            CalendarEventDto created = calendarService.createEvent(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/me/calendar/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable("id") Long eventId,
                                         @Valid @RequestBody CalendarEventRequest request,
                                         HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            CalendarEventDto updated = calendarService.updateEvent(currentUserId, eventId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/me/calendar/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable("id") Long eventId,
                                         HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            calendarService.deleteEvent(currentUserId, eventId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Event deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDto> getMyProfile(HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userProfileService.getProfile(currentUserId, currentUserId));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UserProfileUpdateRequest request,
                                             HttpSession session) {
        Long currentUserId = SessionSecurityUtils.userId(session);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            UserProfileDto updated = userProfileService.updateProfile(currentUserId, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/photographers/{profileId}/availability")
    public ResponseEntity<?> checkAvailability(
            @PathVariable("profileId") Long profileId,
            @RequestParam("date") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @RequestParam(value = "durationMinutes", defaultValue = "120") int durationMinutes) {
        if (scheduleService == null) {
            return ResponseEntity.ok(Map.of("available", true, "slots", List.of()));
        }
        boolean dateAvailable = scheduleService.isDateAvailable(profileId, date);
        List<com.photoconnect.dto.TimeSlotDto> slots = scheduleService.getAvailableTimeSlots(profileId, date, durationMinutes);
        return ResponseEntity.ok(Map.of(
                "profileId", profileId,
                "date", date.toString(),
                "wholeDayAvailable", dateAvailable,
                "slots", slots
        ));
    }
}
