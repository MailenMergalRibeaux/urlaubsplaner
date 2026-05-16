package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmr.domain.Bundesland;
import com.mmr.domain.Mitarbeiter;
import com.mmr.domain.Rolle;
import com.mmr.dto.LoginRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    private static final String EMAIL = "login@example.com";
    private static final String PASSWORT = "geheim123";

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
        m.setId("MA-LOGIN-1");
        m.setVorname("Lisa");
        m.setNachname("Login");
        m.setEmail(EMAIL);
        m.setPasswortHash(passwordEncoder.encode(PASSWORT));
        m.setRolle(Rolle.MITARBEITER);
        m.setBundesland(Bundesland.NW);
        repository.save(m);
    }

    @Test
    void login_mitKorrektenCredentials_gibtProfilZurueck() throws Exception {
        LoginRequest req = new LoginRequest(EMAIL, PASSWORT);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("MA-LOGIN-1"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.rolle").value("MITARBEITER"))
                .andExpect(jsonPath("$.passwort").doesNotExist())
                .andExpect(jsonPath("$.passwortHash").doesNotExist());
    }

    @Test
    void login_mitFalschemPasswort_gibtUnauthorized() throws Exception {
        LoginRequest req = new LoginRequest(EMAIL, "FALSCH123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_mitUnbekannterEmail_gibtUnauthorized() throws Exception {
        LoginRequest req = new LoginRequest("unbekannt@example.com", PASSWORT);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_mitLeeremPasswort_gibtBadRequest() throws Exception {
        String body = "{\"email\":\"" + EMAIL + "\",\"passwort\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_mitUngueltigerEmail_gibtBadRequest() throws Exception {
        String body = "{\"email\":\"kein-format\",\"passwort\":\"geheim123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_brauchtKeineAuthentifizierung() throws Exception {
        LoginRequest req = new LoginRequest(EMAIL, PASSWORT);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
