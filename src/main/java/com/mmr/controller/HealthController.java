package com.mmr.controller;

import com.mmr.dto.HealthResponse;
import com.mmr.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;


    @GetMapping
    public HealthResponse health() {
        return healthService.getHealth();
    }
}

