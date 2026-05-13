package com.mmr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "oeffentlicher_feiertag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"datum", "bundesland"}))
@Getter
@Setter
public class OeffentlicherFeiertag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String bezeichnung;

    @Column(nullable = false)
    private LocalDate datum;

    /**
     * null = bundesweiter Feiertag; gesetzt = nur für dieses Bundesland.
     */
    @Enumerated(EnumType.STRING)
    private Bundesland bundesland;

}

