package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mmr.domain.AntragStatus;
import com.mmr.domain.Bundesland;
import com.mmr.domain.Rolle;
import com.mmr.domain.Urlaubsart;
import com.mmr.dto.*;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "fuehrungskraft@local", roles = "FUEHRUNGSKRAFT")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FeiertagControllerIntegrationTest {

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
    void feiertagAnlegenUndAbrufen() throws Exception {
        FeiertagRequest request = new FeiertagRequest("Tag der Deutschen Einheit",
                LocalDate.of(2026, 10, 3), null);

        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bezeichnung").value("Tag der Deutschen Einheit"))
                .andExpect(jsonPath("$.bundesland").doesNotExist());

        mockMvc.perform(get("/api/feiertage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].datum").value("2026-10-03"));
    }

    @Test
    void feiertagWirdBeimBuchenAbgezogen() throws Exception {
        // Mitarbeiter in NW anlegen
        MitarbeiterRequest maRequest = new MitarbeiterRequest(
                "MA-200", "Klaus", "Klein", "klaus@example.com",
                "geheim12", Rolle.MITARBEITER, Bundesland.NW, null
        );
        mockMvc.perform(post("/api/mitarbeiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maRequest)))
                .andExpect(status().isCreated());

        // Feiertag Fronleichnam 11.06.2026 (NW-spezifisch, aber wir setzen ihn als bundesweit)
        FeiertagRequest feiertagRequest = new FeiertagRequest(
                "Fronleichnam", LocalDate.of(2026, 6, 11), Bundesland.NW);
        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feiertagRequest)))
                .andExpect(status().isCreated());

        // Konto mit 10 Tagen
        UrlaubskontoRequest kontoRequest = new UrlaubskontoRequest("MA-200", 2026, 10);
        mockMvc.perform(post("/api/urlaubskonten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kontoRequest)))
                .andExpect(status().isCreated());

        // Antrag Mo 08.06. bis Fr 12.06.2026 = 5 Werktage, davon 1 Feiertag (Do 11.06.) → 4 buchbare Tage
        UrlaubsAntragRequest antragRequest = new UrlaubsAntragRequest(
                "MA-200", LocalDate.of(2026, 6, 8), LocalDate.of(2026, 6, 12),
                Urlaubsart.ERHOLUNGSURLAUB, null
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
                .andExpect(status().isOk());

        // Konto prüfen: 4 gebuchte Tage (5 Werktage - 1 Feiertag)
        mockMvc.perform(get("/api/urlaubskonten/MA-200/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gebuchteTage").value(4))
                .andExpect(jsonPath("$.verbleibendeTage").value(6));
    }

    @Test
    void zeitraumUndBundeslandLiefertBundesweiteUndRegionaleFeiertage() throws Exception {
        FeiertagRequest bundesweit = new FeiertagRequest("Bundesweit", LocalDate.of(2026, 5, 1), null);
        FeiertagRequest nordrheinWestfalen = new FeiertagRequest("Regional NW", LocalDate.of(2026, 5, 2), Bundesland.NW);
        FeiertagRequest bayern = new FeiertagRequest("Regional BY", LocalDate.of(2026, 5, 3), Bundesland.BY);

        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bundesweit)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nordrheinWestfalen)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/feiertage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bayern)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/feiertage/zeitraum-bundesland")
                        .param("von", "2026-05-01")
                        .param("bis", "2026-05-31")
                        .param("bundesland", "NW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}

