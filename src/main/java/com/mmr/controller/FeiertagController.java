package com.mmr.controller;

import jakarta.validation.Valid;
import com.mmr.domain.Bundesland;
import com.mmr.dto.FeiertagRequest;
import com.mmr.dto.FeiertagResponse;
import com.mmr.service.FeiertagService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/feiertage")
@RequiredArgsConstructor
public class FeiertagController {

    private final FeiertagService service;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeiertagResponse anlegen(@Valid @RequestBody FeiertagRequest request) {
        return service.anlegen(request);
    }

    @GetMapping
    public List<FeiertagResponse> find(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(required = false) Bundesland bundesland) {

        if (von != null && bis != null && bundesland != null) {
            return service.findByZeitraumUndBundesland(von, bis, bundesland);
        }
        return service.findAll();
    }

    @GetMapping("/by-zeitraum")
    public List<FeiertagResponse> findByZeitraum(
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam() @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis) {
        return service.findByZeitraum(von, bis);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void loeschen(@PathVariable Long id) {
        service.loeschen(id);
    }
}

