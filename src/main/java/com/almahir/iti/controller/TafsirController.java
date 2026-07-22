package com.almahir.iti.controller;

import com.almahir.iti.dto.response.ApiResponse;
import com.almahir.iti.dto.response.TafsirResponse;
import com.almahir.iti.service.TafsirService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tafsir")
@RequiredArgsConstructor
@Tag(name = "Tafsir", description = "Quran verse interpretation (tafsir) endpoints")
public class TafsirController {


    private final TafsirService tafsirService;

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
}