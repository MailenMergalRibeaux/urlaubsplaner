package com.mmr.service;

import com.mmr.dto.HealthResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse(
                "UP",
                "urlaubsplaner",
                OffsetDateTime.now(ZoneId.of("Europe/Berlin"))
        );
    }
}

