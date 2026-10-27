package com.photoconnect.service;

import com.photoconnect.entity.AuditOutcome;
import com.photoconnect.entity.OperationalAuditLog;
import com.photoconnect.entity.User;
import com.photoconnect.repository.OperationalAuditLogRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OperationalAuditServiceImpl implements OperationalAuditService {
    private final OperationalAuditLogRepository auditRepository;
    private final UserRepository userRepository;

    public OperationalAuditServiceImpl(OperationalAuditLogRepository auditRepository, UserRepository userRepository) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorId, String action, String targetType, Object targetId,
                       AuditOutcome outcome, String safeSummary) {
        if (actorId == null) return;
        User actor = userRepository.findById(actorId).orElse(null);
        if (actor == null) return;
        String summary = normalize(safeSummary, 1000);
        auditRepository.save(new OperationalAuditLog(actor, normalize(action, 80),
                normalize(targetType, 80), targetId != null ? normalize(targetId.toString(), 100) : null,
                outcome != null ? outcome : AuditOutcome.FAILED, summary));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalAuditLog> recent(int limit) {
        return auditRepository.findRecent(PageRequest.of(0, clamp(limit)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OperationalAuditLog> recentForActor(Long actorId, int limit) {
        return actorId == null ? List.of() : auditRepository.findByActorId(actorId, PageRequest.of(0, clamp(limit)));
    }

    private int clamp(int limit) { return Math.max(1, Math.min(limit, 200)); }

    private String normalize(String value, int max) {
        if (value == null) return null;
        String clean = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return clean.length() <= max ? clean : clean.substring(0, max);
    }
}
