package com.almahir.iti.controller;

import com.almahir.iti.dto.response.SheikhSearchResponse;
import com.almahir.iti.service.SheikhService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sheikh")
@RequiredArgsConstructor
@Tag(name = "Sheikh Management", description = "Search and view details for Sheikhs")
public class SheikhController {

    private final SheikhService sheikhService;

    @Operation(summary = "Search Sheikhs", description = "Search registered Sheikhs optionally filtering by name.")
    @GetMapping("/search")
    public ResponseEntity<List<SheikhSearchResponse>> search(
            @Parameter(description = "Optional name query string")
            @RequestParam(required = false, value = "name") String name
    ) {
        return ResponseEntity.ok(sheikhService.search(name));
    }
}
