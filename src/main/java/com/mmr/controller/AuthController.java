package com.mmr.controller;

import com.mmr.dto.LoginRequest;
import com.mmr.dto.MitarbeiterResponse;
import com.mmr.dto.RegisterFuehrungskraftRequest;
import com.mmr.exception.MitarbeiterNotFoundException;
import com.mmr.repository.MitarbeiterRepository;
import com.mmr.service.MitarbeiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final MitarbeiterRepository repository;
    private final MitarbeiterService mitarbeiterService;

    @Value("${app.fuehrungskraft.invite-code:}")
    private String konfigurierterInviteCode;

    @PostMapping("/login")
    public MitarbeiterResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.passwort()));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Ungueltige Anmeldedaten");
        }
        return repository.findByEmail(request.email())
                .map(MitarbeiterResponse::from)
                .orElseThrow(() -> new MitarbeiterNotFoundException(request.email()));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MitarbeiterResponse register(@Valid @RequestBody RegisterFuehrungskraftRequest request) {
        if (konfigurierterInviteCode == null || konfigurierterInviteCode.isBlank()) {
            throw new AccessDeniedException("Registrierung ist deaktiviert (kein Invite-Code konfiguriert).");
        }
        if (!konfigurierterInviteCode.equals(request.inviteCode())) {
            throw new AccessDeniedException("Ungueltiger Invite-Code.");
        }
        return mitarbeiterService.registriereFuehrungskraft(request);
    }
}
