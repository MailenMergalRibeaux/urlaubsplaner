package com.mmr.service;

import com.mmr.domain.Mitarbeiter;
import com.mmr.repository.MitarbeiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MitarbeiterUserDetailsService implements UserDetailsService {

    /**
     * Spezielle Rolle fuer Konten, deren Initial-Passwort noch nicht gewechselt wurde.
     * Solche Konten duerfen ausschliesslich /api/auth/change-password aufrufen.
     */
    public static final String ROLE_PASSWORT_AENDERUNG_ERFORDERLICH = "PASSWORT_AENDERUNG_ERFORDERLICH";

    private final MitarbeiterRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Mitarbeiter m = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Mitarbeiter mit E-Mail '" + email + "' nicht gefunden."));
        String role = m.isPasswortAenderungErforderlich()
                ? ROLE_PASSWORT_AENDERUNG_ERFORDERLICH
                : m.getRolle().name();
        return User.withUsername(m.getEmail())
                .password(m.getPasswortHash())
                .roles(role)
                .build();
    }
}
