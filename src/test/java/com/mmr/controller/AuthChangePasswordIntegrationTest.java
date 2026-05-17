package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;
import com.mmr.dto.PasswortAenderungRequest;
import com.mmr.repository.MitarbeiterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthChangePasswordIntegrationTest {

    private static final String EMAIL = "neu@example.com";
    private static final String INITIAL_PASSWORT = "Initial1234";
    private static final String NEUES_PASSWORT = "Frisch9876";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MitarbeiterRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        Mitarbeiter m = new Mitarbeiter();
        m.setId("MA-NEU");
        m.setVorname("Neu");
        m.setNachname("Mitarbeiter");
        m.setEmail(EMAIL);
        m.setPasswortHash(passwordEncoder.encode(INITIAL_PASSWORT));
        m.setRolle(Rolle.MITARBEITER);
        m.setBundesland(Bundesland.NW);
        m.setPasswortAenderungErforderlich(true);
        repository.save(m);
    }

    @Test
    void login_zeigtFlagPasswortAenderungErforderlich() throws Exception {
        String body = "{\"email\":\"" + EMAIL + "\",\"passwort\":\"" + INITIAL_PASSWORT + "\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwortAenderungErforderlich").value(true));
    }

    @Test
    void normalerEndpoint_istBlockiert_solangeAenderungAusstehend() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_mitKorrektemAltenPasswort_klaertFlag() throws Exception {
        PasswortAenderungRequest req = new PasswortAenderungRequest(INITIAL_PASSWORT, NEUES_PASSWORT);

        mockMvc.perform(post("/api/auth/change-password")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwortAenderungErforderlich").value(false));

        // Altes Passwort funktioniert nicht mehr
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT)))
                .andExpect(status().isUnauthorized());

        // Neues Passwort funktioniert und Endpoint ist nicht mehr gesperrt
        mockMvc.perform(get("/api/mitarbeiter")
                        .with(httpBasic(EMAIL, NEUES_PASSWORT)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_mitFalschemAltenPasswort_gibtBadRequest() throws Exception {
        PasswortAenderungRequest req = new PasswortAenderungRequest("FALSCH-ALT", NEUES_PASSWORT);

        mockMvc.perform(post("/api/auth/change-password")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_mitGleichemPasswort_gibtBadRequest() throws Exception {
        PasswortAenderungRequest req = new PasswortAenderungRequest(INITIAL_PASSWORT, INITIAL_PASSWORT);

        mockMvc.perform(post("/api/auth/change-password")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_mitZuKurzemNeuenPasswort_gibtBadRequest() throws Exception {
        PasswortAenderungRequest req = new PasswortAenderungRequest(INITIAL_PASSWORT, "kurz");

        mockMvc.perform(post("/api/auth/change-password")
                        .with(httpBasic(EMAIL, INITIAL_PASSWORT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_ohneAuth_gibtUnauthorized() throws Exception {
        PasswortAenderungRequest req = new PasswortAenderungRequest(INITIAL_PASSWORT, NEUES_PASSWORT);

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
