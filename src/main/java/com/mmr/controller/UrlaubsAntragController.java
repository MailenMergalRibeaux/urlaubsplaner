package com.mmr.controller;

import jakarta.validation.Valid;
import com.mmr.domain.AntragStatus;
import com.mmr.dto.StatusUpdateRequest;
import com.mmr.dto.UrlaubsAntragRequest;
import com.mmr.dto.UrlaubsAntragResponse;
import com.mmr.service.UrlaubsAntragService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urlaubsantraege")
@RequiredArgsConstructor
public class UrlaubsAntragController {

    private final UrlaubsAntragService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UrlaubsAntragResponse erstellen(@Valid @RequestBody UrlaubsAntragRequest request) {
        return service.erstellen(request);
    }

    @GetMapping
    public List<UrlaubsAntragResponse> findAll(
            @RequestParam(required = false) String mitarbeiterId,
            @RequestParam(required = false) AntragStatus status) {

        if (mitarbeiterId != null) {
            return service.findByMitarbeiter(mitarbeiterId);
        }
        if (status != null) {
            return service.findByStatus(status);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UrlaubsAntragResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public UrlaubsAntragResponse aktualisieren(
            @PathVariable Long id,
            @Valid @RequestBody UrlaubsAntragRequest request) {
        return service.aktualisieren(id, request);
    }

    @PatchMapping("/{id}/status")
    public UrlaubsAntragResponse statusAktualisieren(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return service.statusAktualisieren(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@PathVariable Long id) {
        service.loeschen(id);
    }
}

