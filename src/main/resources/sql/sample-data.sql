-- Sample Data for Academic Tracker Software
-- Sistema Académico Universidad del Quindío
-- Run this after schema.sql to populate the database with test data

-- Insert Sedes (Campuses) - Manual IDs
INSERT INTO Sede (id_sede, nombre, municipio, direccion, telefono)
VALUES (1, 'Sede Principal', 'Armenia', 'Cra 15 Calle 12 Norte', '7359300');

INSERT INTO Sede (id_sede, nombre, municipio, direccion, telefono)
VALUES (2, 'Sede La Badea', 'Armenia', 'Kilómetro 2 vía Armenia-Circasia', '7461234');

INSERT INTO Sede (id_sede, nombre, municipio, direccion, telefono)
VALUES (3, 'Sede Centro', 'Armenia', 'Carrera 14 Calle 14', '7359400');

-- Insert Facultades (Faculties) - Manual IDs
INSERT INTO Facultad (id_facultad, codigo_facultad, nombre, decano, id_sede)
VALUES (1, 'FING', 'Facultad de Ingeniería', 'Dr. Carlos Ramírez', 1);

INSERT INTO Facultad (id_facultad, codigo_facultad, nombre, decano, id_sede)
VALUES (2, 'FCIE', 'Facultad de Ciencias', 'Dra. María González', 1);

INSERT INTO Facultad (id_facultad, codigo_facultad, nombre, decano, id_sede)
VALUES (3, 'FCEA', 'Facultad de Ciencias Económicas y Administrativas', 'Dr. Jorge Martínez', 1);

INSERT INTO Facultad (id_facultad, codigo_facultad, nombre, decano, id_sede)
VALUES (4, 'FEDU', 'Facultad de Educación', 'Dra. Ana López', 2);

INSERT INTO Facultad (id_facultad, codigo_facultad, nombre, decano, id_sede)
VALUES (5, 'FSAL', 'Facultad de Ciencias de la Salud', 'Dr. Pedro Sánchez', 1);

-- Insert TipoPrograma (Program Types) - Manual IDs
INSERT INTO TipoPrograma (id_tipo_programa, nombre, descripcion, requiere_trabajo_grado)
VALUES (1, 'Pregrado', 'Programas de pregrado universitario', 1);

INSERT INTO TipoPrograma (id_tipo_programa, nombre, descripcion, requiere_trabajo_grado)
VALUES (2, 'Especialización', 'Programas de especialización', 1);

INSERT INTO TipoPrograma (id_tipo_programa, nombre, descripcion, requiere_trabajo_grado)
VALUES (3, 'Maestría', 'Programas de maestría', 1);

INSERT INTO TipoPrograma (id_tipo_programa, nombre, descripcion, requiere_trabajo_grado)
VALUES (4, 'Doctorado', 'Programas de doctorado', 1);

INSERT INTO TipoPrograma (id_tipo_programa, nombre, descripcion, requiere_trabajo_grado)
VALUES (5, 'Técnico', 'Programas técnicos profesionales', 0);

-- Insert ProgramaAcademico (Academic Programs)
INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (1, 'ING-SIS', 'Ingeniería de Sistemas y Computación', 160, 10, 1, 1);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (2, 'ING-CIV', 'Ingeniería Civil', 165, 10, 1, 1);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (3, 'ING-IND', 'Ingeniería Industrial', 158, 10, 1, 1);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (4, 'LIC-MAT', 'Licenciatura en Matemáticas', 150, 10, 1, 2);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (5, 'LIC-BIO', 'Licenciatura en Biología', 152, 10, 1, 2);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (6, 'ADM-EMP', 'Administración de Empresas', 148, 10, 1, 3);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (7, 'CONT-PUB', 'Contaduría Pública', 150, 10, 1, 3);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (8, 'ECO', 'Economía', 145, 10, 1, 3);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (9, 'LIC-INF', 'Licenciatura en Informática', 155, 10, 1, 4);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (10, 'MED', 'Medicina', 200, 12, 1, 5);

INSERT INTO ProgramaAcademico (cod_programa, codigo_programa, nombre, creditos_totales, duracion_semestres, id_tipo_programa, id_facultad)
VALUES (11, 'ENF', 'Enfermería', 165, 10, 1, 5);

-- Insert NivelRiesgo (Risk Levels)
INSERT INTO NivelRiesgo (nivel_riesgo, descripcion, creditos_maximos)
VALUES (0, 'Sin riesgo académico - Puede matricular normalmente', 24);

INSERT INTO NivelRiesgo (nivel_riesgo, descripcion, creditos_maximos)
VALUES (1, 'Riesgo bajo - Promedio entre 3.0 y 3.5', 20);

INSERT INTO NivelRiesgo (nivel_riesgo, descripcion, creditos_maximos)
VALUES (2, 'Riesgo medio - Promedio entre 2.5 y 2.99', 16);

INSERT INTO NivelRiesgo (nivel_riesgo, descripcion, creditos_maximos)
VALUES (3, 'Riesgo alto - Promedio menor a 2.5', 12);

-- Insert TipoDocumento (Document Types)
INSERT INTO TipoDocumento (codigo, descripcion) VALUES ('CC', 'Cédula de Ciudadanía');
INSERT INTO TipoDocumento (codigo, descripcion) VALUES ('TI', 'Tarjeta de Identidad');
INSERT INTO TipoDocumento (codigo, descripcion) VALUES ('CE', 'Cédula de Extranjería');
INSERT INTO TipoDocumento (codigo, descripcion) VALUES ('PA', 'Pasaporte');
INSERT INTO TipoDocumento (codigo, descripcion) VALUES ('RC', 'Registro Civil');

-- Insert TipoAsignatura (Subject Types) - Manual IDs
INSERT INTO TipoAsignatura (id_tipo, tipo, descripcion)
VALUES (1, 'Obligatoria', 'Asignatura obligatoria del programa');

INSERT INTO TipoAsignatura (id_tipo, tipo, descripcion)
VALUES (2, 'Electiva', 'Asignatura electiva');

INSERT INTO TipoAsignatura (id_tipo, tipo, descripcion)
VALUES (3, 'Optativa', 'Asignatura optativa');

INSERT INTO TipoAsignatura (id_tipo, tipo, descripcion)
VALUES (4, 'Fundamental', 'Asignatura fundamental del área');

-- Insert Instituciones (Institutions for teacher formation) - Manual IDs
INSERT INTO Institucion (id_institucion, nombre, pais, ciudad)
VALUES (1, 'Universidad del Quindío', 'Colombia', 'Armenia');

INSERT INTO Institucion (id_institucion, nombre, pais, ciudad)
VALUES (2, 'Universidad Nacional de Colombia', 'Colombia', 'Bogotá');

INSERT INTO Institucion (id_institucion, nombre, pais, ciudad)
VALUES (3, 'Universidad de los Andes', 'Colombia', 'Bogotá');

-- Insert NivelFormacion (Formation Levels) - Manual IDs
INSERT INTO NivelFormacion (id_nivel, nombre) VALUES (1, 'Pregrado');
INSERT INTO NivelFormacion (id_nivel, nombre) VALUES (2, 'Especialización');
INSERT INTO NivelFormacion (id_nivel, nombre) VALUES (3, 'Maestría');
INSERT INTO NivelFormacion (id_nivel, nombre) VALUES (4, 'Doctorado');

-- Insert DiasSemana (Days of Week) - Manual IDs
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (1, 'Lunes', 'L');
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (2, 'Martes', 'M');
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (3, 'Miércoles', 'W');
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (4, 'Jueves', 'J');
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (5, 'Viernes', 'V');
INSERT INTO DiasSemana (id_dia, nombre_dia, abreviatura) VALUES (6, 'Sábado', 'S');

-- Insert sample Docentes (Teachers) - Manual IDs
INSERT INTO Docente (id_docente, numero_documento, tipo_documento, nombres, apellidos, correo_institucional, telefono, horas_asignadas, activo)
VALUES (1, '79123456', 'CC', 'Juan Carlos', 'Ramírez López', 'jramirez@uniquindio.edu.co', '3001234567', 40.0, 1);

INSERT INTO Docente (id_docente, numero_documento, tipo_documento, nombres, apellidos, correo_institucional, telefono, horas_asignadas, activo)
VALUES (2, '52987654', 'CC', 'María Fernanda', 'González Pérez', 'mgonzalez@uniquindio.edu.co', '3109876543', 40.0, 1);

INSERT INTO Docente (id_docente, numero_documento, tipo_documento, nombres, apellidos, correo_institucional, telefono, horas_asignadas, activo)
VALUES (3, '80456789', 'CC', 'Pedro Antonio', 'Martínez Silva', 'pmartinez@uniquindio.edu.co', '3157654321', 30.0, 1);

-- Insert sample UsuarioSistema (System Users) - Manual IDs
-- Password for all: admin123 (hashed with BCrypt)
INSERT INTO UsuarioSistema (id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo)
VALUES (1, 'admin', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'ADMIN', NULL, NULL, 1);

INSERT INTO UsuarioSistema (id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo)
VALUES (2, 'jramirez', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'DOCENTE', '1', 'Docente', 1);

INSERT INTO UsuarioSistema (id_usuario, username, password_hash, rol, id_referencia, tipo_referencia, activo)
VALUES (3, 'mgonzalez', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'DOCENTE', '2', 'Docente', 1);

COMMIT;

-- Display summary
SELECT 'Sample data loaded successfully!' AS status FROM DUAL;
