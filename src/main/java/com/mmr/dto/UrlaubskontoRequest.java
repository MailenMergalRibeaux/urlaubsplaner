package com.mmr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UrlaubskontoRequest(
        @NotBlank(message = "MitarbeiterId darf nicht leer sein")
        String mitarbeiterId,

        @NotNull(message = "Jahr ist erforderlich")
        @Min(value = 2000, message = "Jahr muss mindestens 2000 sein")
        @Max(value = 2100, message = "Jahr darf maximal 2100 sein")
        Integer jahr,

        @NotNull(message = "GesamtTage ist erforderlich")
        @Min(value = 1, message = "GesamtTage muss mindestens 1 sein")
        @Max(value = 365, message = "GesamtTage darf maximal 365 sein")
        Integer gesamtTage
) {
}

