package com.mmr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "urlaubskonto",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mitarbeiterId", "jahr"}))
@Getter
@Setter
public class Urlaubskonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String mitarbeiterId;

    @Column(nullable = false)
    private int jahr;

    @Column(nullable = false)
    private int gesamtTage;

    @Column(nullable = false)
    private int gebuchteTage;

    public int getVerbleibendeTage() {
        return gesamtTage - gebuchteTage;
    }

}

