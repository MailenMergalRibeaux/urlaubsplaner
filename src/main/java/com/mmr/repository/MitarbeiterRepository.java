package com.mmr.repository;

import com.mmr.domain.Mitarbeiter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MitarbeiterRepository extends JpaRepository<Mitarbeiter, String> {

    Optional<Mitarbeiter> findByEmail(String email);

    List<Mitarbeiter> findByVorgesetzterMitarbeiterId(String vorgesetzterMitarbeiterId);
}

