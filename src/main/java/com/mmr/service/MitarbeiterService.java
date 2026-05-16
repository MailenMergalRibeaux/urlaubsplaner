package com.mmr.service;

import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;
import com.mmr.dto.MitarbeiterRequest;
import com.mmr.dto.MitarbeiterResponse;
import com.mmr.dto.RegisterFuehrungskraftRequest;
import com.mmr.exception.MitarbeiterNotFoundException;
import com.mmr.repository.MitarbeiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MitarbeiterService {

    private final MitarbeiterRepository repository;
    private final PasswordEncoder passwordEncoder;


    public MitarbeiterResponse anlegen(MitarbeiterRequest request) {
        if (request.rolle() != Rolle.MITARBEITER) {
            throw new IllegalArgumentException(
                    "Ueber diesen Endpoint koennen nur MITARBEITER-Konten angelegt werden. "
                            + "Fuehrungskraefte registrieren sich ueber /api/auth/register.");
        }
        if (repository.existsById(request.id())) {
            throw new IllegalStateException("Mitarbeiter mit ID '" + request.id() + "' existiert bereits.");
        }
        repository.findByEmail(request.email()).ifPresent(m -> {
            throw new IllegalStateException("E-Mail '" + request.email() + "' ist bereits vergeben.");
        });
        if (request.passwort() == null || request.passwort().isBlank()) {
            throw new IllegalArgumentException("Passwort ist beim Anlegen erforderlich.");
        }
        Mitarbeiter m = new Mitarbeiter();
        m.setPasswortHash(passwordEncoder.encode(request.passwort()));
        return MitarbeiterResponse.from(repository.save(mapToEntity(m, request)));
    }

    public MitarbeiterResponse registriereFuehrungskraft(RegisterFuehrungskraftRequest request) {
        if (repository.existsById(request.id())) {
            throw new IllegalStateException("Mitarbeiter mit ID '" + request.id() + "' existiert bereits.");
        }
        repository.findByEmail(request.email()).ifPresent(m -> {
            throw new IllegalStateException("E-Mail '" + request.email() + "' ist bereits vergeben.");
        });
        Mitarbeiter m = new Mitarbeiter();
        m.setId(request.id());
        m.setVorname(request.vorname());
        m.setNachname(request.nachname());
        m.setEmail(request.email());
        m.setPasswortHash(passwordEncoder.encode(request.passwort()));
        m.setRolle(Rolle.FUEHRUNGSKRAFT);
        m.setBundesland(request.bundesland());
        m.setVorgesetzterMitarbeiterId(request.vorgesetzterMitarbeiterId());
        return MitarbeiterResponse.from(repository.save(m));
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
        if (m.getRolle() == Rolle.FUEHRUNGSKRAFT && request.rolle() != Rolle.FUEHRUNGSKRAFT) {
            throw new IllegalArgumentException("Die Rolle einer Fuehrungskraft kann nicht geaendert werden.");
        }
        if (m.getRolle() == Rolle.MITARBEITER && request.rolle() != Rolle.MITARBEITER) {
            throw new IllegalArgumentException("Mitarbeiter koennen nicht zu Fuehrungskraeften befoerdert werden. "
                    + "Eine neue FK registriert sich ueber /api/auth/register.");
        }
        if (request.passwort() != null && !request.passwort().isBlank()) {
            m.setPasswortHash(passwordEncoder.encode(request.passwort()));
        }
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
        m.setRolle(r.rolle());
        m.setBundesland(r.bundesland());
        m.setVorgesetzterMitarbeiterId(r.vorgesetzterMitarbeiterId());
        return m;
    }

    private Mitarbeiter getOrThrow(String id) {
        return repository.findById(id).orElseThrow(() -> new MitarbeiterNotFoundException(id));
    }
}
