package com.mmr.exception;

public class UrlaubsAntragNotFoundException extends RuntimeException {

    public UrlaubsAntragNotFoundException(Long id) {
        super("Urlaubsantrag mit ID " + id + " nicht gefunden.");
    }
}

