package com.almahir.iti.service;

import com.almahir.iti.dto.response.TafsirCatalogResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public interface TafsirService {
    TafsirResponse getTafsir(
            @Min(1) @Max(114) int surah,
            @Min(1) int ayah,
            String lang,
            String tafsirKey);

    List<TafsirCatalogResponse> getAvailableTafsirs();
}
