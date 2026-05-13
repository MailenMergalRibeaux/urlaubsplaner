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
    public List<FeiertagResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/zeitraum-bundesland")
    public List<FeiertagResponse> findByZeitraumUndBundesland(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam Bundesland bundesland) {
        return service.findByZeitraumUndBundesland(von, bis, bundesland);
    }

    @GetMapping("/zeitraum")
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

