package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mmr.domain.AntragStatus;
import com.mmr.domain.Bundesland;
import com.mmr.domain.Urlaubsart;
import com.mmr.dto.FeiertagRequest;
import com.mmr.dto.MitarbeiterRequest;
import com.mmr.dto.StatusUpdateRequest;
import com.mmr.dto.UrlaubsAntragRequest;
import com.mmr.dto.UrlaubskontoRequest;
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
class UrlaubskontoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void kontoAnlegenUndAbrufen() throws Exception {
        UrlaubskontoRequest request = new UrlaubskontoRequest("MA-010", 2026, 30);

        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mitarbeiterId").value("MA-010"))
                .andExpect(jsonPath("$.gesamtTage").value(30))
                .andExpect(jsonPath("$.gebuchteTage").value(0))
                .andExpect(jsonPath("$.verbleibendeTage").value(30));

        mockMvc.perform(get("/api/urlaubskonten/MA-010/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jahr").value(2026));
    }

    @Test
    void doppeltesKontoWirdAbgelehnt() throws Exception {
        UrlaubskontoRequest request = new UrlaubskontoRequest("MA-011", 2026, 30);

        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void genehmigungBuchtTage() throws Exception {
        // Konto anlegen
        UrlaubskontoRequest kontoRequest = new UrlaubskontoRequest("MA-020", 2026, 30);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        // Antrag erstellen: Mo 2026-07-06 bis Fr 2026-07-10 = 5 Arbeitstage
        UrlaubsAntragRequest antragRequest = new UrlaubsAntragRequest(
                "MA-020",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                Urlaubsart.ERHOLUNGSURLAUB,
                null
        );

        String antragJson = mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(antragRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long antragId = objectMapper.readTree(antragJson).get("id").asLong();

        // Genehmigen
        StatusUpdateRequest statusRequest = new StatusUpdateRequest(AntragStatus.GENEHMIGT, null);
        mockMvc.perform(patch("/api/urlaubsantraege/" + antragId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENEHMIGT"));

        // Konto prüfen: 5 Tage gebucht
        mockMvc.perform(get("/api/urlaubskonten/MA-020/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gebuchteTage").value(5))
                .andExpect(jsonPath("$.verbleibendeTage").value(25));
    }

    @Test
    void nichtGenugTageWirft422() throws Exception {
        // Konto mit nur 2 Tagen anlegen
        UrlaubskontoRequest kontoRequest = new UrlaubskontoRequest("MA-030", 2026, 2);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        // Antrag für 5 Arbeitstage
        UrlaubsAntragRequest antragRequest = new UrlaubsAntragRequest(
                "MA-030",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                Urlaubsart.ERHOLUNGSURLAUB,
                null
        );

        String antragJson = mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(antragRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long antragId = objectMapper.readTree(antragJson).get("id").asLong();

        // Genehmigen muss fehlschlagen
        StatusUpdateRequest statusRequest = new StatusUpdateRequest(AntragStatus.GENEHMIGT, null);
        mockMvc.perform(patch("/api/urlaubsantraege/" + antragId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void stornierungGibtGebuchteTageWiederFrei() throws Exception {
        UrlaubskontoRequest kontoRequest = new UrlaubskontoRequest("MA-021", 2026, 30);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        UrlaubsAntragRequest antragRequest = new UrlaubsAntragRequest(
                "MA-021",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                Urlaubsart.ERHOLUNGSURLAUB,
                null
        );

        String antragJson = mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(antragRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long antragId = objectMapper.readTree(antragJson).get("id").asLong();

        mockMvc.perform(patch("/api/urlaubsantraege/" + antragId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusUpdateRequest(AntragStatus.GENEHMIGT, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENEHMIGT"));

        mockMvc.perform(patch("/api/urlaubsantraege/" + antragId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusUpdateRequest(AntragStatus.STORNIERT, "Urlaub entfällt"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STORNIERT"));

        mockMvc.perform(get("/api/urlaubskonten/MA-021/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gebuchteTage").value(0))
                .andExpect(jsonPath("$.verbleibendeTage").value(30));
    }

    @Test
    void feiertagWirdVonArbeitstagenAbgezogen() throws Exception {
        MitarbeiterRequest mitarbeiterRequest = new MitarbeiterRequest(
                "MA-040",
                "Mia",
                "Muster",
                "mia.muster@example.org",
                Bundesland.NW,
                null
        );
        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mitarbeiterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("MA-040"));

        FeiertagRequest feiertagRequest = new FeiertagRequest(
                "Regionalfeiertag",
                LocalDate.of(2026, 7, 8),
                Bundesland.NW
        );
        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feiertagRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datum").value("2026-07-08"));

        UrlaubskontoRequest kontoRequest = new UrlaubskontoRequest("MA-040", 2026, 30);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        UrlaubsAntragRequest antragRequest = new UrlaubsAntragRequest(
                "MA-040",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 10),
                Urlaubsart.ERHOLUNGSURLAUB,
                null
        );

        String antragJson = mockMvc.perform(post("/api/urlaubsantraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(antragRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long antragId = objectMapper.readTree(antragJson).get("id").asLong();

        mockMvc.perform(patch("/api/urlaubsantraege/" + antragId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StatusUpdateRequest(AntragStatus.GENEHMIGT, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GENEHMIGT"));

        mockMvc.perform(get("/api/urlaubskonten/MA-040/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gebuchteTage").value(4))
                .andExpect(jsonPath("$.verbleibendeTage").value(26));
    }
}

