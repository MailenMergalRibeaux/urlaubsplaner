package com.mmr.repository;

import com.mmr.domain.Bundesland;
import com.mmr.domain.OeffentlicherFeiertag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OeffentlicherFeiertagRepository extends JpaRepository<OeffentlicherFeiertag, Long> {

    /**
     * Alle Feiertage die für ein Bundesland gelten:
     * bundesweite (bundesland IS NULL) + länderspezifische.
     */
    @Query("""
            SELECT f FROM OeffentlicherFeiertag f
            WHERE f.datum BETWEEN :von AND :bis
              AND (f.bundesland IS NULL OR f.bundesland = :bundesland)
            """)
    List<OeffentlicherFeiertag> findByZeitraumUndBundesland(
            @Param("von") LocalDate von,
            @Param("bis") LocalDate bis,
            @Param("bundesland") Bundesland bundesland);

    List<OeffentlicherFeiertag> findByDatumBetweenAndBundeslandIsNull(LocalDate von, LocalDate bis);
}

