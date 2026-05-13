package com.mmr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.mmr.domain.Urlaubsart;

import java.time.LocalDate;

public record UrlaubsAntragRequest(
        @NotBlank(message = "MitarbeiterId darf nicht leer sein")
        String mitarbeiterId,

        @NotNull(message = "Startdatum ist erforderlich")
        LocalDate startdatum,

        @NotNull(message = "Enddatum ist erforderlich")
        LocalDate enddatum,

        Urlaubsart urlaubsart,

        String kommentar
) {
}

