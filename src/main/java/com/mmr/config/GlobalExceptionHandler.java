package com.mmr.config;

import com.mmr.exception.MitarbeiterNotFoundException;
import com.mmr.exception.NichtGenugUrlaubstageException;
import com.mmr.exception.UngueltigerZeitraumException;
import com.mmr.exception.UrlaubsAntragNotFoundException;
import com.mmr.exception.UrlaubskontoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlaubsAntragNotFoundException.class)
    public ProblemDetail handleNotFound(UrlaubsAntragNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UrlaubskontoNotFoundException.class)
    public ProblemDetail handleKontoNotFound(UrlaubskontoNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MitarbeiterNotFoundException.class)
    public ProblemDetail handleMitarbeiterNotFound(MitarbeiterNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NichtGenugUrlaubstageException.class)
    public ProblemDetail handleNichtGenugTage(NichtGenugUrlaubstageException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(UngueltigerZeitraumException.class)
    public ProblemDetail handleUngueltigerZeitraum(UngueltigerZeitraumException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}

