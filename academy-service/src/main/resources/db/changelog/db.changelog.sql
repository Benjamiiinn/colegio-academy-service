--liquibase formatted sql

--changeset equipo:1
CREATE TABLE cursos (
    id BIGSERIAL PRIMARY KEY,
    nivel VARCHAR(50) NOT NULL,
    letra VARCHAR(5) NOT NULL
);

CREATE TABLE matriculas (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    CONSTRAINT fk_curso_matricula FOREIGN KEY (curso_id) REFERENCES cursos(id)
);

CREATE TABLE asignaturas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    docente_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    CONSTRAINT fk_curso_asignatura FOREIGN KEY (curso_id) REFERENCES cursos(id)
);

CREATE TABLE calificaciones (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL,
    asignatura_id BIGINT NOT NULL,
    nota DECIMAL(3,1),
    descripcion VARCHAR(100),
    CONSTRAINT fk_asignatura_calif FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id)
);

--changeset equipo:2
-- 1. Insertamos Cursos
INSERT INTO cursos (nivel, letra) VALUES ('1 Medio', 'A'), ('1 Medio', 'B');

-- 2. Insertamos Matrículas (Pedrito ID 2 y Maria ID 3 al Curso 1)
INSERT INTO matriculas (estudiante_id, curso_id) VALUES (2, 1), (3, 1);

-- 3. Insertamos Asignaturas (Docente ID 4)
INSERT INTO asignaturas (nombre, docente_id, curso_id) VALUES ('Matemáticas', 4, 1), ('Lenguaje', 4, 1), ('Historia', 4, 2);

-- 4. Insertamos Calificaciones
INSERT INTO calificaciones (estudiante_id, asignatura_id, nota, descripcion) VALUES (2, 1, 6.5, 'Solemne 1');
INSERT INTO calificaciones (estudiante_id, asignatura_id, nota, descripcion) VALUES (3, 1, 5.8, 'Solemne 1');
INSERT INTO calificaciones (estudiante_id, asignatura_id, nota, descripcion) VALUES (2, 2, 7.0, 'Control Lectura');

--changeset equipo:3
ALTER TABLE calificaciones ADD COLUMN docente_id BIGINT NOT NULL DEFAULT 4;