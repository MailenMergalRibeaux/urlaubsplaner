package com.mmr.repository;

import com.mmr.domain.Urlaubskonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlaubskontoRepository extends JpaRepository<Urlaubskonto, Long> {

    Optional<Urlaubskonto> findByMitarbeiterIdAndJahr(String mitarbeiterId, int jahr);

    List<Urlaubskonto> findByMitarbeiterId(String mitarbeiterId);
}

