package com.mmr.config;

import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;
import com.mmr.repository.MitarbeiterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Legt eine initiale Fuehrungskraft an, falls die Tabelle leer ist.
 * Es wird nur erzeugt, wenn ein Passwort via Property/Env-Variable gesetzt ist
 * (APP_FUEHRUNGSKRAFT_PASSWORD). So existiert kein hartkodiertes Default-Passwort.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FuehrungskraftInitializer implements CommandLineRunner {

    private static final int MIN_PASSWORT_LAENGE = 8;

    private final MitarbeiterRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.fuehrungskraft.id:FK-001}")
    private String fuehrungskraftId;

    @Value("${app.fuehrungskraft.email:fuehrungskraft@local}")
    private String fuehrungskraftEmail;

    @Value("${app.fuehrungskraft.password:}")
    private String fuehrungskraftPassword;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        if (fuehrungskraftPassword == null || fuehrungskraftPassword.length() < MIN_PASSWORT_LAENGE) {
            log.warn("Keine initiale Fuehrungskraft angelegt: Property 'app.fuehrungskraft.password' "
                    + "(Env: APP_FUEHRUNGSKRAFT_PASSWORD) ist nicht gesetzt oder kuerzer als {} Zeichen.",
                    MIN_PASSWORT_LAENGE);
            return;
        }
        Mitarbeiter fk = new Mitarbeiter();
        fk.setId(fuehrungskraftId);
        fk.setVorname("Fuehrungskraft");
        fk.setNachname("Initial");
        fk.setEmail(fuehrungskraftEmail);
        fk.setPasswortHash(passwordEncoder.encode(fuehrungskraftPassword));
        fk.setRolle(Rolle.FUEHRUNGSKRAFT);
        fk.setBundesland(Bundesland.NW);
        fk.setPasswortAenderungErforderlich(false);
        repository.save(fk);
        log.info("Initiale Fuehrungskraft angelegt: id={}, email={}", fuehrungskraftId, fuehrungskraftEmail);
    }
}
