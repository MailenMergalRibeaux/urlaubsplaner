package com.mmr.service;

import com.mmr.domain.Bundesland;
import com.mmr.domain.OeffentlicherFeiertag;
import com.mmr.dto.FeiertagRequest;
import com.mmr.dto.FeiertagResponse;
import com.mmr.repository.OeffentlicherFeiertagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FeiertagService {

    private final OeffentlicherFeiertagRepository repository;


    public FeiertagResponse anlegen(FeiertagRequest request) {
        OeffentlicherFeiertag f = new OeffentlicherFeiertag();
        f.setBezeichnung(request.bezeichnung());
        f.setDatum(request.datum());
        f.setBundesland(request.bundesland());
        return FeiertagResponse.from(repository.save(f));
    }

    @Transactional(readOnly = true)
    public List<FeiertagResponse> findByZeitraumUndBundesland(LocalDate von, LocalDate bis, Bundesland bundesland) {
        return findByZeitraum(von, bis).stream()
                .filter(Objects::nonNull)
                .filter(f -> f.bundesland().equals(bundesland))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeiertagResponse> findByZeitraum(LocalDate von, LocalDate bis) {
        return repository.findByDatumBetweenAndBundeslandIsNull(von, bis)
                .stream()
                .filter(Objects::nonNull).map(FeiertagResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FeiertagResponse> findAll() {
        return repository.findAll().stream().map(FeiertagResponse::from).toList();
    }

    public void loeschen(Long id) {
        repository.deleteById(id);
    }

    /**
     * Gibt alle Feiertagsdaten (LocalDate) für einen Zeitraum und ein Bundesland zurück.
     * Wird von UrlaubskontoService genutzt.
     */
    @Transactional(readOnly = true)
    public Set<LocalDate> getFeiertagsDaten(LocalDate von, LocalDate bis, Bundesland bundesland) {
        return repository.findByZeitraumUndBundesland(von, bis, bundesland)
                .stream()
                .map(OeffentlicherFeiertag::getDatum)
                .collect(Collectors.toSet());
    }
}

