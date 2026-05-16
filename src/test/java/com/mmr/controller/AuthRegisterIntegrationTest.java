package com.mmr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmr.domain.Bundesland;
import com.mmr.dto.RegisterFuehrungskraftRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.fuehrungskraft.invite-code=TEST-INVITE-9999")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthRegisterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void register_mitKorrektemInviteCode_legtFuehrungskraftAn() throws Exception {
        RegisterFuehrungskraftRequest req = new RegisterFuehrungskraftRequest(
                "FK-REG-1", "Frieda", "Fuehrungskraft", "frieda@example.com",
                "Geheim1234", Bundesland.NW, null, "TEST-INVITE-9999"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("FK-REG-1"))
                .andExpect(jsonPath("$.rolle").value("FUEHRUNGSKRAFT"))
                .andExpect(jsonPath("$.passwort").doesNotExist())
                .andExpect(jsonPath("$.passwortHash").doesNotExist());
    }

    @Test
    void register_mitFalschemInviteCode_gibtForbidden() throws Exception {
        RegisterFuehrungskraftRequest req = new RegisterFuehrungskraftRequest(
                "FK-REG-2", "Frank", "Falsch", "frank@example.com",
                "Geheim1234", Bundesland.BY, null, "WRONG-CODE"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_ohneInviteCode_gibtBadRequest() throws Exception {
        String body = """
                {
                  "id":"FK-REG-3","vorname":"Otto","nachname":"Ohne",
                  "email":"otto@example.com","passwort":"Geheim1234",
                  "bundesland":"NW"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_mitZuKurzemPasswort_gibtBadRequest() throws Exception {
        RegisterFuehrungskraftRequest req = new RegisterFuehrungskraftRequest(
                "FK-REG-4", "Karla", "Kurz", "karla@example.com",
                "kurz", Bundesland.NW, null, "TEST-INVITE-9999"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_doppelteEmail_gibtConflict() throws Exception {
        RegisterFuehrungskraftRequest first = new RegisterFuehrungskraftRequest(
                "FK-REG-5", "Erste", "FK", "doppel@example.com",
                "Geheim1234", Bundesland.NW, null, "TEST-INVITE-9999"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        RegisterFuehrungskraftRequest second = new RegisterFuehrungskraftRequest(
                "FK-REG-6", "Zweite", "FK", "doppel@example.com",
                "Geheim1234", Bundesland.NW, null, "TEST-INVITE-9999"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }
}
