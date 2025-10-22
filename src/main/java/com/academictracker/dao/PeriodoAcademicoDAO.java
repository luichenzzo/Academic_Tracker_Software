package com.academictracker.dao;

import com.academictracker.model.PeriodoAcademico;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for PeriodoAcademico (Academic Periods)
 */
public class PeriodoAcademicoDAO {
    private static final Logger logger = LoggerFactory.getLogger(PeriodoAcademicoDAO.class);

    public List<PeriodoAcademico> findAll() throws SQLException {
        String sql = "SELECT * FROM PeriodoAcademico ORDER BY fecha_inicio DESC";
        List<PeriodoAcademico> periodos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                periodos.add(mapResultSetToPeriodo(rs));
            }
        }
        return periodos;
    }

    public Optional<PeriodoAcademico> findById(String codPeriodo) throws SQLException {
        String sql = "SELECT * FROM PeriodoAcademico WHERE cod_periodo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codPeriodo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPeriodo(rs));
            }
            return Optional.empty();
        }
    }

    private PeriodoAcademico mapResultSetToPeriodo(ResultSet rs) throws SQLException {
        PeriodoAcademico periodo = new PeriodoAcademico();
        periodo.setCodPeriodo(rs.getString("cod_periodo"));
        periodo.setNombre(rs.getString("nombre"));

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            periodo.setFechaInicio(fechaInicio.toLocalDate());
        }

        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            periodo.setFechaFin(fechaFin.toLocalDate());
        }

        Date fechaInicioMatriculas = rs.getDate("fecha_inicio_matriculas");
        if (fechaInicioMatriculas != null) {
            periodo.setFechaInicioMatriculas(fechaInicioMatriculas.toLocalDate());
        }

        Date fechaFinMatriculas = rs.getDate("fecha_fin_matriculas");
        if (fechaFinMatriculas != null) {
            periodo.setFechaFinMatriculas(fechaFinMatriculas.toLocalDate());
        }

        Date fechaCierreNotas = rs.getDate("fecha_cierre_notas");
        if (fechaCierreNotas != null) {
            periodo.setFechaCierreNotas(fechaCierreNotas.toLocalDate());
        }

        periodo.setActivo(rs.getInt("activo") == 1);
        return periodo;
    }
}

