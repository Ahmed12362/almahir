package com.almahir.iti.client;

import com.almahir.iti.dto.response.TafsirRawResponse;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.model.enums.TafsirEdition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TafsirClient {

    private final RestClient tafsirRestClient;

    public TafsirRawResponse fetchRawTafsir(TafsirEdition edition, int surah, int ayah) {
        try {
            return tafsirRestClient.get()
                    .uri("/{slug}/{surah}/{ayah}.json", edition.getSlug(), surah, ayah)
                    .retrieve()
                    .body(TafsirRawResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFound(
                    "Tafsir not found for surah=%d, ayah=%d".formatted(surah, ayah));
        }
    }
}