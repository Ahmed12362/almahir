package com.almahir.iti.controller;

import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.TafsirBuildStatusResponse;
import com.almahir.iti.dto.response.TafsirCatalogResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import com.almahir.iti.service.TafsirBuildService;
import com.almahir.iti.service.TafsirService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/tafsir")
@RequiredArgsConstructor
@Tag(name = "Tafsir", description = "Quran verse interpretation and download catalog endpoints")
public class TafsirController {

    private final TafsirService tafsirService;
    private final TafsirBuildService tafsirBuildService;

    @Value("${admin.build.secret}")
    private String adminSecretKey;

    @Operation(summary = "Get tafsir for an ayah", description = "Fetches the interpretation (tafsir) for a given surah and ayah.")
    @GetMapping
    public ResponseEntity<ApiResponse<TafsirResponse>> getTafsir(
            @RequestParam int surah,
            @RequestParam int ayah,
            @RequestParam(defaultValue = "ar") String lang,
            @RequestParam(defaultValue = "ibn-kathir") String tafsir) {

        TafsirResponse response = tafsirService.getTafsir(surah, ayah, lang, tafsir);

        return ResponseEntity.ok(
                ApiResponse.success("Tafsir fetched successfully.", response));
    }

    @Operation(summary = "Get available full tafsir downloads", description = "Fetches list of fully compiled tafsir JSON files available for offline download.")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TafsirCatalogResponse>>> getAvailableTafsirs() {
        List<TafsirCatalogResponse> responses = tafsirService.getAvailableTafsirs();
        return ResponseEntity.ok(ApiResponse.success("Available tafsir downloads retrieved successfully", responses));
    }

    @Hidden
    @Operation(summary = "Trigger full tafsir build process (Admin)", description = "Asynchronously fetches and compiles all ayahs for a given tafsir edition and uploads to Cloudinary.")
    @PostMapping("/build")
    public ResponseEntity<ApiResponse<Void>> triggerBuild(
            @RequestHeader(value = "X-Admin-Secret", required = false) String incomingSecret,
            @RequestParam String tafsirKey,
            @RequestParam String language) {

        if (incomingSecret == null || !incomingSecret.equals(adminSecretKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Unauthorized access: Invalid or missing secret key", null, Instant.now()));
        }

        tafsirBuildService.buildFullTafsirAsync(tafsirKey, language);

        return ResponseEntity.accepted()
                .body(ApiResponse.success("Tafsir build process started in the background for " + tafsirKey + " (" + language + ")"));
    }
    @Hidden
    @GetMapping("/build/status")
    public ResponseEntity<ApiResponse<TafsirBuildStatusResponse>> getBuildStatus(
            @RequestParam String tafsirKey,
            @RequestParam String language) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Build status fetched successfully",
                        tafsirService.getBuildStatus(tafsirKey, language)
                )
        );
    }
//    @Hidden
    @PostMapping("/build/sync")
    public ResponseEntity<ApiResponse<Void>> syncMetadata(
            @RequestHeader(value = "X-Admin-Secret", required = false) String incomingSecret) {

        if (incomingSecret == null || !incomingSecret.equals(adminSecretKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(
                            false,
                            "Unauthorized access: Invalid or missing secret key",
                            null,
                            Instant.now()
                    ));
        }

        tafsirBuildService.syncMetadataFromCloudinary();

        return ResponseEntity.ok(
                ApiResponse.success("Cloudinary metadata synced successfully.")
        );
    }
}