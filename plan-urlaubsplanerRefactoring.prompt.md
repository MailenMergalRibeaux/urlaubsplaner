# Plan: Urlaubsplaner Refactoring – Lombok, Security & DB-Konfiguration

## Abgeschlossene Arbeiten

### 1. Lombok Integration
**Status:** ✅ **Erledigt**  
**Ziel:** Getter/Setter-Boilerplate durch Lombok-Annotationen ersetzen.

**Umgesetzte Änderungen:**
- `build.gradle.kts`: Lombok als `compileOnly` + `annotationProcessor` hinzugefügt (main + test)
- Domain-Klassen (`Mitarbeiter`, `OeffentlicherFeiertag`, `UrlaubsAntrag`, `Urlaubskonto`, `Bundesland`):
  - `@Getter` + `@Setter` auf Klassenebene
  - `id`, `erstelltAm`, `geaendertAm` mit `@Setter(AccessLevel.NONE)` geschützt (keine Setter für DB-verwaltete Felder)
  - Manuelle Getter/Setter entfernt
  - Fachliche Methode `getVerbleibendeTage()` in `Urlaubskonto` bewusst behalten

**Verifikation:** `./gradlew test` erfolgreich, alle Tests grün.

---

### 2. Constructor-Injection mit Lombok
**Status:** ✅ **Erledigt**  
**Ziel:** Dependency Injection sauberer via `@RequiredArgsConstructor` + final-Felder.

**Umgesetzte Änderungen:**
- 5 Controller + 4 Services:
  - `@RequiredArgsConstructor` ergänzt
  - `final` auf Dependency-Felder gesetzt
  - Manueller Konstruktor entfernt
  - `@Lazy` von Konstruktoren auf einzelne Felder verschoben (z. B. `urlaubskontoService`, `feiertagService`)

**Verifikation:** `./gradlew test` erfolgreich.

---

### 3. Database-Konfiguration (H2 Default, PostgreSQL-Profil)
**Status:** ✅ **Erledigt**  
**Ziel:** App startet lokal ohne externe DB; PostgreSQL per Profil aktivierbar.

**Umgesetzte Änderungen:**

#### `application.yml` (Default)
- **Datasource:** H2 In-Memory mit `DB_CLOSE_ON_EXIT=FALSE` (verhindert Shutdown-Warnungen)
- **Port:** `${SERVER_PORT:8081}` (default 8081, über Env überschreibbar)
- **SQL Init:** `mode: never` (kein Auto-Init im Default-Profil)
- **Hibernate:** `ddl-auto: update`

#### `application-postgresql.yml`
- **Datasource:** PostgreSQL via Env-Vars:
  - `${DB_URL:jdbc:postgresql://localhost:5432/urlaubsplaner}`
  - `${DB_USERNAME:postgres}`
  - `${DB_PASSWORD:postgres}`
- **SQL Init:** `mode: always`, `data-locations: classpath:data-postgresql.sql`

#### `application-docker.yml`
- **Datasource:** PostgreSQL auf Service-Host `db` (Docker Compose)
- **Port:** fest `8080` (Docker-Standard)
- **Logging:** reduziert für Performance

#### `build.gradle.kts`
- H2 als `runtimeOnly` ergänzt (war nur `testRuntimeOnly`)

**Verifikation:**
- Start ohne Profil: H2 startet auf Port 8081 ✅
- Docker Compose neu gestartet, alle Container grün ✅

---

### 4. Security Configuration – HTTP Basic mit `application.yml` Credentials
**Status:** ✅ **Erledigt**  
**Ziel:** Gesicherte Endpoints verwenden Credentials aus `application.yml`, nicht generiertes Passwort.

**Problem vor Fix:**
- Spring generierte Zufallspasswort und ignorierte `spring.security.user.*`
- Resultat: Login mit `application.yml`-Werten (`admin/admin123`) funktionierte nicht

**Umgesetzte Änderung in `SecurityConfig.java`:**
```java
@Bean
UserDetailsManager userDetailsManager(
    @Value("${spring.security.user.name:admin}") String username,
    @Value("${spring.security.user.password:admin123}") String password,
    @Value("${spring.security.user.roles:USER}") String roles
) {
    // Bean liest Properties aus application.yml
    // erstellt InMemory-User mit {noop}-Encoder (Klartext für Dev)
}
```

**Verifikation:**
- Docker Logs zeigen: `userDetailsManager` statt generiertes Passwort ✅
- Test: `GET /api/mitarbeiter`
  - ohne Auth: `401` ✅
  - mit `admin:admin123`: `200` ✅

---

### 5. Port-Konflikt & H2 Shutdown-Warnung
**Status:** ✅ **Erledigt**

**Fixe:**
- Default-Port auf `8081` gesetzt (statt fest `8080`) → Konflikt gelöst
- H2-URL mit `;DB_CLOSE_ON_EXIT=FALSE` erweitert → Shutdown-Warnung weg

---

## Nächste Schritte (Optional)

### A. Security Hardening
- [ ] Kein Default-Passwort in `application.yml` (nur Env-Var akzeptieren)
- [ ] Password-Encoding: `{noop}` → `bcrypt` oder `scrypt`
- [ ] Test-Security-Config (`test/resources/application.yml`) separieren

### B. Git & CI/CD
- [ ] `.gitignore` prüfen/anpassen
- [ ] Initial Commit mit aussagekräftiger Message
- [ ] Git Remote hinzufügen + Push
- [ ] GitHub Actions / GitLab CI für Tests

### C. API Dokumentation
- [ ] OpenAPI/Swagger beschreibungen in Controllers
- [ ] `springdoc-openapi` besser nutzen

### D. Weitere Refactorings
- [ ] Exception Handling vereinheitlichen
- [ ] Validation annotations (`@NotNull`, `@Size`, etc.)
- [ ] Logging mit SLF4J/Logback cleanup
- [ ] Tests auf `@DataJpaTest`, `@WebMvcTest` splitten

---

## Zusammenfassung

| Bereich | Status | Anmerkung |
|---------|--------|-----------|
| Lombok Integration | ✅ Fertig | Getter/Setter + Constructor-Injection |
| DB-Konfiguration | ✅ Fertig | H2-Default, PostgreSQL-Profil, Docker-Profil |
| Security Config | ✅ Fertig | Credentials aus `application.yml` funktionieren |
| Tests | ✅ Grün | Alle Integrationen funktionieren |
| Docker | ✅ Laufen | DB + App + Swagger-UI |

**Nächste Großaufgabe:** Git-Commit, dann ggf. API-Dokumentation oder zusätzliche Tests.

