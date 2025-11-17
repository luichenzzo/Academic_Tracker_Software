-- Archivo: views-reports.sql
-- Vistas para reportes solicitados (1..18)
-- Notas: algunas vistas usan supuestos razonables cuando no existe una tabla explícita
-- (por ejemplo, opiniones estudiantiles). Estos supuestos están indicados en los comentarios.

-- 1) Matrícula y carga por periodo
CREATE OR REPLACE VIEW vw_report_matricula_carga AS
SELECT
  p.codigo_programa,
  p.nombre AS programa,
  s.id_sede,
  s.nombre AS sede,
  g.cod_asignatura,
  a.nombre AS asignatura,
  g.cod_periodo AS periodo,
  NVL(COUNT(d.id_detalle),0) AS inscritos,
  -- Evitar funciones ventana dentro de agregados: calcular créditos como créditos por asignatura * inscritos
  (a.creditos * NVL(COUNT(d.id_detalle),0)) AS creditos_total_grupo,
  g.cupo_maximo,
  g.cupo_ocupado,
  ROUND(CASE WHEN g.cupo_maximo>0 THEN g.cupo_ocupado / g.cupo_maximo * 100 ELSE 0 END,2) AS porcentaje_ocupacion
FROM Grupo g
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
JOIN ProgramaAcademico p ON p.cod_programa = a.cod_programa
JOIN Sede s ON s.id_sede = g.id_sede
LEFT JOIN DetalleMatricula d ON d.id_grupo = g.id_grupo AND d.estado IN ('inscrito','confirmado')
GROUP BY ROLLUP (p.codigo_programa, p.nombre, s.id_sede, s.nombre, g.cod_asignatura, a.nombre, g.cod_periodo, g.id_grupo, g.cupo_maximo, g.cupo_ocupado, a.creditos);

-- 2) Ocupación y top grupos (incluye ranking por sede y periodo)
CREATE OR REPLACE VIEW vw_report_ocupacion_top_grupos AS
SELECT
  g.id_grupo,
  g.cod_periodo AS periodo,
  s.id_sede,
  s.nombre AS sede,
  a.cod_asignatura,
  a.nombre AS asignatura,
  g.cupo_maximo,
  g.cupo_ocupado,
  ROUND(CASE WHEN g.cupo_maximo>0 THEN g.cupo_ocupado / g.cupo_maximo * 100 ELSE 0 END,2) AS porcentaje_ocupacion,
  DENSE_RANK() OVER (PARTITION BY s.id_sede, g.cod_periodo ORDER BY CASE WHEN g.cupo_maximo>0 THEN g.cupo_ocupado / g.cupo_maximo ELSE 0 END DESC) AS posicion_sede_periodo
FROM Grupo g
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
JOIN Sede s ON s.id_sede = g.id_sede;

-- 3) Intentos fallidos de matrícula
-- Supuesto: DetalleMatricula.estado puede contener valores como 'bloqueado_horario' o 'bloqueado_cupo'.
CREATE OR REPLACE VIEW vw_report_intentos_fallidos_matricula AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  g.id_grupo,
  g.numero_grupo,
  SUM(CASE WHEN d.estado LIKE '%bloqueado%' THEN 1 ELSE 0 END) AS intentos_bloqueados_total,
  SUM(CASE WHEN d.estado = 'bloqueado_horario' THEN 1 ELSE 0 END) AS bloqueados_choque_horario,
  SUM(CASE WHEN d.estado = 'bloqueado_cupo' OR (g.cupo_ocupado>=g.cupo_maximo) THEN 1 ELSE 0 END) AS bloqueados_cupo_lleno
FROM DetalleMatricula d
JOIN Grupo g ON g.id_grupo = d.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
GROUP BY a.cod_asignatura, a.nombre, g.id_grupo, g.numero_grupo;

-- 4) Rendimiento por asignatura
CREATE OR REPLACE VIEW vw_report_rendimiento_asignatura AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  d_periodo.cod_periodo AS periodo,
  ROUND(AVG(nd.nota_definitiva),2) AS promedio,
  ROUND(MIN(nd.nota_definitiva),2) AS nota_minima,
  ROUND(MAX(nd.nota_definitiva),2) AS nota_maxima,
  ROUND(NVL(STDDEV(nd.nota_definitiva),0),2) AS desviacion_estandar,
  COUNT(nd.id_nota_definitiva) AS total_evaluaciones
FROM NotaDefinitiva nd
JOIN DetalleMatricula dm ON dm.id_detalle = nd.id_detalle
JOIN Grupo g ON g.id_grupo = dm.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
JOIN (SELECT DISTINCT cod_periodo FROM PeriodoAcademico) d_periodo ON d_periodo.cod_periodo = g.cod_periodo
GROUP BY a.cod_asignatura, a.nombre, d_periodo.cod_periodo;

-- 5) Distribución de notas (rangos: 0–2.9, 3.0–3.9, 4.0–5.0)
CREATE OR REPLACE VIEW vw_report_distribucion_notas AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  g.cod_periodo AS periodo,
  SUM(CASE WHEN nd.nota_definitiva BETWEEN 0 AND 2.99 THEN 1 ELSE 0 END) AS rango_0_2_9,
  SUM(CASE WHEN nd.nota_definitiva BETWEEN 3.0 AND 3.99 THEN 1 ELSE 0 END) AS rango_3_0_3_9,
  SUM(CASE WHEN nd.nota_definitiva BETWEEN 4.0 AND 5.0 THEN 1 ELSE 0 END) AS rango_4_0_5_0,
  COUNT(nd.id_nota_definitiva) AS total
FROM NotaDefinitiva nd
JOIN DetalleMatricula dm ON dm.id_detalle = nd.id_detalle
JOIN Grupo g ON g.id_grupo = dm.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
GROUP BY a.cod_asignatura, a.nombre, g.cod_periodo;

-- 6) Evolución de promedio por estudiante (promedio por periodo y variación respecto periodo anterior)
CREATE OR REPLACE VIEW vw_report_evolucion_promedio_estudiante AS
SELECT
  h.cod_estudiante,
  e.nombres || ' ' || e.apellidos AS estudiante,
  h.cod_periodo AS periodo,
  ROUND(h.promedio_periodo,2) AS promedio_periodo,
  LAG(ROUND(h.promedio_periodo,2)) OVER (PARTITION BY h.cod_estudiante ORDER BY h.cod_periodo) AS promedio_periodo_anterior,
  ROUND(h.promedio_periodo - LAG(h.promedio_periodo) OVER (PARTITION BY h.cod_estudiante ORDER BY h.cod_periodo),2) AS variacion_vs_anterior
FROM HistorialAcademico h
JOIN Estudiante e ON e.cod_estudiante = h.cod_estudiante;

-- 7) Riesgo académico por periodo (conteo por nivel de riesgo, programa y periodo)
CREATE OR REPLACE VIEW vw_report_riesgo_academico_periodo AS
SELECT
  ha.cod_periodo AS periodo,
  p.cod_programa,
  p.nombre AS programa,
  ha.nivel_riesgo_calculado AS nivel_riesgo,
  COUNT(DISTINCT ha.cod_estudiante) AS cantidad_estudiantes
FROM HistorialAcademico ha
JOIN Estudiante est ON est.cod_estudiante = ha.cod_estudiante
JOIN ProgramaAcademico p ON p.cod_programa = est.cod_programa
GROUP BY ha.cod_periodo, p.cod_programa, p.nombre, ha.nivel_riesgo_calculado;

-- 8) Intentos por asignatura: tasa de aprobación según número de intentos
CREATE OR REPLACE VIEW vw_report_intentos_por_asignatura AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  dm.numero_intento,
  COUNT(CASE WHEN nd.nota_definitiva >= 3.0 THEN 1 END) AS aprobados,
  COUNT(nd.id_nota_definitiva) AS total_presentaciones,
  ROUND( CASE WHEN COUNT(nd.id_nota_definitiva)>0 THEN COUNT(CASE WHEN nd.nota_definitiva >= 3.0 THEN 1 END) / COUNT(nd.id_nota_definitiva) * 100 ELSE 0 END,2) AS tasa_aprobacion_pct
FROM DetalleMatricula dm
LEFT JOIN NotaDefinitiva nd ON nd.id_detalle = dm.id_detalle
JOIN Grupo g ON g.id_grupo = dm.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
GROUP BY a.cod_asignatura, a.nombre, dm.numero_intento;

-- 9) Trayectoria por cohorte (porcentaje cursado oportunamente vs con atraso)
-- Supuesto: Estudiante.fecha_ingreso existe (cohorte = año de ingreso)
CREATE OR REPLACE VIEW vw_report_trayectoria_cohorte AS
SELECT
  TO_CHAR(e.fecha_ingreso,'YYYY') AS cohorte,
  a.cod_asignatura,
  a.nombre AS asignatura,
  SUM(CASE WHEN dm.es_repeticion_calculado = 0 THEN 1 ELSE 0 END) AS oportunas,
  SUM(CASE WHEN dm.es_repeticion_calculado = 1 THEN 1 ELSE 0 END) AS con_atraso,
  COUNT(dm.id_detalle) AS total_cursadas,
  ROUND( CASE WHEN COUNT(dm.id_detalle)>0 THEN SUM(CASE WHEN dm.es_repeticion_calculado = 0 THEN 1 ELSE 0 END) / COUNT(dm.id_detalle) * 100 ELSE 0 END,2) AS pct_oportunas
FROM DetalleMatricula dm
JOIN Matricula m ON m.id_matricula = dm.id_matricula
JOIN Estudiante e ON e.cod_estudiante = m.cod_estudiante
JOIN Grupo g ON g.id_grupo = dm.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
GROUP BY TO_CHAR(e.fecha_ingreso,'YYYY'), a.cod_asignatura, a.nombre;

-- 10) Mapa de prerrequisitos (jerarquía desde una asignatura raíz)
-- Esta vista lista relaciones padre->hijo con nivel (distancia desde la raíz)
CREATE OR REPLACE VIEW vw_report_mapa_prerrequisitos AS
WITH prereq (raiz, cod_asignatura, cod_requisito, nivel) AS (
  SELECT r.cod_asignatura AS raiz, r.cod_asignatura_requerida AS cod_asignatura, r.cod_asignatura_requerida AS cod_requisito, 1 FROM RequisitoAsignatura r
  UNION ALL
  SELECT p.raiz, r.cod_asignatura_requerida, r.cod_asignatura_requerida, p.nivel+1
  FROM RequisitoAsignatura r
  JOIN prereq p ON r.cod_asignatura = p.cod_asignatura
)
SELECT DISTINCT raiz AS asignatura_raiz, cod_asignatura AS asignatura, cod_requisito AS prerequisito, nivel
FROM prereq;

-- 11) Impacto de prerrequisitos: relación entre aprobación de prerrequisitos y reprobación en la asignatura objetivo
CREATE OR REPLACE VIEW vw_report_impacto_prerrequisitos AS
SELECT
  req.cod_asignatura AS asignatura_objetivo,
  req.cod_asignatura_requerida AS prerrequisito,
  COUNT(CASE WHEN nd.nota_definitiva < 3.0 THEN 1 END) AS reprobados_objetivo,
  COUNT(CASE WHEN nd_req.nota_definitiva >= 3.0 THEN 1 END) AS prerrequisito_aprobado_count,
  COUNT(*) AS total_registros,
  ROUND( CASE WHEN COUNT(*)>0 THEN COUNT(CASE WHEN nd.nota_definitiva < 3.0 THEN 1 END) / COUNT(*) * 100 ELSE 0 END,2) AS pct_reprobacion_objetivo
FROM RequisitoAsignatura req
LEFT JOIN Grupo g_dest ON g_dest.cod_asignatura = req.cod_asignatura
LEFT JOIN DetalleMatricula dm ON dm.id_grupo = g_dest.id_grupo
LEFT JOIN NotaDefinitiva nd ON nd.id_detalle = dm.id_detalle
-- intento de asociar nota del prerrequisito para el mismo estudiante en periodos anteriores
LEFT JOIN Grupo g_req ON g_req.cod_asignatura = req.cod_asignatura_requerida
LEFT JOIN DetalleMatricula dm_req ON dm_req.id_matricula = dm.id_matricula AND dm_req.id_grupo = g_req.id_grupo
LEFT JOIN NotaDefinitiva nd_req ON nd_req.id_detalle = dm_req.id_detalle
GROUP BY req.cod_asignatura, req.cod_asignatura_requerida;

-- 12) Reglas de evaluación incompletas
CREATE OR REPLACE VIEW vw_report_reglas_evaluacion_incompletas AS
SELECT
  g.id_grupo,
  a.cod_asignatura,
  a.nombre AS asignatura,
  g.cod_periodo,
  NVL(SUM(r.porcentaje),0) AS suma_porcentajes,
  COUNT(r.id_regla) AS numero_items
FROM Grupo g
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
LEFT JOIN ReglaEvaluacion r ON r.id_grupo = g.id_grupo
GROUP BY g.id_grupo, a.cod_asignatura, a.nombre, g.cod_periodo
HAVING NVL(SUM(r.porcentaje),0) <> 100 OR COUNT(r.id_regla) = 0;

-- 13) Reprobación por ítem de evaluación
CREATE OR REPLACE VIEW vw_report_reprobacion_por_item AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  r.id_regla,
  r.nombre_item,
  ROUND(AVG(c.nota),2) AS promedio_por_item,
  COUNT(c.id_calificacion) AS total_calificaciones,
  ROUND( CASE WHEN COUNT(nd.id_nota_definitiva)>0 THEN SUM(CASE WHEN nd.nota_definitiva<3.0 THEN 1 ELSE 0 END) / COUNT(nd.id_nota_definitiva) * 100 ELSE 0 END,2) AS pct_reprobacion_final
FROM Calificacion c
JOIN ReglaEvaluacion r ON r.id_regla = c.id_regla
JOIN DetalleMatricula dm ON dm.id_detalle = c.id_detalle
JOIN Grupo g ON g.id_grupo = dm.id_grupo
JOIN Asignatura a ON a.cod_asignatura = g.cod_asignatura
LEFT JOIN NotaDefinitiva nd ON nd.id_detalle = dm.id_detalle
GROUP BY a.cod_asignatura, a.nombre, r.id_regla, r.nombre_item;

-- 14) Avance en créditos vs plan
CREATE OR REPLACE VIEW vw_report_avance_creditos_vs_plan AS
SELECT
  e.cod_estudiante,
  e.nombres || ' ' || e.apellidos AS estudiante,
  p.cod_programa,
  p.nombre AS programa,
  p.creditos_totales AS creditos_plan,
  COALESCE((SELECT SUM(h.creditos_aprobados) FROM HistorialAcademico h WHERE h.cod_estudiante = e.cod_estudiante),0) AS creditos_aprobados_acumulados,
  ROUND( CASE WHEN p.creditos_totales>0 THEN COALESCE((SELECT SUM(h.creditos_aprobados) FROM HistorialAcademico h WHERE h.cod_estudiante = e.cod_estudiante),0) / p.creditos_totales * 100 ELSE 0 END,2) AS pct_avance,
  CASE
    WHEN ROUND( CASE WHEN p.creditos_totales>0 THEN COALESCE((SELECT SUM(h.creditos_aprobados) FROM HistorialAcademico h WHERE h.cod_estudiante = e.cod_estudiante),0) / p.creditos_totales * 100 ELSE 0 END,2) >= 100 THEN 'Completado'
    WHEN ROUND( CASE WHEN p.creditos_totales>0 THEN COALESCE((SELECT SUM(h.creditos_aprobados) FROM HistorialAcademico h WHERE h.cod_estudiante = e.cod_estudiante),0) / p.creditos_totales * 100 ELSE 0 END,2) >= 75 THEN 'Al día'
    WHEN ROUND( CASE WHEN p.creditos_totales>0 THEN COALESCE((SELECT SUM(h.creditos_aprobados) FROM HistorialAcademico h WHERE h.cod_estudiante = e.cod_estudiante),0) / p.creditos_totales * 100 ELSE 0 END,2) >= 50 THEN 'Atraso leve'
    ELSE 'Atraso severo'
  END AS clasificacion_atraso
FROM Estudiante e
JOIN ProgramaAcademico p ON p.cod_programa = e.cod_programa;

-- 15) Opinión estudiantil consolidada
-- Nota: la tabla OpinionEstudiantil no existe en el esquema actual. Para evitar errores DDL creamos una vista vacía con la firma esperada.
CREATE OR REPLACE VIEW vw_report_opinion_consolidada AS
SELECT
  CAST(NULL AS VARCHAR2(15)) AS cod_asignatura,
  CAST(NULL AS VARCHAR2(150)) AS asignatura,
  CAST(NULL AS VARCHAR2(10)) AS periodo,
  0 AS total_comentarios,
  0 AS positivos,
  0 AS negativos,
  0 AS pct_positivos
FROM DUAL
WHERE 1 = 0; -- estructura vacía; reemplazar cuando exista la tabla real

-- 16) Cruce de opiniones y desempeño
-- Vista segura vacía mientras no exista OpinionEstudiantil
CREATE OR REPLACE VIEW vw_report_cruce_opiniones_desempeno AS
SELECT
  CAST(NULL AS VARCHAR2(15)) AS cod_asignatura,
  CAST(NULL AS VARCHAR2(150)) AS asignatura,
  CAST(NULL AS VARCHAR2(10)) AS periodo,
  0 AS pct_comentarios_negativos,
  0 AS pct_reprobacion
FROM DUAL
WHERE 1 = 0;

-- 17) Asignaturas "cuello de botella"
-- Definición: alta tasa de reprobación (>30%) y alta frecuencia de atraso (pct_oportunas < 60)
CREATE OR REPLACE VIEW vw_report_cuello_botella AS
SELECT
  a.cod_asignatura,
  a.nombre AS asignatura,
  ROUND( CASE WHEN COUNT(nd.id_nota_definitiva)>0 THEN SUM(CASE WHEN nd.nota_definitiva < 3.0 THEN 1 ELSE 0 END) / COUNT(nd.id_nota_definitiva) * 100 ELSE 0 END,2) AS pct_reprobacion,
  COALESCE(t.pct_oportunas,0) AS pct_oportunas
FROM Asignatura a
LEFT JOIN Grupo g ON g.cod_asignatura = a.cod_asignatura
LEFT JOIN DetalleMatricula d ON d.id_grupo = g.id_grupo
LEFT JOIN NotaDefinitiva nd ON nd.id_detalle = d.id_detalle
LEFT JOIN (
  SELECT a.cod_asignatura, ROUND( CASE WHEN COUNT(dm.id_detalle)>0 THEN SUM(CASE WHEN dm.es_repeticion_calculado = 0 THEN 1 ELSE 0 END) / COUNT(dm.id_detalle) * 100 ELSE 0 END,2) AS pct_oportunas
  FROM DetalleMatricula dm
  JOIN Grupo g2 ON g2.id_grupo = dm.id_grupo
  JOIN Asignatura a ON a.cod_asignatura = g2.cod_asignatura
  GROUP BY a.cod_asignatura
) t ON t.cod_asignatura = a.cod_asignatura
GROUP BY a.cod_asignatura, a.nombre, t.pct_oportunas
HAVING ROUND( CASE WHEN COUNT(nd.id_nota_definitiva)>0 THEN SUM(CASE WHEN nd.nota_definitiva < 3.0 THEN 1 ELSE 0 END) / COUNT(nd.id_nota_definitiva) * 100 ELSE 0 END,2) > 30 AND COALESCE(t.pct_oportunas,0) < 60;

-- 18) Calidad de datos: porcentajes de campos nulos o inválidos en registros clave (por tabla y periodo)
-- Suponemos periodo asociado a algunas tablas (por ejemplo Grupo.cod_periodo, HistorialAcademico.cod_periodo)
CREATE OR REPLACE VIEW vw_report_calidad_datos AS
SELECT 'Estudiante' AS tabla,
  COUNT(*) AS total_registros,
  ROUND(SUM(CASE WHEN numero_documento IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2) AS pct_numero_documento_nulo,
  ROUND(SUM(CASE WHEN correo_institucional IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2) AS pct_correo_nulo
FROM Estudiante
UNION ALL
SELECT 'Grupo', COUNT(*),
  ROUND(SUM(CASE WHEN cod_asignatura IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2),
  ROUND(SUM(CASE WHEN id_sede IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2)
FROM Grupo
UNION ALL
SELECT 'DetalleMatricula', COUNT(*),
  ROUND(SUM(CASE WHEN id_matricula IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2),
  ROUND(SUM(CASE WHEN id_grupo IS NULL THEN 1 ELSE 0 END) / COUNT(*) * 100,2)
FROM DetalleMatricula;
