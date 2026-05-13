package com.mmr.service;

import com.mmr.domain.Urlaubskonto;
import com.mmr.domain.UrlaubsAntrag;
import com.mmr.dto.UrlaubskontoRequest;
import com.mmr.dto.UrlaubskontoResponse;
import com.mmr.exception.NichtGenugUrlaubstageException;
import com.mmr.exception.UrlaubskontoNotFoundException;
import com.mmr.repository.MitarbeiterRepository;
import com.mmr.repository.UrlaubskontoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UrlaubskontoService {

    private final UrlaubskontoRepository kontoRepository;
    private final MitarbeiterRepository mitarbeiterRepository;
    private final FeiertagService feiertagService;


    public UrlaubskontoResponse anlegen(UrlaubskontoRequest request) {
        kontoRepository.findByMitarbeiterIdAndJahr(request.mitarbeiterId(), request.jahr())
                .ifPresent(k -> {
                    throw new IllegalStateException(
                            "Urlaubskonto für Mitarbeiter '" + request.mitarbeiterId()
                                    + "' im Jahr " + request.jahr() + " existiert bereits.");
                });

        Urlaubskonto konto = new Urlaubskonto();
        konto.setMitarbeiterId(request.mitarbeiterId());
        konto.setJahr(request.jahr());
        konto.setGesamtTage(request.gesamtTage());
        konto.setGebuchteTage(0);

        return UrlaubskontoResponse.from(kontoRepository.save(konto));
    }

    @Transactional(readOnly = true)
    public UrlaubskontoResponse findByMitarbeiterUndJahr(String mitarbeiterId, int jahr) {
        return UrlaubskontoResponse.from(getOrThrow(mitarbeiterId, jahr));
    }

    @Transactional(readOnly = true)
    public List<UrlaubskontoResponse> findByMitarbeiter(String mitarbeiterId) {
        return kontoRepository.findByMitarbeiterId(mitarbeiterId).stream()
                .map(UrlaubskontoResponse::from)
                .toList();
    }

    public UrlaubskontoResponse gesamtTageAktualisieren(String mitarbeiterId, int jahr, int gesamtTage) {
        Urlaubskonto konto = getOrThrow(mitarbeiterId, jahr);
        konto.setGesamtTage(gesamtTage);
        return UrlaubskontoResponse.from(kontoRepository.save(konto));
    }

    /**
     * Bucht Urlaubstage beim Genehmigen eines Antrags.
     * Wirft NichtGenugUrlaubstageException wenn das Konto nicht ausreicht.
     */
    public void tagebuchen(UrlaubsAntrag antrag) {
        Urlaubskonto konto = getOrThrow(antrag.getMitarbeiterId(), antrag.getStartdatum().getYear());
        int arbeitstage = berechneArbeitstageFuerAntrag(antrag);

        if (konto.getVerbleibendeTage() < arbeitstage) {
            throw new NichtGenugUrlaubstageException(arbeitstage, konto.getVerbleibendeTage());
        }

        aktualisiereGebuchteTage(konto, arbeitstage);
    }

    /**
     * Gibt gebuchte Tage zurück (beim Ablehnen oder Stornieren eines Antrags).
     */
    public void tageFreigeben(UrlaubsAntrag antrag) {
        kontoRepository.findByMitarbeiterIdAndJahr(antrag.getMitarbeiterId(), antrag.getStartdatum().getYear())
                .ifPresent(konto -> aktualisiereGebuchteTage(konto, -berechneArbeitstageFuerAntrag(antrag)));
    }

    /**
     * Berechnet Arbeitstage (Mo–Fr) ohne Feiertage für das Bundesland des Mitarbeiters.
     * Falls Mitarbeiter nicht gefunden, werden nur Wochenenden abgezogen.
     */
    public int berechneArbeitstage(String mitarbeiterId, LocalDate start, LocalDate ende) {
        return zaehleArbeitstage(start, ende, ladeFeiertage(mitarbeiterId, start, ende));
    }

    /**
     * Überladung ohne Mitarbeiter-Kontext (rückwärtskompatibel, kein Feiertagsabzug).
     */
    public int berechneArbeitstage(LocalDate start, LocalDate ende) {
        return zaehleArbeitstage(start, ende, Set.of());
    }

    private int berechneArbeitstageFuerAntrag(UrlaubsAntrag antrag) {
        return berechneArbeitstage(antrag.getMitarbeiterId(), antrag.getStartdatum(), antrag.getEnddatum());
    }

    private Set<LocalDate> ladeFeiertage(String mitarbeiterId, LocalDate start, LocalDate ende) {
        if (mitarbeiterId == null) {
            return Set.of();
        }

        return mitarbeiterRepository.findById(mitarbeiterId)
                .map(mitarbeiter -> feiertagService.getFeiertagsDaten(start, ende, mitarbeiter.getBundesland()))
                .orElse(Set.of());
    }

    private int zaehleArbeitstage(LocalDate start, LocalDate ende, Set<LocalDate> feiertage) {
        int tage = 0;
        LocalDate aktuell = start;
        while (!aktuell.isAfter(ende)) {
            if (istArbeitstag(aktuell, feiertage)) {
                tage++;
            }
            aktuell = aktuell.plusDays(1);
        }
        return tage;
    }

    private boolean istArbeitstag(LocalDate datum, Set<LocalDate> feiertage) {
        DayOfWeek tag = datum.getDayOfWeek();
        return tag != DayOfWeek.SATURDAY
                && tag != DayOfWeek.SUNDAY
                && !feiertage.contains(datum);
    }

    private void aktualisiereGebuchteTage(Urlaubskonto konto, int delta) {
        konto.setGebuchteTage(Math.max(0, konto.getGebuchteTage() + delta));
        kontoRepository.save(konto);
    }

    private Urlaubskonto getOrThrow(String mitarbeiterId, int jahr) {
        return kontoRepository.findByMitarbeiterIdAndJahr(mitarbeiterId, jahr)
                .orElseThrow(() -> new UrlaubskontoNotFoundException(mitarbeiterId, jahr));
    }
}

