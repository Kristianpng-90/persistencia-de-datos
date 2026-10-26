-- Esquema de la base de datos SQLite de la aplicacion Gestion de Estudiantes.
-- Se ejecuta automaticamente al iniciar la aplicacion (clase ConexionBD).
-- "IF NOT EXISTS" garantiza que los datos existentes no se borren en cada inicio.

CREATE TABLE IF NOT EXISTS estudiantes (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    documento       TEXT    NOT NULL UNIQUE,
    nombres         TEXT    NOT NULL,
    apellidos       TEXT    NOT NULL,
    correo          TEXT    NOT NULL UNIQUE,
    programa        TEXT    NOT NULL,
    semestre        INTEGER NOT NULL CHECK (semestre BETWEEN 1 AND 10),
    fecha_registro  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE INDEX IF NOT EXISTS idx_estudiantes_apellidos ON estudiantes (apellidos);
