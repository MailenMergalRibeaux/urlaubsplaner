package com.mmr.dto;

import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;

public record MitarbeiterResponse(
        String id,
        String vorname,
        String nachname,
        String email,
        Rolle rolle,
        Bundesland bundesland,
        String vorgesetzterMitarbeiterId
) {
    public static MitarbeiterResponse from(Mitarbeiter m) {
        return new MitarbeiterResponse(
                m.getId(),
                m.getVorname(),
                m.getNachname(),
                m.getEmail(),
                m.getRolle(),
                m.getBundesland(),
                m.getVorgesetzterMitarbeiterId()
        );
    }
}
