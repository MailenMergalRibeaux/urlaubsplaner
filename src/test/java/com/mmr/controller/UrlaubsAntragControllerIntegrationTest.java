package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mmr.domain.AntragStatus;
import com.mmr.domain.Urlaubsart;
import com.mmr.dto.StatusUpdateRequest;
import com.mmr.dto.UrlaubsAntragRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = "USER")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UrlaubsAntragControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void antraegErstellenUndAbrufen() throws Exception {
        UrlaubsAntragRequest request = new UrlaubsAntragRequest(
                "MA-001",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 10),
                Urlaubsart.ERHOLUNGSURLAUB,
                "Sommerurlaub"
        );

        mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.mitarbeiterId").value("MA-001"))
                .andExpect(jsonPath("$.status").value("BEANTRAGT"))
                .andExpect(jsonPath("$.urlaubsart").value("ERHOLUNGSURLAUB"));
    }

    @Test
    void antragNichtGefunden_gibtNotFound() throws Exception {
        mockMvc.perform(get("/api/urlaubsantraege/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validierungsFehler_gibtBadRequest() throws Exception {
        UrlaubsAntragRequest request = new UrlaubsAntragRequest(
                "",  // mitarbeiterId leer
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 10),
                null,
                null
        );

        mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ungueltigerZeitraum_gibtBadRequest() throws Exception {
        UrlaubsAntragRequest request = new UrlaubsAntragRequest(
                "MA-001",
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 1),  // Ende vor Start
                null,
                null
        );

        mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusAktualisieren_gelingt() throws Exception {
        // Urlaubskonto anlegen (seit Konto-Modul Pflicht bei GENEHMIGT)
        com.mmr.dto.UrlaubskontoRequest kontoRequest =
                new com.mmr.dto.UrlaubskontoRequest("MA-002", 2026, 30);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        // Antrag erstellen: Mo 03.08. bis Fr 07.08.2026 = 5 Arbeitstage
        UrlaubsAntragRequest request = new UrlaubsAntragRequest(
                "MA-002",
                LocalDate.of(2026, 8, 3),
                LocalDate.of(2026, 8, 7),
                Urlaubsart.ERHOLUNGSURLAUB,
                null
        );

        String response = mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Status auf GENEHMIGT setzen
        StatusUpdateRequest statusRequest = new StatusUpdateRequest(AntragStatus.GENEHMIGT, "Genehmigt durch Vorgesetzten");

        mockMvc.perform(patch("/api/urlaubsantraege/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENEHMIGT"));
    }
}
