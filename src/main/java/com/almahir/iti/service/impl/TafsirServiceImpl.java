package com.almahir.iti.service.impl;

import com.almahir.iti.client.TafsirClient;
import com.almahir.iti.dto.response.TafsirBuildStatusResponse;
import com.almahir.iti.dto.response.TafsirCatalogResponse;
import com.almahir.iti.dto.response.TafsirRawResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import com.almahir.iti.exception.ResourceNotFoundException;
import com.almahir.iti.model.TafsirMetadata;
import com.almahir.iti.model.enums.TafsirBuildStatus;
import com.almahir.iti.model.enums.TafsirEdition;
import com.almahir.iti.repository.TafsirMetadataRepository;
import com.almahir.iti.service.TafsirService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class TafsirServiceImpl implements TafsirService {

    private final TafsirClient tafsirClient;
    private final TafsirMetadataRepository metadataRepository;

    @Cacheable(value = "tafsir", key = "#lang + '-' + #tafsirKey + '-' + #surah + '-' + #ayah")
    public TafsirResponse getTafsir(
            @Min(1) @Max(114) int surah,
            @Min(1) int ayah,
            String lang,
            String tafsirKey) {
        log.info("Fetching tafsir from external API");
        TafsirEdition edition = TafsirEdition.resolve(lang, tafsirKey);
        TafsirRawResponse raw = tafsirClient.fetchRawTafsir(edition, surah, ayah);

        if (raw == null || raw.text() == null || raw.text().isBlank()) {
            throw new ResourceNotFoundException(
                    "Tafsir not found for surah=%d, ayah=%d".formatted(surah, ayah));
        }

        return new TafsirResponse(surah, ayah, raw.text());

    }

    @Override
    public List<TafsirCatalogResponse> getAvailableTafsirs() {
        return metadataRepository.findByStatus(TafsirBuildStatus.READY)
                .stream()
                .map(m -> new TafsirCatalogResponse(
                        m.getTafsirKey(),
                        m.getDisplayName(),
                        m.getLanguage(),
                        m.getLanguageName(),
                        m.getFileUrl(),
                        m.getFileSizeBytes()
                ))
                .toList();
    }
    @Override
    public TafsirBuildStatusResponse getBuildStatus(String tafsirKey, String language) {

        TafsirMetadata metadata = metadataRepository
                .findByTafsirKeyAndLanguage(tafsirKey, language)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tafsir metadata not found"));

        return new TafsirBuildStatusResponse(
                metadata.getTafsirKey(),
                metadata.getLanguage(),
                metadata.getStatus(),
                metadata.getFileUrl(),
                metadata.getFileSizeBytes()
        );
    }
}