# Urlaubsplaner (Spring Boot, Java 21)

[![CI](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/ci.yml)
[![Docker](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/docker.yml/badge.svg)](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/docker.yml)
[![Release](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/release.yml/badge.svg)](https://github.com/YOUR_ORG/urlaubsplaner/actions/workflows/release.yml)

Lokales Grundgeruest fuer eine Urlaubsplaner-API mit Spring Boot, JPA, Security und PostgreSQL.

## Voraussetzungen

- Java 21
- PostgreSQL (lokal)

## Lokale DB-Defaults

Die Standardwerte stehen in `src/main/resources/application.yml`:

- URL: `jdbc:postgresql://localhost:5432/urlaubsplaner`
- Benutzer: `postgres`
- Passwort: `postgres`
- Zeitzone: `Europe/Berlin`

Beim Start werden Seed-Daten aus `src/main/resources/data.sql` geladen.

## Starten

```bash
./gradlew bootRun
```

## Erster Endpoint

- `GET /api/health` liefert den Status der Anwendung.

Beispielantwort:

```json
{
  "status": "UP",
  "service": "urlaubsplaner",
  "timestamp": "2026-05-12T23:00:00+02:00"
}
```

## Authentifizierung und Rollen

Alle Endpoints ausser `GET /api/health`, `POST /api/auth/login`, `POST /api/auth/register`
und der Swagger/OpenAPI-Pfade erfordern eine Anmeldung.

- Anmeldung erfolgt ueber **HTTP Basic** mit **E-Mail + Passwort**.
- Passwoerter sind in der `mitarbeiter`-Tabelle als BCrypt-Hash abgelegt.
- Es existiert **kein statischer Default-Benutzer** in der Konfiguration.

### Rollen

| Rolle            | Bedeutung                                                                  |
| ---------------- | -------------------------------------------------------------------------- |
| `MITARBEITER`    | Darf eigene Antraege stellen und lesende Endpoints aufrufen.               |
| `FUEHRUNGSKRAFT` | Darf zusaetzlich Mitarbeiter anlegen, aktualisieren und loeschen.          |

### Login

`POST /api/auth/login` prueft Credentials und liefert das Profil zurueck.

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alex@example.com","passwort":"Geheim1234"}'
```

Antwort bei Erfolg: `200` mit `MitarbeiterResponse` (ohne Passwort/Hash).
Bei falschem Passwort oder unbekannter E-Mail: `401`.

Fuer Folge-Requests muss der Client den Basic-Auth-Header mitsenden, z.B.
`curl -u 'alex@example.com:Geheim1234' http://localhost:8081/api/urlaubsantraege`.

### Registrierung von Fuehrungskraeften

`POST /api/auth/register` legt einen neuen `FUEHRUNGSKRAFT`-Account an.
Der Endpoint ist anonym erreichbar, erwartet aber einen **Invite-Code** im Body.
Der Server vergleicht ihn gegen die Property `app.fuehrungskraft.invite-code`
(Env-Variable `APP_FUEHRUNGSKRAFT_INVITE_CODE`).

Ohne konfigurierten Code ist die Registrierung deaktiviert (`403`).

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "id":"FK-100","vorname":"Alex","nachname":"Anon",
    "email":"alex@example.com","passwort":"Geheim1234",
    "bundesland":"NW","inviteCode":"<dein-invite-code>"
  }'
```

Die Rolle wird serverseitig hartkodiert auf `FUEHRUNGSKRAFT` gesetzt — der Aufrufer kann sie nicht ueber das Request-Body steuern.

### Mitarbeiter anlegen

`POST /api/mitarbeiter` legt einen Account mit Rolle `MITARBEITER` an. Es ist erforderlich:

- **Authentifizierung als `FUEHRUNGSKRAFT`** (sonst `403`).
- **Rolle im Request = `MITARBEITER`** (anderer Wert -> `400`; Fuehrungskraefte registrieren sich ueber `/api/auth/register`).
- Mindestpasswortlaenge **8 Zeichen**.

```bash
curl -u 'alex@example.com:Geheim1234' \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8081/api/mitarbeiter \
  -d '{
    "id":"MA-100","vorname":"Mira","nachname":"Mit",
    "email":"mira@example.com","passwort":"Mira12345",
    "rolle":"MITARBEITER","bundesland":"NW"
  }'
```

### Initiale Fuehrungskraft (Bootstrap)

Damit die App in einer leeren DB nutzbar ist, legt der `FuehrungskraftInitializer` beim Start eine
initiale Fuehrungskraft an — aber **nur**, wenn die Tabelle leer ist *und* ein Passwort konfiguriert ist:

| Property                          | Env-Variable                       | Default                  |
| --------------------------------- | ---------------------------------- | ------------------------ |
| `app.fuehrungskraft.id`           | `APP_FUEHRUNGSKRAFT_ID`            | `FK-001`                 |
| `app.fuehrungskraft.email`        | `APP_FUEHRUNGSKRAFT_EMAIL`         | `fuehrungskraft@local`   |
| `app.fuehrungskraft.password`     | `APP_FUEHRUNGSKRAFT_PASSWORD`      | *(leer = kein Bootstrap)* |
| `app.fuehrungskraft.invite-code`  | `APP_FUEHRUNGSKRAFT_INVITE_CODE`   | *(leer = Register aus)*  |

Empfohlener Start (lokal):

```bash
export APP_FUEHRUNGSKRAFT_PASSWORD="<sicheres-passwort>"
export APP_FUEHRUNGSKRAFT_INVITE_CODE="<langer-zufallsstring>"
./gradlew bootRun
```

In PowerShell:

```powershell
$env:APP_FUEHRUNGSKRAFT_PASSWORD = "<sicheres-passwort>"
$env:APP_FUEHRUNGSKRAFT_INVITE_CODE = "<langer-zufallsstring>"
.\gradlew bootRun
```

## Paketstruktur

- `com.mmr.config` fuer Security-Konfiguration
- `com.mmr.controller` fuer REST-Controller
- `com.mmr.service` fuer Anwendungslogik
- `com.mmr.dto` fuer API-Antworttypen

## Feiertags-Seed 2026

- In `src/main/resources/data.sql` sind bundesweite gesetzliche Feiertage fuer 2026 hinterlegt.
- Diese Daten werden beim App-Start automatisch eingespielt (`spring.sql.init.mode=always`).

## PostgreSQL-Seed (idempotent)

- Fuer produktionsnaehere Setups gibt es `src/main/resources/data-postgresql.sql`.
- Das Script nutzt `ON CONFLICT (id) DO UPDATE` und ist damit idempotent.
- Enthalten sind bundesweite und bundeslandspezifische Feiertage fuer 2026.
- Aktivierung ueber Profil `postgresql` (siehe `src/main/resources/application-postgresql.yml`).

Start mit PostgreSQL-Profil:

```bash
./gradlew bootRun --args='--spring.profiles.active=postgresql'
```

## Testen

```bash
./gradlew test
```

## Docker Compose (App + PostgreSQL)

Die Dateien `Dockerfile`, `docker-compose.yml` und `.dockerignore` sind fuer den Container-Start enthalten.

Starten:

```bash
docker compose up --build
```

Stoppen (inkl. Netzwerk, ohne Datenverlust im Volume):

```bash
docker compose down
```

Neu starten mit leerer DB (Volume loeschen):

```bash
docker compose down -v
docker compose up --build
```

Die API ist danach unter `http://localhost:58080` erreichbar.

Die Swagger UI ist unter `http://localhost:58081` erreichbar.

Direkt zum OpenAPI-JSON: `http://localhost:58080/v3/api-docs`.

PostgreSQL ist von Host-Seite unter `localhost:55432` erreichbar (Container-Port bleibt `5432`).

## Docker Compose Override (Dev)

`docker-compose.override.yml` wird von `docker compose` automatisch mitgeladen.

Aktuell setzt es zusaetzlich fuer die App:

- `SPRING_JPA_SHOW_SQL=true`
- `LOGGING_LEVEL_ORG_HIBERNATE_SQL=DEBUG`
- `LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND=TRACE`

## Docker Task-Skript

Fuer wiederkehrende Befehle gibt es `scripts/compose.sh`.

```bash
./scripts/compose.sh up
./scripts/compose.sh up-db
./scripts/compose.sh logs
./scripts/compose.sh logs-app
./scripts/compose.sh logs-db
./scripts/compose.sh status
./scripts/compose.sh health
./scripts/compose.sh open
./scripts/compose.sh swagger
./scripts/compose.sh swagger-docs
./scripts/compose.sh restart app
./scripts/compose.sh restart db
./scripts/compose.sh down
./scripts/compose.sh reset
```

Hilfe anzeigen:

```bash
./scripts/compose.sh help
```

## Docker Task-Skript (PowerShell)

Alternativ kannst du dieselben Befehle nativ in PowerShell nutzen:

```powershell
.\scripts\compose.ps1 up
.\scripts\compose.ps1 up-db
.\scripts\compose.ps1 logs
.\scripts\compose.ps1 logs-app
.\scripts\compose.ps1 logs-db
.\scripts\compose.ps1 status
.\scripts\compose.ps1 health
.\scripts\compose.ps1 open
.\scripts\compose.ps1 swagger
.\scripts\compose.ps1 swagger-docs
.\scripts\compose.ps1 restart app
.\scripts\compose.ps1 restart db
.\scripts\compose.ps1 down
.\scripts\compose.ps1 reset
```

Konfiguration pruefen:

```powershell
.\scripts\compose.ps1 config
```

## CI/CD (GitHub Actions)

Drei Workflows sind unter `.github/workflows/` definiert:

| Workflow | Trigger | Aufgabe |
|---|---|---|
| `ci.yml` | Push/PR auf `main`, `develop`, `feature/**` | Build + Tests + Artefakte |
| `docker.yml` | Push auf `main`/`develop` nach CI | Docker-Image → GHCR |
| `release.yml` | Tag `v*.*.*` | Release-Image + GitHub Release |

### Erster Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

Das Docker-Image wird danach unter folgendem Namen verfuegbar sein:

```
ghcr.io/YOUR_ORG/urlaubsplaner:v1.0.0
ghcr.io/YOUR_ORG/urlaubsplaner:latest
```

### GitHub-Pakete sichtbar machen

Das Image wird automatisch als Package am Repository veroeffentlicht.  
Sichtbarkeit: **Settings → Packages → Change visibility → Public**.

> Ersetze `YOUR_ORG` in `README.md` und den Badges durch deinen GitHub-Nutzernamen oder deine Organisation.

=======
# urlaubsplaner
>>>>>>> 10d365d5e9d13716a35b405ffab5b877337236b8
