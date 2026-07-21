package com.almahir.iti.service;

import com.almahir.iti.dto.response.SheikhSearchResponse;

import java.util.List;

public interface SheikhService {
    List<SheikhSearchResponse> search(String name);
}
