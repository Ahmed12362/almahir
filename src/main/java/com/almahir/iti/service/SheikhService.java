package com.almahir.iti.service;

import com.almahir.iti.dto.request.UpdateSheikhRequest;
import com.almahir.iti.dto.response.SheikhResponse;
import com.almahir.iti.dto.response.SheikhSearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface SheikhService {
    List<SheikhSearchResponse> search(String name);
    List<SheikhResponse> getAllSheikhs();
    SheikhResponse getSheikhByEmail(String email);
    SheikhResponse getSheikhByUsername(String username);
    SheikhResponse getSheikhById(UUID id);
    SheikhResponse updateSheikh(UUID id, UpdateSheikhRequest request, MultipartFile file);
}

