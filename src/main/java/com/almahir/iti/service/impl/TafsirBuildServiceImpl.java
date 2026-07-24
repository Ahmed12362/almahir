package com.almahir.iti.service.impl;

import com.almahir.iti.client.TafsirClient;
import com.almahir.iti.dto.response.TafsirRawResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import com.almahir.iti.exception.ResourceNotFound;
import com.almahir.iti.model.TafsirMetadata;
import com.almahir.iti.model.enums.TafsirBuildStatus;
import com.almahir.iti.model.enums.TafsirEdition;
import com.almahir.iti.repository.TafsirMetadataRepository;
import com.almahir.iti.service.CloudinaryService;
import com.almahir.iti.service.TafsirBuildService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TafsirBuildServiceImpl implements TafsirBuildService {

    private final TafsirClient tafsirClient;
    private final TafsirMetadataRepository metadataRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    private static final int[] AYAH_COUNTS = {
            7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
            123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
            112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
            34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
            54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
            60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
            14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
            28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
            29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
            15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
            11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
            5, 4, 5, 6
    };

    @Override
    @Async
    public CompletableFuture<Void> buildFullTafsirAsync(String tafsirKey, String language) {
        log.info("Starting background build process for Tafsir: {} - Lang: {}", tafsirKey, language);

        TafsirMetadata metadata = metadataRepository.findByTafsirKeyAndLanguage(tafsirKey, language)
                .orElseThrow(() -> new ResourceNotFound("Metadata entry not found for " + tafsirKey + " (" + language + ")"));

        metadata.setStatus(TafsirBuildStatus.IN_PROGRESS);
        metadataRepository.save(metadata);

        TafsirEdition edition = TafsirEdition.resolve(language, tafsirKey);

        ExecutorService executor = Executors.newFixedThreadPool(15);
        List<CompletableFuture<TafsirResponse>> futures = new ArrayList<>();

        try {
            for (int surah = 1; surah <= 114; surah++) {
                int totalAyahs = AYAH_COUNTS[surah - 1];
                for (int ayah = 1; ayah <= totalAyahs; ayah++) {
                    int currentSurah = surah;
                    int currentAyah = ayah;

                    CompletableFuture<TafsirResponse> future =
                            CompletableFuture.supplyAsync(
                                    () -> fetchAyahWithRetry(edition.getSlug(), currentSurah, currentAyah, 3),
                                    executor);

                    futures.add(future);
                }
            }

            log.info("Dispatched {} tasks to queue for Tafsir {}", futures.size(), tafsirKey);
            log.info("Waiting for all futures...");

            long completed = futures.stream().filter(CompletableFuture::isDone).count();

            log.info("Completed before join: {}/{}", completed, futures.size());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("All futures completed.");
            log.info("Collecting futures...");
            List<TafsirResponse> allAyahs = new ArrayList<>();
            for (CompletableFuture<TafsirResponse> future : futures) {
                allAyahs.add(future.get());
            }

            allAyahs.sort(Comparator.comparingInt(TafsirResponse::surah)
                    .thenComparingInt(TafsirResponse::ayah));

            log.info("Successfully fetched and sorted {} ayahs for Tafsir {}", allAyahs.size(), tafsirKey);

            String jsonContent = objectMapper.writeValueAsString(allAyahs);
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
                gzip.write(jsonContent.getBytes(StandardCharsets.UTF_8));
            }
            byte[] fileBytes = obj.toByteArray();

            String fileName = tafsirKey + "_" + language + "_full.json.gz";
            log.info("Uploading to Cloudinary...");
            String cloudinaryUrl = cloudinaryService.uploadRawFile(fileBytes, fileName, "almahir/tafsirs");

            metadata.setFileUrl(cloudinaryUrl);
            metadata.setFileSizeBytes((long) fileBytes.length);
            metadata.setStatus(TafsirBuildStatus.READY);
            metadataRepository.save(metadata);

            log.info("Successfully built and deployed Tafsir: {} - URL: {}", tafsirKey, cloudinaryUrl);

        } catch (Exception e) {
            log.error("Failed to build Tafsir {} ({})", tafsirKey, language, e);
            metadata.setStatus(TafsirBuildStatus.FAILED);
            metadataRepository.save(metadata);
        } finally {
            executor.shutdown();
        }

        return CompletableFuture.completedFuture(null);
    }

    private TafsirResponse fetchAyahWithRetry(String slug, int surah, int ayah, int retriesLeft) {
        try {
            TafsirRawResponse raw = tafsirClient.getAyahTafsir(slug, surah, ayah);
            return new TafsirResponse(surah, ayah, raw.text());
        } catch (Exception e) {
            if (retriesLeft > 0) {
                log.warn("Retrying fetch for Surah {} Ayah {} (Attempts left: {})", surah, ayah, retriesLeft - 1);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                return fetchAyahWithRetry(slug, surah, ayah, retriesLeft - 1);
            } else {
                log.error("Failed to fetch Surah {} Ayah {} after retries", surah, ayah, e);
                throw e;
            }
        }
    }
}