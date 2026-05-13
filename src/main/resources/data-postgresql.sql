-- PostgreSQL Seed-Daten fuer 2026 (idempotent via ON CONFLICT)
-- Verwendung mit Profil "postgresql" (siehe application-postgresql.yml)

INSERT INTO oeffentlicher_feiertag (id, bezeichnung, datum, bundesland) VALUES
    (1001, 'Neujahr', '2026-01-01', NULL),
    (1002, 'Karfreitag', '2026-04-03', NULL),
    (1003, 'Ostermontag', '2026-04-06', NULL),
    (1004, 'Tag der Arbeit', '2026-05-01', NULL),
    (1005, 'Christi Himmelfahrt', '2026-05-14', NULL),
    (1006, 'Pfingstmontag', '2026-05-25', NULL),
    (1007, 'Tag der Deutschen Einheit', '2026-10-03', NULL),
    (1008, '1. Weihnachtstag', '2026-12-25', NULL),
    (1009, '2. Weihnachtstag', '2026-12-26', NULL),

    -- Laenderspezifische Feiertage 2026
    (1101, 'Heilige Drei Koenige', '2026-01-06', 'BA'),
    (1102, 'Heilige Drei Koenige', '2026-01-06', 'BY'),
    (1103, 'Heilige Drei Koenige', '2026-01-06', 'ST'),

    (1111, 'Fronleichnam', '2026-06-04', 'BA'),
    (1112, 'Fronleichnam', '2026-06-04', 'BY'),
    (1113, 'Fronleichnam', '2026-06-04', 'HE'),
    (1114, 'Fronleichnam', '2026-06-04', 'NW'),
    (1115, 'Fronleichnam', '2026-06-04', 'RP'),
    (1116, 'Fronleichnam', '2026-06-04', 'SL'),

    (1121, 'Mariae Himmelfahrt', '2026-08-15', 'BY'),
    (1122, 'Mariae Himmelfahrt', '2026-08-15', 'SL'),

    (1131, 'Weltkindertag', '2026-09-20', 'TH'),

    (1141, 'Reformationstag', '2026-10-31', 'BB'),
    (1142, 'Reformationstag', '2026-10-31', 'HB'),
    (1143, 'Reformationstag', '2026-10-31', 'HH'),
    (1144, 'Reformationstag', '2026-10-31', 'MV'),
    (1145, 'Reformationstag', '2026-10-31', 'NI'),
    (1146, 'Reformationstag', '2026-10-31', 'SN'),
    (1147, 'Reformationstag', '2026-10-31', 'ST'),
    (1148, 'Reformationstag', '2026-10-31', 'SH'),
    (1149, 'Reformationstag', '2026-10-31', 'TH'),

    (1151, 'Allerheiligen', '2026-11-01', 'BA'),
    (1152, 'Allerheiligen', '2026-11-01', 'BY'),
    (1153, 'Allerheiligen', '2026-11-01', 'NW'),
    (1154, 'Allerheiligen', '2026-11-01', 'RP'),
    (1155, 'Allerheiligen', '2026-11-01', 'SL'),

    (1161, 'Buss- und Bettag', '2026-11-18', 'SN')
ON CONFLICT (id) DO UPDATE SET
    bezeichnung = EXCLUDED.bezeichnung,
    datum = EXCLUDED.datum,
    bundesland = EXCLUDED.bundesland;

