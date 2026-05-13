package com.mmr.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "urlaubsantrag")
@Getter
@Setter
public class UrlaubsAntrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String mitarbeiterId;

    @NotNull
    @Column(nullable = false)
    private LocalDate startdatum;

    @NotNull
    @Column(nullable = false)
    private LocalDate enddatum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Urlaubsart urlaubsart = Urlaubsart.ERHOLUNGSURLAUB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AntragStatus status = AntragStatus.BEANTRAGT;

    private String kommentar;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime erstelltAm;

    @Column(nullable = false)
    @Setter(AccessLevel.NONE)
    private LocalDateTime geaendertAm;

    @PrePersist
    void prePersist() {
        erstelltAm = LocalDateTime.now();
        geaendertAm = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        geaendertAm = LocalDateTime.now();
    }

}

