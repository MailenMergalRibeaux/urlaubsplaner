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

    private final MitarbeiterRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Mitarbeiter m = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Mitarbeiter mit E-Mail '" + email + "' nicht gefunden."));
        return User.withUsername(m.getEmail())
                .password(m.getPasswortHash())
                .roles(m.getRolle().name())
                .build();
    }
}
