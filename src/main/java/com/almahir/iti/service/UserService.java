package com.almahir.iti.service;

import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User getUserByEmail(String email);
    UserResponse updateProfilePicture(MultipartFile file);
}