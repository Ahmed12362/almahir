package com.almahir.iti.service;

import com.almahir.iti.dto.response.AdminAuthResponse;
import com.almahir.iti.dto.response.AdminStatsResponse;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.enums.SheikhStatus;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    AdminAuthResponse login(String email, String password);
    void logout(String refreshToken);
    SheikhResponse approveSheikh(UUID sheikhId);
    SheikhResponse declineSheikh(UUID sheikhId);
    UserResponse blockUser(UUID userId);
    UserResponse unblockUser(UUID userId);
    AdminStatsResponse getStats();
}
