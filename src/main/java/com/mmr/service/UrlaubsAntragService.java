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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UrlaubsAntragService {

    private static final Set<AntragStatus> STATUS_MIT_TAGE_FREIGABE =
            EnumSet.of(AntragStatus.ABGELEHNT, AntragStatus.STORNIERT);

    private final UrlaubsAntragRepository repository;
    private final UrlaubskontoService urlaubskontoService;

    public UrlaubsAntragResponse erstellen(UrlaubsAntragRequest request) {
        validiereZeitraum(request);
        UrlaubsAntrag antrag = mapRequestToAntrag(new UrlaubsAntrag(), request);
        return speichernUndMappen(antrag);
    }

    @Transactional(readOnly = true)
    public UrlaubsAntragResponse findById(Long id) {
        return UrlaubsAntragResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findAll() {
        return toResponses(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findByMitarbeiter(String mitarbeiterId) {
        return toResponses(repository.findByMitarbeiterId(mitarbeiterId));
    }

    @Transactional(readOnly = true)
    public List<UrlaubsAntragResponse> findByStatus(AntragStatus status) {
        return toResponses(repository.findByStatus(status));
    }

    public UrlaubsAntragResponse aktualisieren(Long id, UrlaubsAntragRequest request) {
        validiereZeitraum(request);
        UrlaubsAntrag antrag = getOrThrow(id);
        assertIstBearbeitbar(antrag);
        return speichernUndMappen(mapRequestToAntrag(antrag, request));
    }

    public UrlaubsAntragResponse statusAktualisieren(Long id, StatusUpdateRequest request) {
        UrlaubsAntrag antrag = getOrThrow(id);
        AntragStatus alterStatus = antrag.getStatus();
        aktualisiereStatusUndKommentar(antrag, request);

        UrlaubsAntrag gespeichert = repository.save(antrag);
        triggerKontoBuchung(alterStatus, gespeichert);
        return UrlaubsAntragResponse.from(gespeichert);
    }

    public void loeschen(Long id) {
        UrlaubsAntrag antrag = getOrThrow(id);
        if (antrag.getStatus() == AntragStatus.GENEHMIGT) {
            throw new IllegalStateException("Ein genehmigter Antrag kann nicht gelöscht werden.");
        }
        repository.delete(antrag);
    }

    // --- private Hilfsmethoden ---
    private UrlaubsAntrag mapRequestToAntrag(UrlaubsAntrag antrag, UrlaubsAntragRequest request) {
        antrag.setMitarbeiterId(request.mitarbeiterId());
        antrag.setStartdatum(request.startdatum());
        antrag.setEnddatum(request.enddatum());
        if (request.urlaubsart() != null) {
            antrag.setUrlaubsart(request.urlaubsart());
        }
        antrag.setKommentar(request.kommentar());
        return antrag;
    }

    private void triggerKontoBuchung(AntragStatus alterStatus, UrlaubsAntrag antrag) {
        AntragStatus neuerStatus = antrag.getStatus();
        if (neuerStatus == AntragStatus.GENEHMIGT && alterStatus != AntragStatus.GENEHMIGT) {
            urlaubskontoService.tagebuchen(antrag);
        } else if (STATUS_MIT_TAGE_FREIGABE.contains(neuerStatus) && alterStatus == AntragStatus.GENEHMIGT) {
            urlaubskontoService.tageFreigeben(antrag);
        }
    }

    private void aktualisiereStatusUndKommentar(UrlaubsAntrag antrag, StatusUpdateRequest request) {
        antrag.setStatus(request.status());
        if (request.kommentar() != null) {
            antrag.setKommentar(request.kommentar());
        }
    }

    private void assertIstBearbeitbar(UrlaubsAntrag antrag) {
        if (antrag.getStatus() != AntragStatus.BEANTRAGT) {
            throw new IllegalStateException(
                    "Antrag kann nur im Status BEANTRAGT bearbeitet werden, aktueller Status: " + antrag.getStatus()
            );
        }
    }

    private List<UrlaubsAntragResponse> toResponses(List<UrlaubsAntrag> antraege) {
        return antraege.stream().map(UrlaubsAntragResponse::from).toList();
    }

    private UrlaubsAntragResponse speichernUndMappen(UrlaubsAntrag antrag) {
        return UrlaubsAntragResponse.from(repository.save(antrag));
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
