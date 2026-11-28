-- Actualización idempotente: conserva los libros y no inventa su clasificación.
BEGIN;
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL
);
ALTER TABLE libro ADD COLUMN IF NOT EXISTS id_categoria INTEGER
    REFERENCES categoria(id_categoria) ON DELETE RESTRICT;
COMMIT;
