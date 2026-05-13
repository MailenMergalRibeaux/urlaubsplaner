package com.mmr.controller;

import jakarta.validation.Valid;
import com.mmr.dto.UrlaubskontoRequest;
import com.mmr.dto.UrlaubskontoResponse;
import com.mmr.service.UrlaubskontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urlaubskonten")
@RequiredArgsConstructor
public class UrlaubskontoController {

    private final UrlaubskontoService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UrlaubskontoResponse anlegen(@Valid @RequestBody UrlaubskontoRequest request) {
        return service.anlegen(request);
    }

    @GetMapping
    public List<UrlaubskontoResponse> findByMitarbeiter(
            @RequestParam String mitarbeiterId) {
        return service.findByMitarbeiter(mitarbeiterId);
    }

    @GetMapping("/{mitarbeiterId}/{jahr}")
    public UrlaubskontoResponse findByMitarbeiterUndJahr(
            @PathVariable String mitarbeiterId,
            @PathVariable int jahr) {
        return service.findByMitarbeiterUndJahr(mitarbeiterId, jahr);
    }

    @PatchMapping("/{mitarbeiterId}/{jahr}/gesamttage")
    public UrlaubskontoResponse gesamtTageAktualisieren(
            @PathVariable String mitarbeiterId,
            @PathVariable int jahr,
            @RequestParam int gesamtTage) {
        return service.gesamtTageAktualisieren(mitarbeiterId, jahr, gesamtTage);
    }
}

