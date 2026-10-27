package com.photoconnect.service;

import com.photoconnect.entity.AuditOutcome;
import com.photoconnect.entity.OperationalAuditLog;

import java.util.List;

public interface OperationalAuditService {
    void record(Long actorId, String action, String targetType, Object targetId,
                AuditOutcome outcome, String safeSummary);

    List<OperationalAuditLog> recent(int limit);

    List<OperationalAuditLog> recentForActor(Long actorId, int limit);
}
