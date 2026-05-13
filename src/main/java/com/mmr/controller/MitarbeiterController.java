package com.mmr.controller;

import jakarta.validation.Valid;
import com.mmr.dto.MitarbeiterRequest;
import com.mmr.dto.MitarbeiterResponse;
import com.mmr.service.MitarbeiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mitarbeiter")
@RequiredArgsConstructor
public class MitarbeiterController {

    private final MitarbeiterService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MitarbeiterResponse anlegen(@Valid @RequestBody MitarbeiterRequest request) {
        return service.anlegen(request);
    }

    @GetMapping
    public List<MitarbeiterResponse> findAll(
            @RequestParam(required = false) String vorgesetzterMitarbeiterId) {
        if (vorgesetzterMitarbeiterId != null) {
            return service.findByVorgesetzter(vorgesetzterMitarbeiterId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MitarbeiterResponse findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public MitarbeiterResponse aktualisieren(
            @PathVariable String id,
            @Valid @RequestBody MitarbeiterRequest request) {
        return service.aktualisieren(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@PathVariable String id) {
        service.loeschen(id);
    }
}

