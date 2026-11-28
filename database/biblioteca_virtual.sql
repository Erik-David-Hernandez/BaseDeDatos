CREATE DATABASE "Biblioteca_virtual";
\c "Biblioteca_virtual"

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    pswd VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL DEFAULT 'static_salt_pr3',
    rol VARCHAR(20) NOT NULL DEFAULT 'lector' CHECK (rol IN ('administrador', 'bibliotecario', 'lector')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS genero (
    id_genero SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS autor (
    id_autor SERIAL PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellido_p VARCHAR(50) NOT NULL,
    apellido_m VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS libro (
    id_libro SERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    codigo_seccion VARCHAR(20) NOT NULL,
    n_ejemplares INTEGER NOT NULL DEFAULT 0 CHECK (n_ejemplares >= 0),
    id_genero INTEGER NOT NULL,
    id_autor INTEGER NOT NULL,
    id_categoria INTEGER NULL,
    CONSTRAINT fk_libro_genero FOREIGN KEY (id_genero) REFERENCES genero(id_genero) ON DELETE RESTRICT,
    CONSTRAINT fk_libro_autor FOREIGN KEY (id_autor) REFERENCES autor(id_autor) ON DELETE RESTRICT,
    CONSTRAINT fk_libro_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT
);

-- Soporta 'reservado', 'prestado', 'devuelto', 'vencido', 'cancelado'
CREATE TABLE IF NOT EXISTS prestamo (
    id_reserva SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    id_libro INTEGER NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_fin TIMESTAMP NOT NULL,
    fecha_devolucion_real TIMESTAMP NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'reservado'
        CHECK (estado IN ('reservado', 'prestado', 'devuelto', 'vencido', 'cancelado')),
    CONSTRAINT chk_prestamo_fechas CHECK (fecha_inicio < fecha_fin),
    CONSTRAINT fk_prestamo_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro) REFERENCES libro(id_libro) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria SERIAL PRIMARY KEY,
    id_usuario INTEGER NULL,
    accion VARCHAR(100) NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    detalle TEXT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS notificacion_correo (
    id_notificacion SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    asunto VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    enviado BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- ============================================================================
-- ÍNDICES 
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_libro_id_genero ON libro(id_genero);
CREATE INDEX IF NOT EXISTS idx_libro_id_autor ON libro(id_autor);
CREATE INDEX IF NOT EXISTS idx_prestamo_id_usuario ON prestamo(id_usuario);
CREATE INDEX IF NOT EXISTS idx_prestamo_id_libro ON prestamo(id_libro);

CREATE INDEX IF NOT EXISTS idx_autor_nombres ON autor(nombres);
CREATE INDEX IF NOT EXISTS idx_autor_apellido_p ON autor(apellido_p);
CREATE INDEX IF NOT EXISTS idx_autor_apellido_m ON autor(apellido_m);
CREATE INDEX IF NOT EXISTS idx_autor_busqueda_texto ON autor USING gin (
    to_tsvector('spanish', nombres || ' ' || apellido_p || ' ' || COALESCE(apellido_m, ''))
);

CREATE INDEX IF NOT EXISTS idx_libro_titulo ON libro(titulo);
CREATE INDEX IF NOT EXISTS idx_libro_codigo_seccion ON libro(codigo_seccion);
CREATE INDEX IF NOT EXISTS idx_libro_disponibles ON libro(id_libro, titulo) WHERE n_ejemplares > 0;
CREATE INDEX IF NOT EXISTS idx_libro_genero_titulo ON libro(id_genero, titulo);
CREATE INDEX IF NOT EXISTS idx_libro_titulo_trgm ON libro USING gin(titulo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_prestamo_rango_fechas ON prestamo(fecha_inicio, fecha_fin);
CREATE INDEX IF NOT EXISTS idx_prestamo_activos ON prestamo(id_usuario, id_libro) WHERE estado IN ('prestado', 'reservado');
CREATE INDEX IF NOT EXISTS idx_prestamo_vencimiento ON prestamo(fecha_fin) WHERE estado = 'prestado';
CREATE INDEX IF NOT EXISTS idx_prestamo_usuario_cubrimiento ON prestamo(id_usuario) INCLUDE (id_libro, fecha_fin, estado);
CREATE INDEX IF NOT EXISTS idx_usuario_email_lower ON usuario(LOWER(email));

-- ============================================================================
-- TRIGGER PARA CONTROL DE INVENTARIO AUTOMÁTICO
-- ============================================================================
CREATE OR REPLACE FUNCTION fn_actualizar_inventario_prestamo()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        IF NEW.estado IN ('reservado', 'prestado') THEN
            UPDATE libro SET n_ejemplares = n_ejemplares - 1 WHERE id_libro = NEW.id_libro;
        END IF;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.estado IN ('reservado', 'prestado') AND NEW.estado IN ('devuelto', 'cancelado') THEN
            UPDATE libro SET n_ejemplares = n_ejemplares + 1 WHERE id_libro = NEW.id_libro;
        ELSIF OLD.estado IN ('devuelto', 'cancelado') AND NEW.estado IN ('reservado', 'prestado') THEN
            UPDATE libro SET n_ejemplares = n_ejemplares - 1 WHERE id_libro = NEW.id_libro;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_actualizar_inventario ON prestamo;
CREATE TRIGGER trg_actualizar_inventario
AFTER INSERT OR UPDATE OF estado ON prestamo
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_inventario_prestamo();

-- ============================================================================
-- DATOS INICIALES 
-- ============================================================================
INSERT INTO genero (nombre) VALUES
('Ficción Realista'), ('Ciencia Ficción'), ('Fantasía'), ('Novela Negra y Policial'),
('Terror y Horror'), ('Romance'), ('Historia'), ('Biografía y Memorias'),
('Ensayo y Filosofía'), ('Poesía'), ('Desarrollo Personal'), ('Ciencia y Tecnología'),
('Cómics y Novela Gráfica'), ('Literatura Infantil y Juvenil'), ('Clásicos Universales')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO autor (nombres, apellido_p, apellido_m) VALUES
('Gabriel', 'García', 'Márquez'), ('Isabel', 'Allende', NULL), ('Mario', 'Vargas', 'Llosa'),
('Jorge Luis', 'Borges', NULL), ('Julio', 'Cortázar', NULL), ('Octavio', 'Paz', 'Lozano'),
('Carlos', 'Fuentes', 'Macías'), ('Laura', 'Esquivel', 'Valdés'), ('Juan', 'Rulfo', 'Vizcaíno'),
('Stephen', 'King', NULL), ('Agatha', 'Christie', NULL), ('Isaac', 'Asimov', NULL),
('George', 'Orwell', NULL), ('Jane', 'Austen', NULL), ('J.K.', 'Rowling', NULL);

-- Hash SHA-256 con salt 'salt123' para password '123456':
-- SHA256("salt123123456") = 95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8
INSERT INTO usuario (nombre, email, pswd, salt, rol, activo) VALUES
('Carlos Gómez', 'carlos.gomez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'administrador', TRUE),
('Elena Morales', 'elena.morales@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'bibliotecario', TRUE),
('Juan Pérez', 'juan.perez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('María Rodríguez', 'maria.rodriguez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Ana Martínez', 'ana.martinez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Luis Hernández', 'luis.hernandez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Sofía López', 'sofia.lopez@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Diego Torres', 'diego.torres@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', FALSE),
('Laura Díaz', 'laura.diaz@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Fernando Ruiz', 'fernando.ruiz@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Javier Castro', 'javier.castro@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Patricia Ortiz', 'patricia.ortiz@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Alejandro Gutiérrez', 'alejandro.g@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Camila Navarro', 'camila.navarro@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', TRUE),
('Roberto Ramos', 'roberto.ramos@email.com', '95f533f8dabec3c236fdb030957824dc1f36efb4fe6428a9f2d79f0d243075f8', 'salt123', 'lector', FALSE)
ON CONFLICT (email) DO NOTHING;

-- Formatos de demostración; pueden compartirse entre libros.
INSERT INTO categoria (nombre) VALUES
('Impreso'), ('Digital'), ('Audiolibro')
ON CONFLICT (nombre) DO NOTHING;

-- Asignaciones aleatorias fijadas para que los datos iniciales sean reproducibles.
INSERT INTO libro (titulo, isbn, codigo_seccion, n_ejemplares, id_genero, id_autor, id_categoria) VALUES
('Cien años de soledad', '978-0307474728', 'SEC-FIC-001', 5, 1, 1, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('La casa de los espíritus', '978-0525433477', 'SEC-FIC-002', 3, 1, 2, (SELECT id_categoria FROM categoria WHERE nombre = 'Audiolibro')),
('La ciudad y los perros', '978-8420471830', 'SEC-FIC-003', 2, 1, 3, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('Ficciones', '978-8497593373', 'SEC-ENS-001', 4, 9, 4, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('Rayuela', '978-8437604572', 'SEC-FIC-004', 3, 1, 5, (SELECT id_categoria FROM categoria WHERE nombre = 'Audiolibro')),
('El laberinto de la soledad', '978-9681603014', 'SEC-ENS-002', 2, 9, 6, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('La muerte de Artemio Cruz', '978-8437600871', 'SEC-FIC-005', 1, 1, 7, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('Como agua para chocolate', '978-0385720168', 'SEC-ROM-001', 6, 6, 8, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('Pedro Páramo', '978-8437604183', 'SEC-FIC-006', 4, 1, 9, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('El resplandor', '978-0307743657', 'SEC-TER-001', 3, 5, 10, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('Asesinato en el Orient Express', '978-0062073501', 'SEC-POL-001', 5, 4, 11, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('Fundación', '978-0553293357', 'SEC-CFI-001', 4, 2, 12, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('1984', '978-0451524935', 'SEC-CFI-002', 7, 2, 13, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso')),
('Orgullo y prejuicio', '978-0141439518', 'SEC-ROM-002', 3, 6, 14, (SELECT id_categoria FROM categoria WHERE nombre = 'Digital')),
('Harry Potter y la piedra filosofal', '978-8478888566', 'SEC-INF-001', 8, 14, 15, (SELECT id_categoria FROM categoria WHERE nombre = 'Impreso'))
ON CONFLICT (isbn) DO NOTHING;

INSERT INTO prestamo (id_usuario, id_libro, fecha_inicio, fecha_fin, estado) VALUES
(3, 1, '2026-08-01 10:00:00', '2026-08-15 10:00:00', 'devuelto'),
(4, 3, '2026-08-05 11:30:00', '2026-08-19 11:30:00', 'devuelto'),
(5, 5, '2026-08-10 09:15:00', '2026-08-24 09:15:00', 'devuelto'),
(6, 2, '2026-09-01 14:00:00', '2026-09-15 14:00:00', 'devuelto'),
(7, 10, '2026-09-10 16:45:00', '2026-09-24 16:45:00', 'vencido'),
(9, 13, '2026-09-15 08:00:00', '2026-09-29 08:00:00', 'devuelto'),
(10, 8, '2026-09-20 12:20:00', '2026-10-04 12:20:00', 'devuelto'),
(11, 15, '2026-09-25 15:10:00', '2026-10-09 15:10:00', 'vencido'),
(3, 12, '2026-10-01 10:00:00', '2026-10-15 10:00:00', 'devuelto'),
(12, 4, '2026-10-02 11:00:00', '2026-10-16 11:00:00', 'vencido'),
(5, 11, '2026-11-10 13:00:00', '2026-11-28 13:00:00', 'prestado'),
(7, 7, '2026-11-12 10:30:00', '2026-11-29 10:30:00', 'prestado'),
(9, 14, '2026-11-14 17:00:00', '2026-11-30 17:00:00', 'reservado'),
(13, 9, '2026-11-15 09:00:00', '2026-12-01 09:00:00', 'prestado'),
(14, 6, '2026-11-16 16:00:00', '2026-12-02 16:00:00', 'reservado');
