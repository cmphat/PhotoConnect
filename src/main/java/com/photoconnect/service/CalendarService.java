package com.photoconnect.service;

import com.photoconnect.dto.CalendarEventDto;
import com.photoconnect.dto.CalendarEventRequest;

import java.util.List;

public interface CalendarService {

    List<CalendarEventDto> getCalendarEvents(Long targetUserId, Long currentUserId);

    CalendarEventDto createEvent(Long currentUserId, CalendarEventRequest request);

    CalendarEventDto updateEvent(Long currentUserId, Long eventId, CalendarEventRequest request);

    void deleteEvent(Long currentUserId, Long eventId);
}
