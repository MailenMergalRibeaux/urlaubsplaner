package com.mmr.exception;

public class UrlaubskontoNotFoundException extends RuntimeException {

    public UrlaubskontoNotFoundException(String mitarbeiterId, int jahr) {
        super("Urlaubskonto für Mitarbeiter '" + mitarbeiterId + "' im Jahr " + jahr + " nicht gefunden.");
    }
}

