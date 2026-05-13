package com.mmr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.mmr.domain.Bundesland;

import java.time.LocalDate;

public record FeiertagRequest(
        @NotBlank(message = "Bezeichnung darf nicht leer sein")
        String bezeichnung,

        @NotNull(message = "Datum ist erforderlich")
        LocalDate datum,

        /** null = bundesweiter Feiertag */
        Bundesland bundesland
) {
}

