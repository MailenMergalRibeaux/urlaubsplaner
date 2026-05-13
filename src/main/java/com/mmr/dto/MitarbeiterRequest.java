package com.mmr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.mmr.domain.Bundesland;

public record MitarbeiterRequest(
        @NotBlank(message = "ID darf nicht leer sein")
        String id,

        @NotBlank(message = "Vorname darf nicht leer sein")
        String vorname,

        @NotBlank(message = "Nachname darf nicht leer sein")
        String nachname,

        @NotBlank @Email(message = "Ungültige E-Mail-Adresse")
        String email,

        @NotNull(message = "Bundesland ist erforderlich")
        Bundesland bundesland,

        String vorgesetzterMitarbeiterId
) {
}

