package com.mmr.dto;

import com.mmr.domain.Urlaubskonto;

public record UrlaubskontoResponse(
        Long id,
        String mitarbeiterId,
        int jahr,
        int gesamtTage,
        int gebuchteTage,
        int verbleibendeTage
) {
    public static UrlaubskontoResponse from(Urlaubskonto konto) {
        return new UrlaubskontoResponse(
                konto.getId(),
                konto.getMitarbeiterId(),
                konto.getJahr(),
                konto.getGesamtTage(),
                konto.getGebuchteTage(),
                konto.getVerbleibendeTage()
        );
    }
}

