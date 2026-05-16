package com.mmr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email(message = "Ungültige E-Mail-Adresse")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        String passwort
) {
}
