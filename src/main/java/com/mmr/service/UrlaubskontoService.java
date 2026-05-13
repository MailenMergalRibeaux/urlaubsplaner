package com.mmr.service;

import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Urlaubskonto;
import com.mmr.domain.UrlaubsAntrag;
import com.mmr.dto.UrlaubskontoRequest;
import com.mmr.dto.UrlaubskontoResponse;
import com.mmr.exception.NichtGenugUrlaubstageException;
import com.mmr.exception.UrlaubskontoNotFoundException;
import com.mmr.repository.MitarbeiterRepository;
import com.mmr.repository.UrlaubsAntragRepository;
import com.mmr.repository.UrlaubskontoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
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
    private final UrlaubsAntragRepository antragRepository;
    private final MitarbeiterRepository mitarbeiterRepository;
    @Lazy
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
        int jahr = antrag.getStartdatum().getYear();
        Urlaubskonto konto = getOrThrow(antrag.getMitarbeiterId(), jahr);

        int arbeitstage = berechneArbeitstage(antrag.getMitarbeiterId(),
                antrag.getStartdatum(), antrag.getEnddatum());

        if (konto.getVerbleibendeTage() < arbeitstage) {
            throw new NichtGenugUrlaubstageException(arbeitstage, konto.getVerbleibendeTage());
        }

        konto.setGebuchteTage(konto.getGebuchteTage() + arbeitstage);
        kontoRepository.save(konto);
    }

    /**
     * Gibt gebuchte Tage zurück (beim Ablehnen oder Stornieren eines Antrags).
     */
    public void tageFreigeben(UrlaubsAntrag antrag) {
        int jahr = antrag.getStartdatum().getYear();
        kontoRepository.findByMitarbeiterIdAndJahr(antrag.getMitarbeiterId(), jahr)
                .ifPresent(konto -> {
                    int arbeitstage = berechneArbeitstage(antrag.getMitarbeiterId(),
                            antrag.getStartdatum(), antrag.getEnddatum());
                    int neu = Math.max(0, konto.getGebuchteTage() - arbeitstage);
                    konto.setGebuchteTage(neu);
                    kontoRepository.save(konto);
                });
    }

    /**
     * Berechnet Arbeitstage (Mo–Fr) ohne Feiertage für das Bundesland des Mitarbeiters.
     * Falls Mitarbeiter nicht gefunden, werden nur Wochenenden abgezogen.
     */
    public int berechneArbeitstage(String mitarbeiterId, LocalDate start, LocalDate ende) {
        Bundesland bundesland = mitarbeiterRepository.findById(mitarbeiterId)
                .map(Mitarbeiter::getBundesland)
                .orElse(null);

        Set<LocalDate> feiertage = bundesland != null
                ? feiertagService.getFeiertagsDaten(start, ende, bundesland)
                : Set.of();

        int tage = 0;
        LocalDate aktuell = start;
        while (!aktuell.isAfter(ende)) {
            DayOfWeek tag = aktuell.getDayOfWeek();
            if (tag != DayOfWeek.SATURDAY && tag != DayOfWeek.SUNDAY
                    && !feiertage.contains(aktuell)) {
                tage++;
            }
            aktuell = aktuell.plusDays(1);
        }
        return tage;
    }

    /**
     * Überladung ohne Mitarbeiter-Kontext (rückwärtskompatibel, kein Feiertagsabzug).
     */
    public int berechneArbeitstage(LocalDate start, LocalDate ende) {
        return berechneArbeitstage(null, start, ende);
    }

    private Urlaubskonto getOrThrow(String mitarbeiterId, int jahr) {
        return kontoRepository.findByMitarbeiterIdAndJahr(mitarbeiterId, jahr)
                .orElseThrow(() -> new UrlaubskontoNotFoundException(mitarbeiterId, jahr));
    }
}

