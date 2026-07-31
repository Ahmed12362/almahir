package com.almahir.iti.scheduler;

import com.almahir.iti.service.InstantMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingRequestExpirationScheduler {

    private final InstantMeetingService instantMeetingService;

    @Scheduled(fixedDelay = 5000)
    public void expirePendingRequests() {
        instantMeetingService.expirePendingRequests();
    }
}