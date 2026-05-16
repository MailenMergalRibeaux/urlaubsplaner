package com.mmr.controller;

import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;
import com.mmr.repository.MitarbeiterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MitarbeiterLoginIntegrationTest {

    private static final String MA_EMAIL = "anna.mitarbeiter@example.com";
    private static final String MA_PASSWORT = "geheim12";

    private static final String FK_EMAIL = "frank.fuehrung@example.com";
    private static final String FK_PASSWORT = "fuehrung12";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MitarbeiterRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUser() {
        repository.save(buildUser("MA-LOGIN", MA_EMAIL, MA_PASSWORT, Rolle.MITARBEITER));
        repository.save(buildUser("FK-LOGIN", FK_EMAIL, FK_PASSWORT, Rolle.FUEHRUNGSKRAFT));
    }

    @Test
    void mitarbeiterMitKorrektenCredentials_kannGeschuetztenEndpointAufrufen() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic(MA_EMAIL, MA_PASSWORT)))
                .andExpect(status().isOk());
    }

    @Test
    void fuehrungskraftMitKorrektenCredentials_darfAnlegen() throws Exception {
        String body = """
                {
                  "id": "MA-NEU",
                  "vorname": "Neu",
                  "nachname": "Hier",
                  "email": "neu.hier@example.com",
                  "passwort": "geheim12",
                  "rolle": "MITARBEITER",
                  "bundesland": "NW"
                }
                """;
        mockMvc.perform(post("/api/mitarbeiter")
                        .with(httpBasic(FK_EMAIL, FK_PASSWORT))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void mitarbeiterDarfNichtAnlegen() throws Exception {
        String body = """
                {
                  "id": "MA-BLOCK",
                  "vorname": "Soll",
                  "nachname": "Scheitern",
                  "email": "block@example.com",
                  "passwort": "geheim12",
                  "rolle": "MITARBEITER",
                  "bundesland": "NW"
                }
                """;
        mockMvc.perform(post("/api/mitarbeiter")
                        .with(httpBasic(MA_EMAIL, MA_PASSWORT))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void falschesPasswort_gibtUnauthorized() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic(MA_EMAIL, "FALSCH123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unbekannteEmail_gibtUnauthorized() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic("unbekannt@example.com", MA_PASSWORT)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ohneAuth_gibtUnauthorized() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpoint_brauchtKeineAnmeldung() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void alterStatischerAdmin_funktioniertNichtMehr() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic("admin", "admin123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mitarbeiterIdAlsBenutzername_wirdAbgelehnt() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic("MA-LOGIN", MA_PASSWORT)))
                .andExpect(status().isUnauthorized());
    }

    private Mitarbeiter buildUser(String id, String email, String passwort, Rolle rolle) {
        Mitarbeiter m = new Mitarbeiter();
        m.setId(id);
        m.setVorname("Test");
        m.setNachname("User");
        m.setEmail(email);
        m.setPasswortHash(passwordEncoder.encode(passwort));
        m.setRolle(rolle);
        m.setBundesland(Bundesland.NW);
        return m;
    }
}
