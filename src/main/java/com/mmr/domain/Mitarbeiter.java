package com.mmr.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mitarbeiter")
@Getter
@Setter
public class Mitarbeiter {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String vorname;

    @NotBlank
    @Column(nullable = false)
    private String nachname;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Bundesland bundesland;

    /** Optional: ID des Vorgesetzten (selbstreferenzierend) */
    private String vorgesetzterMitarbeiterId;

}

