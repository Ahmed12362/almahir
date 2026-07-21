package com.almahir.iti.service.impl;

import com.almahir.iti.dto.response.UserResponse;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.mapper.UserMapper;
import com.almahir.iti.model.AuthUser;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.CloudinaryService;
import com.almahir.iti.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;

    @Override
    public UserResponse updateProfilePicture(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser currentUser = (AuthUser) authentication.getPrincipal();

        UUID userId = currentUser.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFound("User not found with id: " + userId));

        String imageUrl = cloudinaryService.uploadFile(file, "almahir/profile_pictures");
        user.setProfilePictureUrl(imageUrl);

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }
}