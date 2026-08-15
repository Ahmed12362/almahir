package com.almahir.iti.dto.response;

import java.util.Map;
import java.util.UUID;

public record AdminStatsResponse(
        long studentsCount,
        long sheikhsCount,
        long blockedUsersCount,
        long totalCircles,
        long runningCircles,
        Map<UUID, Long> circlesPerSheikhId,
        Map<String, Long> circlesPerSheikhEmail
) {}
