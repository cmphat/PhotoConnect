package com.photoconnect.service;

import com.photoconnect.entity.*;
import com.photoconnect.repository.OperationalAuditLogRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalAuditServiceTest {
    @Mock private OperationalAuditLogRepository auditRepository;
    @Mock private UserRepository userRepository;

    @Test
    void recordsActorRoleOutcomeAndSanitizedSummary() {
        User manager = new User();
        manager.setId(7L);
        manager.setRole(UserRole.MANAGER);
        when(userRepository.findById(7L)).thenReturn(Optional.of(manager));
        OperationalAuditService service = new OperationalAuditServiceImpl(auditRepository, userRepository);

        service.record(7L, "REPORT_RESOLVE", "UserReport", 44L,
                AuditOutcome.SUCCESS, "reviewed\r\nwithout secret");

        ArgumentCaptor<OperationalAuditLog> captor = ArgumentCaptor.forClass(OperationalAuditLog.class);
        verify(auditRepository).save(captor.capture());
        OperationalAuditLog log = captor.getValue();
        assertThat(log.getActorRole()).isEqualTo(UserRole.MANAGER);
        assertThat(log.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(log.getTargetId()).isEqualTo("44");
        assertThat(log.getSummary()).isEqualTo("reviewed without secret");
    }

    @Test
    void missingActorFailsClosedWithoutWritingOrLeakingData() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        OperationalAuditService service = new OperationalAuditServiceImpl(auditRepository, userRepository);

        service.record(999L, "ROLE_GRANT", "User", 1L, AuditOutcome.FAILED, "raw JWT should not be persisted");

        verifyNoInteractions(auditRepository);
    }
}
