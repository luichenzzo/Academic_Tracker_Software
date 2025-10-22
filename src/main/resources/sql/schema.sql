-- =============================================
-- SISTEMA ACADÉMICO UNIVERSIDAD DEL QUINDÍO
-- Script de creación de tablas - SIN IDENTITY
-- =============================================

-- Tabla: Sede
CREATE TABLE Sede (
                      id_sede NUMBER PRIMARY KEY,
                      nombre VARCHAR2(100) NOT NULL,
                      municipio VARCHAR2(50) NOT NULL,
                      direccion VARCHAR2(200),
                      telefono VARCHAR2(20)
);

-- Tabla: Facultad
CREATE TABLE Facultad (
                          id_facultad NUMBER PRIMARY KEY,
                          codigo_facultad VARCHAR2(10) UNIQUE NOT NULL,
                          nombre VARCHAR2(100) NOT NULL,
                          decano VARCHAR2(100),
                          id_sede NUMBER NOT NULL,
                          CONSTRAINT fk_facultad_sede FOREIGN KEY (id_sede) REFERENCES Sede(id_sede)
);

-- Tabla: TipoPrograma
CREATE TABLE TipoPrograma (
                              id_tipo_programa NUMBER PRIMARY KEY,
                              nombre VARCHAR2(20) UNIQUE NOT NULL,
                              descripcion VARCHAR2(100),
                              requiere_trabajo_grado NUMBER(1) DEFAULT 1
);

-- Tabla: ProgramaAcademico
CREATE TABLE ProgramaAcademico (
                                   cod_programa NUMBER PRIMARY KEY,
                                   codigo_programa VARCHAR2(10) UNIQUE NOT NULL,
                                   nombre VARCHAR2(150) NOT NULL,
                                   creditos_totales NUMBER NOT NULL,
                                   duracion_semestres NUMBER NOT NULL,
                                   id_tipo_programa NUMBER NOT NULL,
                                   id_facultad NUMBER NOT NULL,
                                   CONSTRAINT fk_programa_tipo FOREIGN KEY (id_tipo_programa) REFERENCES TipoPrograma(id_tipo_programa),
                                   CONSTRAINT fk_programa_facultad FOREIGN KEY (id_facultad) REFERENCES Facultad(id_facultad)
);

-- Tabla: TipoAsignatura
CREATE TABLE TipoAsignatura (
                                id_tipo NUMBER PRIMARY KEY,
                                tipo VARCHAR2(20) UNIQUE NOT NULL,
                                descripcion VARCHAR2(100)
);

-- Tabla: Asignatura
CREATE TABLE Asignatura (
                            cod_asignatura VARCHAR2(15) PRIMARY KEY,
                            nombre VARCHAR2(150) NOT NULL,
                            creditos NUMBER NOT NULL,
                            horas_semanales NUMBER NOT NULL,
                            semestre_sugerido NUMBER NOT NULL,
                            es_trabajo_grado NUMBER(1) DEFAULT 0,
                            id_tipo NUMBER NOT NULL,
                            cod_programa NUMBER NOT NULL,
                            CONSTRAINT fk_asignatura_tipo FOREIGN KEY (id_tipo) REFERENCES TipoAsignatura(id_tipo),
                            CONSTRAINT fk_asignatura_programa FOREIGN KEY (cod_programa) REFERENCES ProgramaAcademico(cod_programa)
);

-- Tabla: RequisitoAsignatura
CREATE TABLE RequisitoAsignatura (
                                     id_requisito NUMBER PRIMARY KEY,
                                     cod_asignatura VARCHAR2(15) NOT NULL,
                                     cod_asignatura_requerida VARCHAR2(15) NOT NULL,
                                     tipo_requisito VARCHAR2(20) DEFAULT 'prerrequisito',
                                     nota_minima NUMBER(3,2) DEFAULT 3.00,
                                     observacion VARCHAR2(200),
                                     CONSTRAINT fk_requisito_asignatura FOREIGN KEY (cod_asignatura) REFERENCES Asignatura(cod_asignatura),
                                     CONSTRAINT fk_requisito_requerida FOREIGN KEY (cod_asignatura_requerida) REFERENCES Asignatura(cod_asignatura)
);

-- Tabla: NivelRiesgo
CREATE TABLE NivelRiesgo (
                             nivel_riesgo NUMBER PRIMARY KEY,
                             descripcion VARCHAR2(200) NOT NULL,
                             creditos_maximos NUMBER NOT NULL
);

-- Tabla: TipoDocumento
CREATE TABLE TipoDocumento (
                               codigo CHAR(2) PRIMARY KEY,
                               descripcion VARCHAR2(50) NOT NULL
);

-- Tabla: Estudiante
CREATE TABLE Estudiante (
                            cod_estudiante VARCHAR2(15) PRIMARY KEY,
                            numero_documento VARCHAR2(20) UNIQUE NOT NULL,
                            tipo_documento VARCHAR2(10) NOT NULL,
                            nombres VARCHAR2(100) NOT NULL,
                            apellidos VARCHAR2(100) NOT NULL,
                            correo_institucional VARCHAR2(100) UNIQUE NOT NULL,
                            telefono VARCHAR2(20),
                            fecha_ingreso DATE NOT NULL,
                            nivel_riesgo NUMBER DEFAULT 0,
                            cod_programa NUMBER NOT NULL,
                            id_sede NUMBER NOT NULL,
                            activo NUMBER(1) DEFAULT 1,
                            CONSTRAINT fk_estudiante_riesgo FOREIGN KEY (nivel_riesgo) REFERENCES NivelRiesgo(nivel_riesgo),
                            CONSTRAINT fk_estudiante_programa FOREIGN KEY (cod_programa) REFERENCES ProgramaAcademico(cod_programa),
                            CONSTRAINT fk_estudiante_sede FOREIGN KEY (id_sede) REFERENCES Sede(id_sede)
);

-- Tabla: Docente
CREATE TABLE Docente (
                         id_docente NUMBER PRIMARY KEY,
                         numero_documento VARCHAR2(20) UNIQUE NOT NULL,
                         tipo_documento CHAR(2) NOT NULL,
                         nombres VARCHAR2(100) NOT NULL,
                         apellidos VARCHAR2(100) NOT NULL,
                         correo_institucional VARCHAR2(100) UNIQUE NOT NULL,
                         telefono VARCHAR2(20),
                         horas_asignadas NUMBER(4,1) DEFAULT 0,
                         activo NUMBER(1) DEFAULT 1
);

-- Tabla: Institucion
CREATE TABLE Institucion (
                             id_institucion NUMBER PRIMARY KEY,
                             nombre VARCHAR2(150) UNIQUE NOT NULL,
                             pais VARCHAR2(50),
                             ciudad VARCHAR2(50)
);

-- Tabla: NivelFormacion
CREATE TABLE NivelFormacion (
                                id_nivel NUMBER PRIMARY KEY,
                                nombre VARCHAR2(30) UNIQUE NOT NULL
);

-- Tabla: FormacionDocente
CREATE TABLE FormacionDocente (
                                  id_formacion NUMBER PRIMARY KEY,
                                  id_docente NUMBER NOT NULL,
                                  id_nivel NUMBER NOT NULL,
                                  id_institucion NUMBER NOT NULL,
                                  titulo VARCHAR2(200) NOT NULL,
                                  anio_graduacion NUMBER,
                                  es_principal NUMBER(1) DEFAULT 0,
                                  CONSTRAINT fk_formacion_docente FOREIGN KEY (id_docente) REFERENCES Docente(id_docente),
                                  CONSTRAINT fk_formacion_nivel FOREIGN KEY (id_nivel) REFERENCES NivelFormacion(id_nivel),
                                  CONSTRAINT fk_formacion_institucion FOREIGN KEY (id_institucion) REFERENCES Institucion(id_institucion)
);

-- Tabla: PeriodoAcademico
CREATE TABLE PeriodoAcademico (
                                  cod_periodo VARCHAR2(10) PRIMARY KEY,
                                  nombre VARCHAR2(50) NOT NULL,
                                  fecha_inicio DATE NOT NULL,
                                  fecha_fin DATE NOT NULL,
                                  fecha_inicio_matriculas DATE NOT NULL,
                                  fecha_fin_matriculas DATE NOT NULL,
                                  fecha_cierre_notas DATE NOT NULL,
                                  activo NUMBER(1) DEFAULT 0
);

-- Tabla: Aula
CREATE TABLE Aula (
                      id_aula NUMBER PRIMARY KEY,
                      codigo VARCHAR2(20) NOT NULL,
                      capacidad_maxima NUMBER NOT NULL,
                      id_facultad NUMBER NOT NULL,
                      CONSTRAINT fk_aula_facultad FOREIGN KEY (id_facultad) REFERENCES Facultad(id_facultad)
);

-- Tabla: DiasSemana
CREATE TABLE DiasSemana (
                            id_dia NUMBER PRIMARY KEY,
                            nombre_dia VARCHAR2(10) UNIQUE NOT NULL,
                            abreviatura VARCHAR2(2) UNIQUE NOT NULL
);

-- Tabla: Grupo
CREATE TABLE Grupo (
                       id_grupo NUMBER PRIMARY KEY,
                       numero_grupo NUMBER NOT NULL,
                       cupo_maximo NUMBER NOT NULL,
                       cupo_ocupado NUMBER DEFAULT 0,
                       cod_asignatura VARCHAR2(15) NOT NULL,
                       cod_periodo VARCHAR2(10) NOT NULL,
                       id_sede NUMBER NOT NULL,
                       activo NUMBER(1) DEFAULT 1,
                       CONSTRAINT fk_grupo_asignatura FOREIGN KEY (cod_asignatura) REFERENCES Asignatura(cod_asignatura),
                       CONSTRAINT fk_grupo_periodo FOREIGN KEY (cod_periodo) REFERENCES PeriodoAcademico(cod_periodo),
                       CONSTRAINT fk_grupo_sede FOREIGN KEY (id_sede) REFERENCES Sede(id_sede)
);

-- Tabla: HorarioGrupo
CREATE TABLE HorarioGrupo (
                              id_horario NUMBER PRIMARY KEY,
                              id_grupo NUMBER NOT NULL,
                              id_dia NUMBER NOT NULL,
                              hora_inicio TIMESTAMP NOT NULL,
                              hora_fin TIMESTAMP NOT NULL,
                              id_aula NUMBER NOT NULL,
                              CONSTRAINT fk_horario_grupo FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo),
                              CONSTRAINT fk_horario_dia FOREIGN KEY (id_dia) REFERENCES DiasSemana(id_dia),
                              CONSTRAINT fk_horario_aula FOREIGN KEY (id_aula) REFERENCES Aula(id_aula)
);

-- Tabla: DocenteGrupo
CREATE TABLE DocenteGrupo (
                              id_docente_grupo NUMBER PRIMARY KEY,
                              id_docente NUMBER NOT NULL,
                              id_grupo NUMBER NOT NULL,
                              horas_grupo NUMBER(4,1) NOT NULL,
                              es_principal NUMBER(1) DEFAULT 1,
                              CONSTRAINT fk_docentegrupo_docente FOREIGN KEY (id_docente) REFERENCES Docente(id_docente),
                              CONSTRAINT fk_docentegrupo_grupo FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo)
);

-- Tabla: Matricula
CREATE TABLE Matricula (
                           id_matricula NUMBER PRIMARY KEY,
                           cod_estudiante VARCHAR2(15) NOT NULL,
                           cod_periodo VARCHAR2(10) NOT NULL,
                           fecha_matricula TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           total_creditos NUMBER DEFAULT 0,
                           estado VARCHAR2(20) DEFAULT 'activa',
                           CONSTRAINT fk_matricula_estudiante FOREIGN KEY (cod_estudiante) REFERENCES Estudiante(cod_estudiante),
                           CONSTRAINT fk_matricula_periodo FOREIGN KEY (cod_periodo) REFERENCES PeriodoAcademico(cod_periodo)
);

-- Tabla: DetalleMatricula
CREATE TABLE DetalleMatricula (
                                  id_detalle NUMBER PRIMARY KEY,
                                  id_matricula NUMBER NOT NULL,
                                  id_grupo NUMBER NOT NULL,
                                  fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  estado VARCHAR2(20) DEFAULT 'inscrito',
                                  numero_intento NUMBER DEFAULT 1,
                                  es_repeticion_calculado NUMBER(1),
                                  CONSTRAINT fk_detalle_matricula FOREIGN KEY (id_matricula) REFERENCES Matricula(id_matricula),
                                  CONSTRAINT fk_detalle_grupo FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo)
);

-- Tabla: ReglaEvaluacion
CREATE TABLE ReglaEvaluacion (
                                 id_regla NUMBER PRIMARY KEY,
                                 id_grupo NUMBER NOT NULL,
                                 nombre_item VARCHAR2(100) NOT NULL,
                                 porcentaje NUMBER(5,2) NOT NULL,
                                 fecha_limite DATE,
                                 CONSTRAINT fk_regla_grupo FOREIGN KEY (id_grupo) REFERENCES Grupo(id_grupo)
);

-- Tabla: Calificacion
CREATE TABLE Calificacion (
                              id_calificacion NUMBER PRIMARY KEY,
                              id_detalle NUMBER NOT NULL,
                              id_regla NUMBER NOT NULL,
                              nota NUMBER(3,2) NOT NULL,
                              fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              id_docente_registra NUMBER NOT NULL,
                              CONSTRAINT fk_calificacion_detalle FOREIGN KEY (id_detalle) REFERENCES DetalleMatricula(id_detalle),
                              CONSTRAINT fk_calificacion_regla FOREIGN KEY (id_regla) REFERENCES ReglaEvaluacion(id_regla),
                              CONSTRAINT fk_calificacion_docente FOREIGN KEY (id_docente_registra) REFERENCES Docente(id_docente)
);

-- Tabla: NotaDefinitiva
CREATE TABLE NotaDefinitiva (
                                id_nota_definitiva NUMBER PRIMARY KEY,
                                id_detalle NUMBER NOT NULL UNIQUE,
                                nota_definitiva NUMBER(3,2) NOT NULL,
                                fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                cerrada NUMBER(1) DEFAULT 0,
                                CONSTRAINT fk_notadef_detalle FOREIGN KEY (id_detalle) REFERENCES DetalleMatricula(id_detalle)
);

-- Tabla: HistorialAcademico
CREATE TABLE HistorialAcademico (
                                    id_historial NUMBER PRIMARY KEY,
                                    cod_estudiante VARCHAR2(15) NOT NULL,
                                    cod_periodo VARCHAR2(10) NOT NULL,
                                    creditos_matriculados NUMBER DEFAULT 0,
                                    creditos_aprobados NUMBER DEFAULT 0,
                                    creditos_acumulados NUMBER DEFAULT 0,
                                    promedio_periodo NUMBER(3,2),
                                    promedio_acumulado NUMBER(3,2),
                                    nivel_riesgo_calculado NUMBER DEFAULT 0,
                                    CONSTRAINT fk_historial_estudiante FOREIGN KEY (cod_estudiante) REFERENCES Estudiante(cod_estudiante),
                                    CONSTRAINT fk_historial_periodo FOREIGN KEY (cod_periodo) REFERENCES PeriodoAcademico(cod_periodo),
                                    CONSTRAINT fk_historial_riesgo FOREIGN KEY (nivel_riesgo_calculado) REFERENCES NivelRiesgo(nivel_riesgo)
);

-- Tabla: DirectorTrabajoGrado
CREATE TABLE DirectorTrabajoGrado (
                                      id_director NUMBER PRIMARY KEY,
                                      cod_estudiante VARCHAR2(15) NOT NULL,
                                      id_docente NUMBER NOT NULL,
                                      tema_trabajo VARCHAR2(300),
                                      fecha_asignacion DATE NOT NULL,
                                      estado VARCHAR2(20) DEFAULT 'activo',
                                      CONSTRAINT fk_director_estudiante FOREIGN KEY (cod_estudiante) REFERENCES Estudiante(cod_estudiante),
                                      CONSTRAINT fk_director_docente FOREIGN KEY (id_docente) REFERENCES Docente(id_docente)
);

-- Tabla: TrabajoGrado
CREATE TABLE TrabajoGrado (
                              id_trabajo NUMBER PRIMARY KEY,
                              cod_estudiante VARCHAR2(15) NOT NULL,
                              id_docente_director NUMBER NOT NULL,
                              titulo VARCHAR2(300),
                              fecha_inicio DATE,
                              fecha_finalizacion DATE,
                              estado VARCHAR2(20) DEFAULT 'en_proceso',
                              porcentaje_avance NUMBER(5,2) DEFAULT 0.00,
                              cumple_creditos NUMBER(1) DEFAULT 0,
                              CONSTRAINT fk_trabajo_estudiante FOREIGN KEY (cod_estudiante) REFERENCES Estudiante(cod_estudiante),
                              CONSTRAINT fk_trabajo_director FOREIGN KEY (id_docente_director) REFERENCES Docente(id_docente)
);

-- Tabla: UsuarioSistema
CREATE TABLE UsuarioSistema (
                                id_usuario NUMBER PRIMARY KEY,
                                username VARCHAR2(50) UNIQUE NOT NULL,
                                password_hash VARCHAR2(255) NOT NULL,
                                rol VARCHAR2(20) NOT NULL,
                                id_referencia VARCHAR2(20),
                                tipo_referencia VARCHAR2(20),
                                activo NUMBER(1) DEFAULT 1
);

-- Tabla: Bitacora
CREATE TABLE Bitacora (
                          id_bitacora NUMBER PRIMARY KEY,
                          id_usuario NUMBER NOT NULL,
                          operacion VARCHAR2(50) NOT NULL,
                          tabla_afectada VARCHAR2(50) NOT NULL,
                          id_registro_afectado VARCHAR2(50),
                          detalle CLOB,
                          resultado_operacion VARCHAR2(20),
                          ip_origen VARCHAR2(45),
                          fecha_operacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES UsuarioSistema(id_usuario)
);

COMMIT;

SELECT 'TABLAS CREADAS EXITOSAMENTE' as mensaje FROM DUAL;