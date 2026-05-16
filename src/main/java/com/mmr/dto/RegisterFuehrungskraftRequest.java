package com.mmr.dto;

import com.mmr.domain.Bundesland;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterFuehrungskraftRequest(
        @NotBlank(message = "ID darf nicht leer sein")
        String id,

        @NotBlank(message = "Vorname darf nicht leer sein")
        String vorname,

        @NotBlank(message = "Nachname darf nicht leer sein")
        String nachname,

        @NotBlank @Email(message = "Ungültige E-Mail-Adresse")
        String email,

        @NotBlank @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
        String passwort,

        @NotNull(message = "Bundesland ist erforderlich")
        Bundesland bundesland,

        String vorgesetzterMitarbeiterId,

        @NotBlank(message = "Invite-Code ist erforderlich")
        String inviteCode
) {
}
