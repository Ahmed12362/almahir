package com.almahir.iti.service.impl;

import com.almahir.iti.client.TafsirClient;
import com.almahir.iti.dto.response.TafsirRawResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.model.enums.TafsirEdition;
import com.almahir.iti.service.TafsirService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class TafsirServiceImpl implements TafsirService {

    private final TafsirClient tafsirClient;

    @Cacheable(value = "tafsir", key = "#lang + '-' + #tafsirKey + '-' + #surah + '-' + #ayah")
    public TafsirResponse getTafsir(
            @Min(1) @Max(114) int surah,
            @Min(1) int ayah,
            String lang,
            String tafsirKey) {

        TafsirEdition edition = TafsirEdition.resolve(lang, tafsirKey);
        TafsirRawResponse raw = tafsirClient.fetchRawTafsir(edition, surah, ayah);

        if (raw == null || raw.text() == null || raw.text().isBlank()) {
            throw new ResourceNotFound(
                    "Tafsir not found for surah=%d, ayah=%d".formatted(surah, ayah));
        }

        return new TafsirResponse(surah, ayah, raw.text());

    }
}