package com.mmr.dto;

import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;

public record MitarbeiterResponse(
        String id,
        String vorname,
        String nachname,
        String email,
        Bundesland bundesland,
        String vorgesetzterMitarbeiterId
) {
    public static MitarbeiterResponse from(Mitarbeiter m) {
        return new MitarbeiterResponse(
                m.getId(),
                m.getVorname(),
                m.getNachname(),
                m.getEmail(),
                m.getBundesland(),
                m.getVorgesetzterMitarbeiterId()
        );
    }
}

