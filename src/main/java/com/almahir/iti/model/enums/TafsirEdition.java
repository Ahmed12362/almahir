package com.almahir.iti.model.enums;

import com.almahir.iti.exception.ResourceNotFound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TafsirEdition {

    AR_IBN_KATHIR("ar", "ibn-kathir", "ar-tafsir-ibn-kathir"),
    EN_IBN_KATHIR("en", "ibn-kathir", "en-tafisr-ibn-kathir");

    private final String lang;
    private final String key;
    private final String slug;

    public static TafsirEdition resolve(String lang, String key) {
        for (TafsirEdition edition : values()) {
            if (edition.lang.equalsIgnoreCase(lang) && edition.key.equalsIgnoreCase(key)) {
                return edition;
            }
        }
        throw new ResourceNotFound(
                "No tafsir edition found for lang=%s, tafsir=%s".formatted(lang, key));
    }
}