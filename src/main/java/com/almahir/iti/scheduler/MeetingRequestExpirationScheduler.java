package com.almahir.iti.scheduler;

import com.almahir.iti.service.InstantMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingRequestExpirationScheduler {

    private final InstantMeetingService instantMeetingService;

    @Scheduled(fixedDelayString = "${meeting.request.expiration.check-delay}")
    public void expirePendingRequests() {
        instantMeetingService.expirePendingRequests();
    }
}