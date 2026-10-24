package com.photoconnect.repository;

import com.photoconnect.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByUserIdOrderByStartAtAsc(Long userId);

    List<CalendarEvent> findByUserIdAndVisibilityOrderByStartAtAsc(Long userId, String visibility);

    List<CalendarEvent> findByUserIdAndStartAtBetweenOrUserIdAndEndAtBetweenOrderByStartAtAsc(
            Long userId1, LocalDateTime start1, LocalDateTime end1,
            Long userId2, LocalDateTime start2, LocalDateTime end2
    );

    @org.springframework.data.jpa.repository.Query("""
            SELECT e FROM CalendarEvent e
            WHERE e.userId = :userId
              AND e.eventType IN ('BLOCKED', 'PERSONAL', 'SESSION')
              AND e.startAt < :endAt
              AND e.endAt > :startAt
            """)
    List<CalendarEvent> findConflictingEvents(@org.springframework.data.repository.query.Param("userId") Long userId,
                                             @org.springframework.data.repository.query.Param("startAt") LocalDateTime startAt,
                                             @org.springframework.data.repository.query.Param("endAt") LocalDateTime endAt);
}
