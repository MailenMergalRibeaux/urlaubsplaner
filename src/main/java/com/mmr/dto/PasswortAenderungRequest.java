package com.mmr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswortAenderungRequest(
        @NotBlank(message = "Aktuelles Passwort darf nicht leer sein")
        String altesPasswort,

        @NotBlank @Size(min = 8, message = "Neues Passwort muss mindestens 8 Zeichen lang sein")
        String neuesPasswort
) {
}
