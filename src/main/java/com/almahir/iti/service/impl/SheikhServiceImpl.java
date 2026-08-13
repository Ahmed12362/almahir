package com.almahir.iti.service.impl;

import com.almahir.iti.dto.request.UpdateSheikhRequest;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.SheikhSearchResponse;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.mapper.SheikhMapper;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.repository.UserRepository;
import com.almahir.iti.service.CloudinaryService;
import com.almahir.iti.service.SheikhService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SheikhServiceImpl implements SheikhService {

    private final SheikhRepository sheikhRepository;
    private final UserRepository userRepository;
    private final SheikhMapper sheikhMapper;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<SheikhSearchResponse> search(String name) {
        if (!StringUtils.hasText(name)) {
            return sheikhRepository.findAllWithUser().stream()
                    .map(sheikh -> toResponse(sheikh, null, null))
                    .toList();
        }

        String searchTerm = name.trim();
        String normalizedSearchTerm = searchTerm.toLowerCase(Locale.ROOT);

        return sheikhRepository.findByFullNameContainingIgnoreCase(searchTerm).stream()
                .map(sheikh -> toMatchedResponse(sheikh, normalizedSearchTerm))
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(SheikhSearchResponse::startIndex)
                        .thenComparing(SheikhSearchResponse::endIndex, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    public List<SheikhResponse> getAllSheikhs() {
        return sheikhRepository.findAllWithUser().stream()
                .map(sheikhMapper::toSheikhResponse)
                .toList();
    }

    @Override
    public SheikhResponse getSheikhByEmail(String email) {
        Sheikh sheikh = sheikhRepository.findByUserEmailFetchUser(email)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with email: " + email));
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    public SheikhResponse getSheikhByUsername(String username) {
        Sheikh sheikh = sheikhRepository.findByUserUsernameFetchUser(username)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with username: " + username));
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    public SheikhResponse getSheikhById(UUID id) {
        Sheikh sheikh = sheikhRepository.findByIdFetchUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with id: " + id));
        return sheikhMapper.toSheikhResponse(sheikh);
    }

    @Override
    @Transactional
    public SheikhResponse updateSheikh(UUID id, UpdateSheikhRequest request, MultipartFile file) {
        Sheikh sheikh = sheikhRepository.findByIdFetchUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sheikh not found with id: " + id));

        User user = sheikh.getUser();

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(file, "almahir/profile_pictures");
            user.setProfilePictureUrl(imageUrl);
        } else if (request != null && StringUtils.hasText(request.profilePictureUrl())) {
            user.setProfilePictureUrl(request.profilePictureUrl().trim());
        }

        if (request != null) {
            if (StringUtils.hasText(request.password())) {
                user.setPassword(passwordEncoder.encode(request.password()));
            }

            if (StringUtils.hasText(request.firstName()) || StringUtils.hasText(request.lastName())) {
                if (StringUtils.hasText(request.firstName())) {
                    user.setFirstName(request.firstName().trim());
                }
                if (StringUtils.hasText(request.lastName())) {
                    user.setLastName(request.lastName().trim());
                }
            } else if (StringUtils.hasText(request.name())) {
                String full = request.name().trim();
                int spaceIdx = full.indexOf(' ');
                if (spaceIdx > 0) {
                    user.setFirstName(full.substring(0, spaceIdx).trim());
                    user.setLastName(full.substring(spaceIdx + 1).trim());
                } else {
                    user.setFirstName(full);
                    user.setLastName(full);
                }
            }

            if (request.gender() != null) {
                user.setGender(request.gender());
            }

            if (StringUtils.hasText(request.phoneNumber())) {
                user.setPhoneNumber(request.phoneNumber().trim());
            }

            if (request.sheikhStatus() != null) {
                sheikh.setSheikhStatus(request.sheikhStatus());
            }
        }

        userRepository.save(user);
        Sheikh updatedSheikh = sheikhRepository.save(sheikh);
        return sheikhMapper.toSheikhResponse(updatedSheikh);
    }

    private SheikhSearchResponse toMatchedResponse(Sheikh sheikh, String normalizedSearchTerm) {
        User user = sheikh.getUser();
        String fullName = user.getFirstName() + " " + user.getLastName();
        int startIndex = fullName.toLowerCase(Locale.ROOT).indexOf(normalizedSearchTerm);

        if (startIndex < 0) {
            return null;
        }

        return toResponse(sheikh, startIndex, startIndex + normalizedSearchTerm.length());
    }

    private SheikhSearchResponse toResponse(Sheikh sheikh, Integer startIndex, Integer endIndex) {
        User user = sheikh.getUser();

        return new SheikhSearchResponse(
                sheikh.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getGender(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                sheikh.getSheikhStatus(),
                sheikh.getRate(),
                startIndex,
                endIndex
        );
    }
}
