-- =============================================
-- PROCEDIMIENTO: Actualizar Historial Académico
-- =============================================
CREATE OR REPLACE PROCEDURE ActualizarHistorialAcademico(
    p_id_detalle IN NUMBER
) AS
    v_cod_estudiante VARCHAR2(15);
    v_cod_periodo VARCHAR2(10);
    v_creditos_asignatura NUMBER;
    v_nota_definitiva NUMBER(3,2);
    v_creditos_aprobados NUMBER;
    v_total_creditos_mat NUMBER;
    v_promedio_periodo NUMBER(3,2);
    v_promedio_acumulado NUMBER(3,2);
    v_nivel_riesgo NUMBER;
BEGIN
    -- Obtener datos del estudiante y periodo
SELECT m.cod_estudiante, m.cod_periodo, a.creditos, nd.nota_definitiva
INTO v_cod_estudiante, v_cod_periodo, v_creditos_asignatura, v_nota_definitiva
FROM DetalleMatricula dm
         JOIN Matricula m ON dm.id_matricula = m.id_matricula
         JOIN Grupo g ON dm.id_grupo = g.id_grupo
         JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura
         JOIN NotaDefinitiva nd ON dm.id_detalle = nd.id_detalle
WHERE dm.id_detalle = p_id_detalle;

-- Calcular créditos aprobados en el periodo
SELECT NVL(SUM(a.creditos), 0)
INTO v_creditos_aprobados
FROM DetalleMatricula dm
         JOIN Matricula m ON dm.id_matricula = m.id_matricula
         JOIN Grupo g ON dm.id_grupo = g.id_grupo
         JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura
         JOIN NotaDefinitiva nd ON dm.id_detalle = nd.id_detalle
WHERE m.cod_estudiante = v_cod_estudiante
  AND m.cod_periodo = v_cod_periodo
  AND nd.nota_definitiva >= 3.0
  AND nd.cerrada = 1;

-- Calcular total créditos matriculados
SELECT NVL(SUM(a.creditos), 0)
INTO v_total_creditos_mat
FROM DetalleMatricula dm
         JOIN Matricula m ON dm.id_matricula = m.id_matricula
         JOIN Grupo g ON dm.id_grupo = g.id_grupo
         JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura
         JOIN NotaDefinitiva nd ON dm.id_detalle = nd.id_detalle
WHERE m.cod_estudiante = v_cod_estudiante
  AND m.cod_periodo = v_cod_periodo
  AND nd.cerrada = 1;

-- Calcular promedio del periodo
SELECT ROUND(AVG(nd.nota_definitiva), 2)
INTO v_promedio_periodo
FROM DetalleMatricula dm
         JOIN Matricula m ON dm.id_matricula = m.id_matricula
         JOIN NotaDefinitiva nd ON dm.id_detalle = nd.id_detalle
WHERE m.cod_estudiante = v_cod_estudiante
  AND m.cod_periodo = v_cod_periodo
  AND nd.cerrada = 1;

-- Calcular nivel de riesgo basado en rendimiento
IF v_promedio_periodo < 2.5 THEN
        v_nivel_riesgo := 4; -- Riesgo alto
    ELSIF v_promedio_periodo < 3.0 THEN
        v_nivel_riesgo := 3; -- Riesgo medio-alto
    ELSIF v_promedio_periodo < 3.5 THEN
        v_nivel_riesgo := 2; -- Riesgo medio
    ELSIF v_promedio_periodo < 4.0 THEN
        v_nivel_riesgo := 1; -- Riesgo medio-bajo
ELSE
        v_nivel_riesgo := 0; -- Riesgo bajo
END IF;

    -- Insertar o actualizar historial académico
BEGIN
INSERT INTO HistorialAcademico (
    id_historial, cod_estudiante, cod_periodo,
    creditos_matriculados, creditos_aprobados, creditos_acumulados,
    promedio_periodo, promedio_acumulado, nivel_riesgo_calculado
)
VALUES (
           historial_seq.NEXTVAL, v_cod_estudiante, v_cod_periodo,
           v_total_creditos_mat, v_creditos_aprobados, v_creditos_aprobados,
           v_promedio_periodo, v_promedio_periodo, v_nivel_riesgo
       );
EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN
            -- Si ya existe, actualizar
UPDATE HistorialAcademico
SET creditos_matriculados = v_total_creditos_mat,
    creditos_aprobados = v_creditos_aprobados,
    creditos_acumulados = v_creditos_aprobados,
    promedio_periodo = v_promedio_periodo,
    promedio_acumulado = v_promedio_periodo,
    nivel_riesgo_calculado = v_nivel_riesgo
WHERE cod_estudiante = v_cod_estudiante
  AND cod_periodo = v_cod_periodo;
END;

    -- Actualizar nivel de riesgo del estudiante
UPDATE Estudiante
SET nivel_riesgo = v_nivel_riesgo
WHERE cod_estudiante = v_cod_estudiante;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20030, 'Error actualizando historial académico: ' || SQLERRM);
END;

-- =============================================
-- PROCEDIMIENTO: Crear Usuario Estudiante
-- =============================================
CREATE OR REPLACE PROCEDURE CrearUsuarioPorCedula(
    p_numero_cedula IN VARCHAR2
) AS
    v_cod_estudiante VARCHAR2(15);
    v_nombres VARCHAR2(100);
    v_apellidos VARCHAR2(100);
    v_correo_institucional VARCHAR2(100);
    v_username VARCHAR2(50);
    v_password_plano VARCHAR2(100);
    v_password_hash VARCHAR2(255);
    v_primer_nombre VARCHAR2(50);
    v_id_usuario NUMBER;
    v_existe_usuario NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Buscando estudiante con cédula: ' || p_numero_cedula);

    BEGIN
        SELECT cod_estudiante, nombres, apellidos, correo_institucional
        INTO v_cod_estudiante, v_nombres, v_apellidos, v_correo_institucional
        FROM Estudiante
        WHERE numero_documento = p_numero_cedula;

        DBMS_OUTPUT.PUT_LINE('Estudiante encontrado: ' || v_nombres || ' ' || v_apellidos);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20060,
                                    'No se encontró ningún estudiante con la cédula: ' || p_numero_cedula);
        WHEN TOO_MANY_ROWS THEN
            RAISE_APPLICATION_ERROR(-20061,
                                    'Múltiples estudiantes con la misma cédula: ' || p_numero_cedula);
    END;

    v_primer_nombre := SUBSTR(v_nombres, 1, INSTR(v_nombres || ' ', ' ') - 1);
    IF v_primer_nombre IS NULL THEN
        v_primer_nombre := v_nombres;
    END IF;

    v_username := LOWER(v_correo_institucional);


    v_password_plano := v_primer_nombre || '123';

    v_password_hash := '$2a$10$MBTlYXlcD7LqdlrsDSlPAeQSANWGbF3z4AiY9zfB6B7Q4xbBbBt6.';


    BEGIN
        SELECT id_usuario INTO v_existe_usuario
        FROM UsuarioSistema
        WHERE username = v_username;


        UPDATE UsuarioSistema
        SET password_hash = v_password_hash,
            id_referencia = v_cod_estudiante,
            tipo_referencia = 'estudiante',
            activo = 1
        WHERE username = v_username;

        DBMS_OUTPUT.PUT_LINE('✓ Usuario actualizado exitosamente');
        DBMS_OUTPUT.PUT_LINE('  Cédula: ' || p_numero_cedula);
        DBMS_OUTPUT.PUT_LINE('  Estudiante: ' || v_nombres || ' ' || v_apellidos);
        DBMS_OUTPUT.PUT_LINE('  Username: ' || v_username);
        DBMS_OUTPUT.PUT_LINE('  Contraseña: ' || v_password_plano);
        DBMS_OUTPUT.PUT_LINE('  Rol: estudiante');

    EXCEPTION
        WHEN NO_DATA_FOUND THEN

            SELECT NVL(MAX(id_usuario), 0) + 1 INTO v_id_usuario
            FROM UsuarioSistema;


            INSERT INTO UsuarioSistema (
                id_usuario, username, password_hash, rol,
                id_referencia, tipo_referencia, activo
            ) VALUES (
                         v_id_usuario,
                         v_username,
                         v_password_hash,
                         'estudiante',
                         v_cod_estudiante,
                         'Estudiante',
                         1
                     );

            DBMS_OUTPUT.PUT_LINE('✓ Nuevo usuario creado exitosamente');
            DBMS_OUTPUT.PUT_LINE('  Cédula: ' || p_numero_cedula);
            DBMS_OUTPUT.PUT_LINE('  Estudiante: ' || v_nombres || ' ' || v_apellidos);
            DBMS_OUTPUT.PUT_LINE('  Username: ' || v_username);
            DBMS_OUTPUT.PUT_LINE('  Contraseña: ' || v_password_plano);
            DBMS_OUTPUT.PUT_LINE('  Rol: estudiante');
    END;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('✗ Error: ' || SQLERRM);
        RAISE;
END;

-- =============================================
-- PROCEDIMIENTO: Crear Usuario Profesor
-- =============================================
create or replace PROCEDURE CrearUsuarioPorCedulaDocente(
    p_numero_cedula IN VARCHAR2
) AS
    v_id_docente NUMBER;
    v_nombres VARCHAR2(100);
    v_apellidos VARCHAR2(100);
    v_correo_institucional VARCHAR2(100);
    v_username VARCHAR2(50);
    v_password_plano VARCHAR2(100);
    v_password_hash VARCHAR2(255);
    v_primer_nombre VARCHAR2(50);
    v_id_usuario NUMBER;
    v_existe_usuario NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Buscando docente con cédula: ' || p_numero_cedula);

    BEGIN
        SELECT id_docente, nombres, apellidos, correo_institucional
        INTO v_id_docente, v_nombres, v_apellidos, v_correo_institucional
        FROM Docente
        WHERE numero_documento = p_numero_cedula;

        DBMS_OUTPUT.PUT_LINE('Docente encontrado: ' || v_nombres || ' ' || v_apellidos);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE_APPLICATION_ERROR(-20062,
                                    'No se encontró ningún docente con la cédula: ' || p_numero_cedula);
        WHEN TOO_MANY_ROWS THEN
            RAISE_APPLICATION_ERROR(-20063,
                                    'Múltiples docentes con la misma cédula: ' || p_numero_cedula);
    END;

    v_primer_nombre := SUBSTR(v_nombres, 1, INSTR(v_nombres || ' ', ' ') - 1);
    IF v_primer_nombre IS NULL THEN
        v_primer_nombre := v_nombres;
    END IF;

    v_username := LOWER(v_correo_institucional);
    v_password_plano := v_primer_nombre || '123';
    v_password_hash := '$2a$10$/Pcjm0fpOh0qwP/OjTbyJeUjOmD.txxbh8v27A.bv59yZbLyM5omO';

    BEGIN
        SELECT id_usuario INTO v_existe_usuario
        FROM UsuarioSistema
        WHERE username = v_username;

        -- Si existe, actualizar
        UPDATE UsuarioSistema
        SET password_hash = v_password_hash,
            id_referencia = TO_CHAR(v_id_docente),
            tipo_referencia = 'docente',
            activo = 1
        WHERE username = v_username;

        DBMS_OUTPUT.PUT_LINE(' Usuario actualizado exitosamente');
        DBMS_OUTPUT.PUT_LINE('  Cédula: ' || p_numero_cedula);
        DBMS_OUTPUT.PUT_LINE('  Docente: ' || v_nombres || ' ' || v_apellidos);
        DBMS_OUTPUT.PUT_LINE('  Username: ' || v_username);
        DBMS_OUTPUT.PUT_LINE('  Contraseña: ' || v_password_plano);
        DBMS_OUTPUT.PUT_LINE('  Rol: docente');

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            SELECT NVL(MAX(id_usuario), 0) + 1 INTO v_id_usuario
            FROM UsuarioSistema;

            INSERT INTO UsuarioSistema (
                id_usuario, username, password_hash, rol,
                id_referencia, tipo_referencia, activo
            ) VALUES (
                         v_id_usuario,
                         v_username,
                         v_password_hash,
                         'docente',
                         TO_CHAR(v_id_docente),
                         'Docente',
                         1
                     );

            DBMS_OUTPUT.PUT_LINE(' Nuevo usuario creado exitosamente');
            DBMS_OUTPUT.PUT_LINE('  Cédula: ' || p_numero_cedula);
            DBMS_OUTPUT.PUT_LINE('  Docente: ' || v_nombres || ' ' || v_apellidos);
            DBMS_OUTPUT.PUT_LINE('  Username: ' || v_username);
            DBMS_OUTPUT.PUT_LINE('  Contraseña: ' || v_password_plano);
            DBMS_OUTPUT.PUT_LINE('  Rol: docente');
    END;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE(' Error: ' || SQLERRM);
        RAISE;
END;