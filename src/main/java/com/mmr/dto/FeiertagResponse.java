package com.mmr.dto;

import com.mmr.domain.Bundesland;
import com.mmr.domain.OeffentlicherFeiertag;

import java.time.LocalDate;

public record FeiertagResponse(
        Long id,
        String bezeichnung,
        LocalDate datum,
        Bundesland bundesland
) {
    public static FeiertagResponse from(OeffentlicherFeiertag f) {
        return new FeiertagResponse(f.getId(), f.getBezeichnung(), f.getDatum(), f.getBundesland());
    }
}

