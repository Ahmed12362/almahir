package com.almahir.iti.data;

import com.almahir.iti.model.TafsirMetadata;
import com.almahir.iti.model.enums.TafsirBuildStatus;
import com.almahir.iti.model.enums.TafsirEdition;
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
        for (TafsirEdition edition : TafsirEdition.values()) {
            seedTafsir(
                    edition.getKey(),
                    edition.getDisplayName(),
                    edition.getLang(),
                    edition.getLangName()
            );
        }
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