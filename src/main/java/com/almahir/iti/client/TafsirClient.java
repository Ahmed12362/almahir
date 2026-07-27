package com.almahir.iti.client;

import com.almahir.iti.dto.response.TafsirRawResponse;
import com.almahir.iti.exception.ResourceNotFoundException;
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
        return getAyahTafsir(edition.getSlug(), surah, ayah);
    }

    public TafsirRawResponse getAyahTafsir(String slug, int surah, int ayah) {
        try {
            return tafsirRestClient.get()
                    .uri("/{slug}/{surah}/{ayah}.json", slug, surah, ayah)
                    .retrieve()
                    .body(TafsirRawResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException(
                    String.format("Tafsir not found for surah %d, ayah %d with edition %s", surah, ayah, slug)
            );
        }
    }
}