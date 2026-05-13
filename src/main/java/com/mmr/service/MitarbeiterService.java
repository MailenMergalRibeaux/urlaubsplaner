package com.mmr.service;

import com.mmr.domain.Mitarbeiter;
import com.mmr.dto.MitarbeiterRequest;
import com.mmr.dto.MitarbeiterResponse;
import com.mmr.exception.MitarbeiterNotFoundException;
import com.mmr.repository.MitarbeiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MitarbeiterService {

    private final MitarbeiterRepository repository;


    public MitarbeiterResponse anlegen(MitarbeiterRequest request) {
        if (repository.existsById(request.id())) {
            throw new IllegalStateException("Mitarbeiter mit ID '" + request.id() + "' existiert bereits.");
        }
        repository.findByEmail(request.email()).ifPresent(m -> {
            throw new IllegalStateException("E-Mail '" + request.email() + "' ist bereits vergeben.");
        });
        Mitarbeiter m = new Mitarbeiter();
        return MitarbeiterResponse.from(repository.save(mapToEntity(m, request)));
    }

    @Transactional(readOnly = true)
    public MitarbeiterResponse findById(String id) {
        return MitarbeiterResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MitarbeiterResponse> findAll() {
        return repository.findAll().stream().map(MitarbeiterResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<MitarbeiterResponse> findByVorgesetzter(String vorgesetzterMitarbeiterId) {
        return repository.findByVorgesetzterMitarbeiterId(vorgesetzterMitarbeiterId)
                .stream().map(MitarbeiterResponse::from).toList();
    }

    public MitarbeiterResponse aktualisieren(String id, MitarbeiterRequest request) {
        Mitarbeiter m = getOrThrow(id);
        return MitarbeiterResponse.from(repository.save(mapToEntity(m, request)));
    }

    public void loeschen(String id) {
        repository.delete(getOrThrow(id));
    }

    private Mitarbeiter mapToEntity(Mitarbeiter m, MitarbeiterRequest r) {
        m.setId(r.id());
        m.setVorname(r.vorname());
        m.setNachname(r.nachname());
        m.setEmail(r.email());
        m.setBundesland(r.bundesland());
        m.setVorgesetzterMitarbeiterId(r.vorgesetzterMitarbeiterId());
        return m;
    }

    private Mitarbeiter getOrThrow(String id) {
        return repository.findById(id).orElseThrow(() -> new MitarbeiterNotFoundException(id));
    }
}

