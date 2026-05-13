package com.mmr.exception;

public class NichtGenugUrlaubstageException extends RuntimeException {

    public NichtGenugUrlaubstageException(int beantragt, int verfuegbar) {
        super("Nicht genug Urlaubstage: beantragt=" + beantragt + ", verfügbar=" + verfuegbar + ".");
    }
}

