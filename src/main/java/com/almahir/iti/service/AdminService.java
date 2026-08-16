package com.almahir.iti.service;

import com.almahir.iti.dto.request.CreateSubscriptionPackageRequest;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.SubscriptionPackageResponse;
import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.enums.SheikhStatus;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    SheikhResponse approveSheikh(UUID sheikhId);
    SheikhResponse declineSheikh(UUID sheikhId);
    UserResponse blockUser(UUID userId);
    UserResponse unblockUser(UUID userId);
    SubscriptionPackageResponse createSubscriptionPackage(CreateSubscriptionPackageRequest request);
}
