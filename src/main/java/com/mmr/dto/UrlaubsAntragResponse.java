package com.mmr.dto;

import com.mmr.domain.AntragStatus;
import com.mmr.domain.UrlaubsAntrag;
import com.mmr.domain.Urlaubsart;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UrlaubsAntragResponse(
        Long id,
        String mitarbeiterId,
        LocalDate startdatum,
        LocalDate enddatum,
        Urlaubsart urlaubsart,
        AntragStatus status,
        String kommentar,
        LocalDateTime erstelltAm,
        LocalDateTime geaendertAm
) {
    public static UrlaubsAntragResponse from(UrlaubsAntrag antrag) {
        return new UrlaubsAntragResponse(
                antrag.getId(),
                antrag.getMitarbeiterId(),
                antrag.getStartdatum(),
                antrag.getEnddatum(),
                antrag.getUrlaubsart(),
                antrag.getStatus(),
                antrag.getKommentar(),
                antrag.getErstelltAm(),
                antrag.getGeaendertAm()
        );
    }
}

