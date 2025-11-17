-- =============================================
-- TRIGGER 1: Validar fechas de matrícula
-- =============================================
CREATE OR REPLACE TRIGGER TRG_ValidarFechasMatricula
BEFORE INSERT OR UPDATE ON DetalleMatricula
                            FOR EACH ROW
DECLARE
v_fecha_actual DATE := SYSDATE;
v_fecha_inicio_mat DATE;
v_fecha_fin_mat DATE;
v_periodo_matricula VARCHAR2(10);
v_estado_periodo NUMBER;
BEGIN

SELECT m.cod_periodo INTO v_periodo_matricula
FROM Matricula m
WHERE m.id_matricula = :NEW.id_matricula;

-- Obtener fechas de matrícula del periodo
SELECT p.fecha_inicio_matriculas, p.fecha_fin_matriculas, p.activo
INTO v_fecha_inicio_mat, v_fecha_fin_mat, v_estado_periodo
FROM PeriodoAcademico p
WHERE p.cod_periodo = v_periodo_matricula;

-- Validar que el periodo esté activo para matrículas
IF v_estado_periodo = 0 THEN
        RAISE_APPLICATION_ERROR(-20001,
            'El periodo académico ' || v_periodo_matricula || ' no está activo para matrículas.');
END IF;

    -- Validar fechas de matrícula
    IF v_fecha_actual < v_fecha_inicio_mat THEN
        RAISE_APPLICATION_ERROR(-20002,
            'Las matrículas para el periodo ' || v_periodo_matricula ||
            ' inician el ' || TO_CHAR(v_fecha_inicio_mat, 'DD/MM/YYYY'));
    ELSIF v_fecha_actual > v_fecha_fin_mat THEN
        RAISE_APPLICATION_ERROR(-20003,
            'Las matrículas para el periodo ' || v_periodo_matricula ||
            ' finalizaron el ' || TO_CHAR(v_fecha_fin_mat, 'DD/MM/YYYY'));
END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20004, 'Periodo académico no encontrado.');
WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20005, 'Error validando fechas de matrícula: ' || SQLERRM);
END;

-- =============================================
-- TRIGGER 2: Validar prerrequisitos de asignaturas
-- =============================================
CREATE OR REPLACE TRIGGER TRG_ValidarPrerrequisitos
    BEFORE INSERT ON DetalleMatricula
    FOR EACH ROW
DECLARE
    v_cod_estudiante VARCHAR2(15);
    v_cod_asignatura VARCHAR2(15);
    v_cod_asignatura_req VARCHAR2(15);
    v_nota_minima NUMBER(3,2);
    v_tipo_requisito VARCHAR2(20);
    v_nota_obtenida NUMBER(3,2);
    v_requisito_cumplido BOOLEAN := FALSE;
    v_contador NUMBER := 0;

    CURSOR c_requisitos IS
        SELECT r.cod_asignatura_requerida, r.tipo_requisito, r.nota_minima
        FROM RequisitoAsignatura r
        WHERE r.cod_asignatura = v_cod_asignatura
          AND r.tipo_requisito = 'prerrequisito';
BEGIN
    -- Obtener código del estudiante y asignatura
    SELECT m.cod_estudiante INTO v_cod_estudiante
    FROM Matricula m
    WHERE m.id_matricula = :NEW.id_matricula;

    SELECT g.cod_asignatura INTO v_cod_asignatura
    FROM Grupo g
    WHERE g.id_grupo = :NEW.id_grupo;

    -- Verificar si la asignatura tiene prerrequisitos
    OPEN c_requisitos;
    FETCH c_requisitos INTO v_cod_asignatura_req, v_tipo_requisito, v_nota_minima;

    IF c_requisitos%NOTFOUND THEN
        -- No tiene prerrequisitos, permitir matrícula
        CLOSE c_requisitos;
        RETURN;
    END IF;

    -- Validar cada prerrequisito
    WHILE c_requisitos%FOUND LOOP
            v_contador := v_contador + 1;

            -- Buscar si el estudiante aprobó el prerrequisito
            BEGIN
                SELECT nd.nota_definitiva INTO v_nota_obtenida
                FROM DetalleMatricula dm
                         JOIN Grupo g ON dm.id_grupo = g.id_grupo
                         JOIN Matricula m ON dm.id_matricula = m.id_matricula
                         JOIN NotaDefinitiva nd ON dm.id_detalle = nd.id_detalle
                WHERE m.cod_estudiante = v_cod_estudiante
                  AND g.cod_asignatura = v_cod_asignatura_req
                  AND nd.nota_definitiva >= v_nota_minima
                  AND nd.cerrada = 1
                  AND ROWNUM = 1;

                v_requisito_cumplido := TRUE;

            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_requisito_cumplido := FALSE;
            END;

            -- Si no cumple un prerrequisito, mostrar error
            IF NOT v_requisito_cumplido THEN
                CLOSE c_requisitos;

                -- Obtener nombre de la asignatura requerida
                DECLARE
                    v_nombre_asignatura VARCHAR2(150);
                    v_nombre_requerida VARCHAR2(150);
                BEGIN
                    SELECT a.nombre INTO v_nombre_asignatura
                    FROM Asignatura a
                    WHERE a.cod_asignatura = v_cod_asignatura;

                    SELECT a.nombre INTO v_nombre_requerida
                    FROM Asignatura a
                    WHERE a.cod_asignatura = v_cod_asignatura_req;

                    RAISE_APPLICATION_ERROR(-20010,
                                            'No cumple con el prerrequisito de la asignatura ' || v_nombre_asignatura ||
                                            '. Debe aprobar ' || v_nombre_requerida || ' con nota mínima de ' || v_nota_minima);
                END;
            END IF;

            FETCH c_requisitos INTO v_cod_asignatura_req, v_tipo_requisito, v_nota_minima;
        END LOOP;

    CLOSE c_requisitos;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL; -- No hay matrícula o grupo, el constraint PK manejará el error
    WHEN OTHERS THEN
        IF c_requisitos%ISOPEN THEN
            CLOSE c_requisitos;
        END IF;
        RAISE_APPLICATION_ERROR(-20011, 'Error validando prerrequisitos: ' || SQLERRM);
END;


-- =============================================
-- TRIGGER 3: Actualizar nota definitiva automáticamente
-- =============================================
-- Package to hold collection of id_detalle affected by DML on Calificacion

CREATE OR REPLACE PACKAGE pkg_trg_nota AS
    TYPE t_id_set IS TABLE OF PLS_INTEGER INDEX BY PLS_INTEGER;
    g_id_set t_id_set;

    PROCEDURE add_det(p_det IN PLS_INTEGER);
    PROCEDURE process_and_clear;
END pkg_trg_nota;
CREATE OR REPLACE PACKAGE BODY pkg_trg_nota AS
    PROCEDURE add_det(p_det IN PLS_INTEGER) IS
    BEGIN
        IF p_det IS NOT NULL THEN
            g_id_set(p_det) := 1; -- mark presence
        END IF;
    EXCEPTION WHEN OTHERS THEN
        NULL; -- safe guard, do not raise from row trigger
    END add_det;

    PROCEDURE process_and_clear IS
        key PLS_INTEGER;
        v_id_detalle PLS_INTEGER;
        v_nota_definitiva NUMBER(7,4);
        v_id_nota_def NUMBER;
    BEGIN
        key := g_id_set.FIRST;
        WHILE key IS NOT NULL LOOP
                v_id_detalle := key;

                BEGIN
                    -- Calculate definitive grade for this enrollment as the average of Calificacion.nota
                    SELECT NVL(AVG(c.nota), 0)
                    INTO v_nota_definitiva
                    FROM Calificacion c
                    WHERE c.id_detalle = v_id_detalle;

                    v_nota_definitiva := GREATEST(0, LEAST(5, v_nota_definitiva));

                    -- Try to update existing NotaDefinitiva
                    UPDATE NotaDefinitiva
                    SET nota_definitiva = v_nota_definitiva,
                        fecha_calculo = CURRENT_TIMESTAMP
                    WHERE id_detalle = v_id_detalle;

                    IF SQL%ROWCOUNT = 0 THEN
                        SELECT NVL(MAX(id_nota_definitiva), 0) + 1 INTO v_id_nota_def FROM NotaDefinitiva;
                        BEGIN
                            INSERT INTO NotaDefinitiva (id_nota_definitiva, id_detalle, nota_definitiva, fecha_calculo, cerrada)
                            VALUES (v_id_nota_def, v_id_detalle, v_nota_definitiva, CURRENT_TIMESTAMP, 0);
                        EXCEPTION
                            WHEN DUP_VAL_ON_INDEX THEN
                                UPDATE NotaDefinitiva
                                SET nota_definitiva = v_nota_definitiva,
                                    fecha_calculo = CURRENT_TIMESTAMP
                                WHERE id_detalle = v_id_detalle;
                        END;
                    END IF;

                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        BEGIN
                            DELETE FROM NotaDefinitiva WHERE id_detalle = v_id_detalle;
                        EXCEPTION WHEN OTHERS THEN NULL;
                        END;
                    WHEN OTHERS THEN
                        -- In statement-level processing we rethrow so the caller sees the error
                        RAISE;
                END;

                -- move to next key
                key := g_id_set.NEXT(key);
            END LOOP;

        -- clear the collection for next execution
        -- reinitialize the associative array to empty
        g_id_set := t_id_set();
    END process_and_clear;
END pkg_trg_nota;
-- Row-level trigger: collect affected id_detalle values into package collection
CREATE OR REPLACE TRIGGER trg_actualizar_nota_detalle_row
    AFTER INSERT OR UPDATE OR DELETE ON Calificacion
    FOR EACH ROW
BEGIN
    IF INSERTING OR UPDATING THEN
        pkg_trg_nota.add_det(:NEW.id_detalle);
    ELSIF DELETING THEN
        pkg_trg_nota.add_det(:OLD.id_detalle);
    END IF;
END trg_actualizar_nota_detalle_row;
-- Statement-level trigger: process collected ids and update NotaDefinitiva
CREATE OR REPLACE TRIGGER trg_actualizar_nota_detalle_stmt
    AFTER INSERT OR UPDATE OR DELETE ON Calificacion
BEGIN
    pkg_trg_nota.process_and_clear;
END trg_actualizar_nota_detalle_stmt;
-- Prevent modifications to Calificacion if NotaDefinitiva is closed (cerrada = 1)
CREATE OR REPLACE TRIGGER trg_prevent_modify_when_closed
    BEFORE INSERT OR UPDATE OR DELETE ON Calificacion
    FOR EACH ROW
DECLARE
    v_id_detalle PLS_INTEGER;
    v_cerrada NUMBER;
BEGIN
    IF INSERTING OR UPDATING THEN
        v_id_detalle := :NEW.id_detalle;
    ELSIF DELETING THEN
        v_id_detalle := :OLD.id_detalle;
    END IF;

    IF v_id_detalle IS NOT NULL THEN
        SELECT cerrada INTO v_cerrada FROM NotaDefinitiva WHERE id_detalle = v_id_detalle AND ROWNUM = 1;
        IF v_cerrada = 1 THEN
            RAISE_APPLICATION_ERROR(-20020, 'La nota definitiva está cerrada para esta inscripción; no se permiten modificaciones.');
        END IF;
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        NULL; -- no final grade record, allow operation
END trg_prevent_modify_when_closed;


-- =============================================
-- TRIGGER 4: Verificar limite de creditos en base de nivel de riesgo
-- =============================================
CREATE OR REPLACE TRIGGER trg_limit_credits_by_risk
    BEFORE INSERT OR UPDATE ON DetalleMatricula
    FOR EACH ROW
DECLARE
    v_cod_estudiante    Estudiante.cod_estudiante%TYPE;
    v_nivel             Estudiante.nivel_riesgo%TYPE;
    v_allowed           NUMBER;
    v_existing_credits  NUMBER := 0;
    v_group_credits     NUMBER := 0;
BEGIN
    -- Get credits of the group being enrolled
    SELECT a.creditos
    INTO v_group_credits
    FROM Grupo g JOIN Asignatura a ON g.cod_asignatura = a.cod_asignatura
    WHERE g.id_grupo = :NEW.id_grupo;

    -- Sum existing credits for this matricula, excluding the current detail if updating
    SELECT NVL(SUM(a.creditos), 0)
    INTO v_existing_credits
    FROM DetalleMatricula dm
             JOIN Grupo g2 ON dm.id_grupo = g2.id_grupo
             JOIN Asignatura a ON g2.cod_asignatura = a.cod_asignatura
    WHERE dm.id_matricula = :NEW.id_matricula
      AND ( :NEW.id_detalle IS NULL OR dm.id_detalle != :NEW.id_detalle );

    -- Determine student's code from Matricula
    SELECT m.cod_estudiante
    INTO v_cod_estudiante
    FROM Matricula m
    WHERE m.id_matricula = :NEW.id_matricula;

    -- Get student's risk level
    SELECT e.nivel_riesgo
    INTO v_nivel
    FROM Estudiante e
    WHERE e.cod_estudiante = v_cod_estudiante;

    -- Map allowed credits according to risk level
    IF v_nivel IS NULL THEN
        v_allowed := 21; -- default no risk
    ELSIF v_nivel = 0 THEN
        v_allowed := 21;
    ELSIF v_nivel = 1 OR v_nivel = 3 THEN
        v_allowed := 8;
    ELSIF v_nivel = 2 THEN
        v_allowed := 12;
    ELSIF v_nivel = 4 THEN
        v_allowed := 16;
    ELSE
        v_allowed := 21;
    END IF;

    -- New total if this enrollment proceeds
    IF (v_existing_credits + v_group_credits) > v_allowed THEN
        RAISE_APPLICATION_ERROR(-20032, 'Límite de créditos excedido para nivel de riesgo ' || NVL(TO_CHAR(v_nivel),'0')
            || '. Permitidos: ' || v_allowed || ', intentados: ' || (v_existing_credits + v_group_credits));
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20031, 'Datos insuficientes para validar límite de créditos.');
    WHEN OTHERS THEN
        -- Propagate unexpected errors with context
        RAISE_APPLICATION_ERROR(-20033, 'Error validando límite de créditos: ' || SQLERRM);
END trg_limit_credits_by_risk;

-- =============================================
-- TRIGGER 5: Verificar que un docente no exceda 16 horas semanales en el mismo periodo
-- =============================================
CREATE OR REPLACE TRIGGER trg_verificar_horas_docencia_semanal
    BEFORE INSERT OR UPDATE ON DocenteGrupo
    FOR EACH ROW
DECLARE
    v_cod_periodo      Grupo.cod_periodo%TYPE;
    v_horas_existentes NUMBER := 0;
    v_horas_nuevas     DocenteGrupo.horas_grupo%TYPE := :NEW.horas_grupo;
BEGIN
    -- Obtener el periodo del grupo al que se está vinculando
    SELECT g.cod_periodo
    INTO v_cod_periodo
    FROM Grupo g
    WHERE g.id_grupo = :NEW.id_grupo;

    -- Sumar las horas ya asignadas al docente en ese periodo, excluyendo
    -- el propio registro cuando se trata de un UPDATE
    SELECT NVL(SUM(dg.horas_grupo), 0)
    INTO v_horas_existentes
    FROM DocenteGrupo dg
    JOIN Grupo g2 ON dg.id_grupo = g2.id_grupo
    WHERE dg.id_docente = :NEW.id_docente
      AND g2.cod_periodo = v_cod_periodo
      AND ( :NEW.id_docente_grupo IS NULL OR dg.id_docente_grupo != :NEW.id_docente_grupo );

    -- Validar límite de 16 horas semanales
    IF (v_horas_existentes + v_horas_nuevas) > 16 THEN
        RAISE_APPLICATION_ERROR(-20040,
            'Asignación inválida: el docente (id=' || :NEW.id_docente || ') excedería 16 horas semanales en el periodo ' || v_cod_periodo ||
            '. Horas actuales: ' || v_horas_existentes || ', horas intentadas: ' || (v_horas_existentes + v_horas_nuevas));
    END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20041, 'No se encontró el grupo para validar el periodo o datos incompletos.');
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20042, 'Error validando horas de docencia: ' || SQLERRM);
END trg_verificar_horas_docencia_semanal;
