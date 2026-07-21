package com.almahir.iti.service;

import com.almahir.iti.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse updateProfilePicture(MultipartFile file);
}