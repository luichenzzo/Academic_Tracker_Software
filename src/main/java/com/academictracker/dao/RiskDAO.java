package com.academictracker.dao;

import com.academictracker.util.DatabaseConnection;

import java.sql.*;

public class RiskDAO {

    public RiskDAO() {}

    /**
     * Calls the PROC_EVALUAR_RIESGOS stored procedure and returns the number updated, expelled and run timestamp.
     */
    public RiskRunResult evaluateRisks() throws SQLException {
        String call = "{ call PROC_EVALUAR_RIESGOS(?, ?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(call)) {

            cs.registerOutParameter(1, Types.TIMESTAMP);
            cs.registerOutParameter(2, Types.NUMERIC);
            cs.registerOutParameter(3, Types.NUMERIC);

            cs.execute();

            Timestamp ts = cs.getTimestamp(1);
            int updated = cs.getInt(2);
            int expelled = cs.getInt(3);

            return new RiskRunResult(ts, updated, expelled);
        }
    }

    public static class RiskRunResult {
        public final Timestamp runTimestamp;
        public final int updatedCount;
        public final int expelledCount;

        public RiskRunResult(Timestamp runTimestamp, int updatedCount, int expelledCount) {
            this.runTimestamp = runTimestamp;
            this.updatedCount = updatedCount;
            this.expelledCount = expelledCount;
        }
    }
}
