package com.mmr.dto;

import com.mmr.domain.AntragStatus;

import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status ist erforderlich")
        AntragStatus status,

        String kommentar
) {
}

