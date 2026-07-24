package com.almahir.iti.model.enums;

import com.almahir.iti.exception.ResourceNotFound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TafsirEdition {

    IBN_KATHIR_AR("ar", "العربية", "ibn-kathir", "تفسير ابن كثير", "ar-tafsir-ibn-kathir"),
    SAADI_AR("ar", "العربية", "al-saadi", "تفسير السعدي", "ar-tafsir-as-saadi"),
    QURTUBI_AR("ar", "العربية", "al-qurtubi", "تفسير القرطبي", "ar-tafseer-al-qurtubi"),
    TABARI_AR("ar", "العربية", "al-tabari", "تفسير الطبري", "ar-tafsir-al-tabari"),
    BAGHAWI_AR("ar", "العربية", "al-baghawi", "تفسير البغوي", "ar-tafsir-al-baghawi"),
    MUYASSAR_AR("ar", "العربية", "muyassar", "التفسير الميسر", "ar-tafsir-muyassar"),

    // English Editions
    IBN_KATHIR_EN("en", "English", "ibn-kathir", "Tafsir Ibn Kathir", "en-tafisr-ibn-kathir"),
    JALALAYN_EN("en", "English", "jalalayn", "Tafsir Al-Jalalayn", "en-al-jalalayn");

    private final String lang;
    private final String langName;
    private final String key;
    private final String displayName;
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