package com.mmr.service;

import com.mmr.domain.AntragStatus;
import com.mmr.domain.UrlaubsAntrag;
import com.mmr.dto.StatusUpdateRequest;
import com.mmr.dto.UrlaubsAntragRequest;
import com.mmr.dto.UrlaubsAntragResponse;
import com.mmr.exception.UngueltigerZeitraumException;
import com.mmr.exception.UrlaubsAntragNotFoundException;
import com.mmr.repository.UrlaubsAntragRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UrlaubsAntragService {

    private final UrlaubsAntragRepository repository;
    @Lazy
    private final UrlaubskontoService urlaubskontoService;


    public UrlaubsAntragResponse erstellen(UrlaubsAntragRequest request) {
        validiereZeitraum(request);

        UrlaubsAntrag antrag = new UrlaubsAntrag();
        antrag.setMitarbeiterId(request.mitarbeiterId());
        antrag.setStartdatum(request.startdatum());
        antrag.setEnddatum(request.enddatum());
        if (request.urlaubsart() != null) {
            antrag.setUrlaubsart(request.urlaubsart());
        }
        antrag.setKommentar(request.kommentar());

        return UrlaubsAntragResponse.from(repository.save(antrag));
    }

    @Transactional(readOnly = true)
    public UrlaubsAntragResponse findById(Long id) {
        return UrlaubsAntragResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findAll() {
        return repository.findAll().stream()
                .map(UrlaubsAntragResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findByMitarbeiter(String mitarbeiterId) {
        return repository.findByMitarbeiterId(mitarbeiterId).stream()
                .map(UrlaubsAntragResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findByStatus(AntragStatus status) {
        return repository.findByStatus(status).stream()
                .map(UrlaubsAntragResponse::from)
                .toList();
    }

    public UrlaubsAntragResponse aktualisieren(Long id, UrlaubsAntragRequest request) {
        validiereZeitraum(request);

        UrlaubsAntrag antrag = getOrThrow(id);

        if (antrag.getStatus() != AntragStatus.BEANTRAGT) {
            throw new IllegalStateException(
                    "Antrag kann nur im Status BEANTRAGT bearbeitet werden, aktueller Status: " + antrag.getStatus()
            );
        }

        antrag.setMitarbeiterId(request.mitarbeiterId());
        antrag.setStartdatum(request.startdatum());
        antrag.setEnddatum(request.enddatum());
        if (request.urlaubsart() != null) {
            antrag.setUrlaubsart(request.urlaubsart());
        }
        antrag.setKommentar(request.kommentar());

        return UrlaubsAntragResponse.from(repository.save(antrag));
    }

    public UrlaubsAntragResponse statusAktualisieren(Long id, StatusUpdateRequest request) {
        UrlaubsAntrag antrag = getOrThrow(id);
        AntragStatus alterStatus = antrag.getStatus();
        AntragStatus neuerStatus = request.status();

        antrag.setStatus(neuerStatus);
        if (request.kommentar() != null) {
            antrag.setKommentar(request.kommentar());
        }
        UrlaubsAntrag gespeichert = repository.save(antrag);

        // Urlaubskonto-Buchung: nur wenn Konto vorhanden (optional), keine Exception wenn nicht
        if (neuerStatus == AntragStatus.GENEHMIGT && alterStatus != AntragStatus.GENEHMIGT) {
            urlaubskontoService.tagebuchen(gespeichert);
        } else if ((neuerStatus == AntragStatus.ABGELEHNT || neuerStatus == AntragStatus.STORNIERT)
                && alterStatus == AntragStatus.GENEHMIGT) {
            urlaubskontoService.tageFreigeben(gespeichert);
        }

        return UrlaubsAntragResponse.from(gespeichert);
    }

    public void loeschen(Long id) {
        UrlaubsAntrag antrag = getOrThrow(id);
        if (antrag.getStatus() == AntragStatus.GENEHMIGT) {
            throw new IllegalStateException("Ein genehmigter Antrag kann nicht gelöscht werden.");
        }
        repository.delete(antrag);
    }

    private UrlaubsAntrag getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UrlaubsAntragNotFoundException(id));
    }

    private void validiereZeitraum(UrlaubsAntragRequest request) {
        if (request.startdatum() != null && request.enddatum() != null
                && request.enddatum().isBefore(request.startdatum())) {
            throw new UngueltigerZeitraumException(
                    "Enddatum (" + request.enddatum() + ") liegt vor dem Startdatum (" + request.startdatum() + ")."
            );
        }
    }
}

