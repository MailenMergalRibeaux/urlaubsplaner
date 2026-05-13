package com.mmr.domain;

import lombok.Getter;

@Getter
public enum Bundesland {
    BA("Baden-Württemberg"),
    BY("Bayern"),
    BE("Berlin"),
    BB("Brandenburg"),
    HB("Bremen"),
    HH("Hamburg"),
    HE("Hessen"),
    MV("Mecklenburg-Vorpommern"),
    NI("Niedersachsen"),
    NW("Nordrhein-Westfalen"),
    RP("Rheinland-Pfalz"),
    SL("Saarland"),
    SN("Sachsen"),
    ST("Sachsen-Anhalt"),
    SH("Schleswig-Holstein"),
    TH("Thüringen");

    private final String bezeichnung;

    Bundesland(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

}

