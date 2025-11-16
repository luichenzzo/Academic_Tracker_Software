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
/

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
/

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
/

-- Statement-level trigger: process collected ids and update NotaDefinitiva
CREATE OR REPLACE TRIGGER trg_actualizar_nota_detalle_stmt
    AFTER INSERT OR UPDATE OR DELETE ON Calificacion
BEGIN
    pkg_trg_nota.process_and_clear;
END trg_actualizar_nota_detalle_stmt;
/

commit;





