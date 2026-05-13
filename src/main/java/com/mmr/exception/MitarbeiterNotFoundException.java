package com.mmr.exception;

public class MitarbeiterNotFoundException extends RuntimeException {
    public MitarbeiterNotFoundException(String id) {
        super("Mitarbeiter mit ID '" + id + "' nicht gefunden.");
    }
}

