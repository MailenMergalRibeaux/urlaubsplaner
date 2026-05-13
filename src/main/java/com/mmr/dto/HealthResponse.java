package com.mmr.dto;

import java.time.OffsetDateTime;

public record HealthResponse(String status, String service, OffsetDateTime timestamp) {
}

