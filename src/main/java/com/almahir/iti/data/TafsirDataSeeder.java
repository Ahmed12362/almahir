package com.almahir.iti.data;

import com.almahir.iti.model.TafsirMetadata;
import com.almahir.iti.model.enums.TafsirBuildStatus;
import com.almahir.iti.repository.TafsirMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TafsirDataSeeder implements CommandLineRunner {

    private final TafsirMetadataRepository metadataRepository;

    @Override
    public void run(String... args) {
        seedTafsir("ibn-kathir", "تفسير ابن كثير", "ar", "العربية");
        seedTafsir("ibn-kathir", "Tafsir Ibn Kathir", "en", "English");
        seedTafsir("al-qurtubi", "تفسير القرطبي", "ar", "العربية");
        seedTafsir("al-saadi", "تفسير السعدي", "ar", "العربية");
    }

    private void seedTafsir(String key, String displayName, String lang, String langName) {
        if (metadataRepository.findByTafsirKeyAndLanguage(key, lang).isEmpty()) {
            TafsirMetadata metadata = TafsirMetadata.builder()
                    .tafsirKey(key)
                    .displayName(displayName)
                    .language(lang)
                    .languageName(langName)
                    .status(TafsirBuildStatus.PENDING)
                    .build();

            metadataRepository.save(metadata);
            log.info("Seeded Tafsir metadata: {} ({})", key, lang);
        }
    }
}