package com.mmr.repository;

import com.mmr.domain.AntragStatus;
import com.mmr.domain.UrlaubsAntrag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UrlaubsAntragRepository extends JpaRepository<UrlaubsAntrag, Long> {

    List<UrlaubsAntrag> findByMitarbeiterId(String mitarbeiterId);

    List<UrlaubsAntrag> findByStatus(AntragStatus status);

    List<UrlaubsAntrag> findByMitarbeiterIdAndStatus(String mitarbeiterId, AntragStatus status);
}

