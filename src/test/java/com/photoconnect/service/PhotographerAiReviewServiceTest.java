package com.photoconnect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotographerAiReviewServiceTest {
    @Mock private ManagerOperationsService operations;

    @Test
    void pendingApplicationGetsChecklistWithoutAnApiKey() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        profile.setCity("Hà Nội");
        profile.setSpecialties("WEDDING");
        profile.setBio("Chụp ảnh chuyên nghiệp, nhận chụp sự kiện và chân dung tại studio và ngoài trời.");
        profile.setExperienceYears(3);
        profile.setEquipmentSummary("Máy ảnh và ống kính");
        when(operations.photographerApplication(7L, 21L)).thenReturn(profile);

        var service = new PhotographerAiReviewService(operations, new ObjectMapper(), "", "gpt-4.1-mini");
        var advice = service.assess(7L, 21L);

        assertEquals(5, advice.getCompleted());
        assertEquals(5, advice.getTotal());
        assertFalse(advice.isAiGenerated());
        assertNull(advice.getAiSummary());
        assertTrue(advice.getAiStatus().contains("chưa cấu hình"));
        assertTrue(advice.getMissing().stream().anyMatch(s -> s.contains("xác minh")));
        verify(operations).photographerApplication(7L, 21L);
    }

    @Test
    void incompletePendingApplicationIsNotAutomaticallyRejected() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setVerificationStatus(PhotographerVerificationStatus.PENDING);
        when(operations.photographerApplication(7L, 21L)).thenReturn(profile);

        var advice = new PhotographerAiReviewService(operations, new ObjectMapper(), "", "gpt-4.1-mini")
                .assess(7L, 21L);

        assertEquals(0, advice.getCompleted());
        assertEquals(6, advice.getMissing().size());
        assertEquals(PhotographerVerificationStatus.PENDING, profile.getVerificationStatus());
    }

    @Test
    void alreadyApprovedApplicationCannotBeRescoredThroughThisAction() {
        PhotographerProfile profile = new PhotographerProfile();
        profile.setVerificationStatus(PhotographerVerificationStatus.APPROVED);
        when(operations.photographerApplication(7L, 21L)).thenReturn(profile);

        var service = new PhotographerAiReviewService(operations, new ObjectMapper(), "", "gpt-4.1-mini");
        assertThrows(IllegalStateException.class, () -> service.assess(7L, 21L));
    }
}
