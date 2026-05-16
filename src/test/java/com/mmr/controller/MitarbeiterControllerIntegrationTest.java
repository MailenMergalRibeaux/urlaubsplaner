package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmr.domain.Bundesland;
import com.mmr.domain.Rolle;
import com.mmr.dto.MitarbeiterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "fuehrungskraft@local", roles = "FUEHRUNGSKRAFT")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MitarbeiterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void mitarbeiterAnlegenUndAbrufen() throws Exception {
        MitarbeiterRequest request = new MitarbeiterRequest(
                "MA-100", "Max", "Mustermann", "max@example.com",
                "geheim12", Rolle.MITARBEITER, Bundesland.NW, null
        );

        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("MA-100"))
                .andExpect(jsonPath("$.rolle").value("MITARBEITER"))
                .andExpect(jsonPath("$.bundesland").value("NW"))
                .andExpect(jsonPath("$.passwort").doesNotExist())
                .andExpect(jsonPath("$.passwortHash").doesNotExist());

        mockMvc.perform(get("/api/mitarbeiter/MA-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vorname").value("Max"));
    }

    @Test
    void doppelteMitarbeiterId_gibtKonflikt() throws Exception {
        MitarbeiterRequest request = new MitarbeiterRequest(
                "MA-101", "Anna", "Schmidt", "anna@example.com",
                "geheim12", Rolle.MITARBEITER, Bundesland.BY, null
        );
        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void mitarbeiterNichtGefunden_gibtNotFound() throws Exception {
        mockMvc.perform(get("/api/mitarbeiter/UNBEKANNT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void anlegenMitRolleFuehrungskraft_gibtBadRequest() throws Exception {
        MitarbeiterRequest request = new MitarbeiterRequest(
                "FK-X", "Frank", "Falsch", "fk-x@example.com",
                "geheim12", Rolle.FUEHRUNGSKRAFT, Bundesland.NW, null
        );

        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
