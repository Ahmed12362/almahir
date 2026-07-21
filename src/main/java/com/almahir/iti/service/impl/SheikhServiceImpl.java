package com.almahir.iti.service.impl;

import com.almahir.iti.dto.response.SheikhSearchResponse;
import com.almahir.iti.model.Sheikh;
import com.almahir.iti.model.User;
import com.almahir.iti.repository.SheikhRepository;
import com.almahir.iti.service.SheikhService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SheikhServiceImpl implements SheikhService {

    private final SheikhRepository sheikhRepository;

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
                user.getEmail(),
                user.getProfilePictureUrl(),
                sheikh.getSheikhStatus(),
                sheikh.getRate(),
                startIndex,
                endIndex
        );
    }
}
