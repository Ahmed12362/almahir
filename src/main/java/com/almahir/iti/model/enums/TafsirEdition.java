package com.almahir.iti.model.enums;

import com.almahir.iti.exception.ResourceNotFound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TafsirEdition {

    IBN_KATHIR_AR("ar", "ibn-kathir", "ar-tafsir-ibn-kathir"),
    SAADI_AR("ar", "al-saadi", "ar-tafsir-as-saadi"),
    QURTUBI_AR("ar", "al-qurtubi", "ar-tafseer-al-qurtubi"),
    TABARI_AR("ar", "al-tabari", "ar-tafsir-al-tabari"),
    BAGHAWI_AR("ar", "al-baghawi", "ar-tafsir-al-baghawi"),
    MUYASSAR_AR("ar", "muyassar", "ar-tafsir-muyassar"),

    // English Editions
    IBN_KATHIR_EN("en", "ibn-kathir", "en-tafisr-ibn-kathir"),
    JALALAYN_EN("en", "jalalayn", "en-al-jalalayn");

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