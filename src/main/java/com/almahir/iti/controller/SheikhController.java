package com.almahir.iti.controller;

import com.almahir.iti.dto.response.SheikhSearchResponse;
import com.almahir.iti.service.SheikhService;
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
public class SheikhController {

    private final SheikhService sheikhService;

    @GetMapping("/search")
    public ResponseEntity<List<SheikhSearchResponse>> search(
            @RequestParam(required = false, value = "name") String name
    ) {
        return ResponseEntity.ok(sheikhService.search(name));
    }
}
