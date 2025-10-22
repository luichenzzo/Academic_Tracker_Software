package com.academictracker.util;

import com.academictracker.dao.ProgramDAO;
import com.academictracker.dao.SedeDAO;
import com.academictracker.model.Program;
import com.academictracker.model.Sede;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * Simple database diagnostic tool to test connection and data retrieval
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("=== DATABASE CONNECTION TEST ===\n");

        // Test 1: Basic connection
        testConnection();

        // Test 2: Count programs in database
        testProgramCount();

        // Test 3: Fetch programs via DAO
        testProgramDAO();

        // Test 4: Count sedes in database
        testSedeCount();

        // Test 5: Fetch sedes via DAO
        testSedeDAO();
    }

    private static void testConnection() {
        System.out.println("Test 1: Testing database connection...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection successful!");
                System.out.println("  Database: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("  Version: " + conn.getMetaData().getDatabaseProductVersion());
                System.out.println("  URL: " + conn.getMetaData().getURL());
            } else {
                System.out.println("✗ Connection is null or closed!");
            }
        } catch (Exception e) {
            System.out.println("✗ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testProgramCount() {
        System.out.println("Test 2: Counting programs in ProgramaAcademico table...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM ProgramaAcademico")) {

            if (rs.next()) {
                int count = rs.getInt("total");
                System.out.println("✓ Found " + count + " programs in database");

                if (count == 0) {
                    System.out.println("  ⚠ WARNING: No programs found! The table is empty.");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Query failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testProgramDAO() {
        System.out.println("Test 3: Fetching programs via ProgramDAO...");
        try {
            ProgramDAO programDAO = new ProgramDAO();
            List<Program> programs = programDAO.findAll();

            System.out.println("✓ ProgramDAO returned " + programs.size() + " programs");

            if (programs.isEmpty()) {
                System.out.println("  ⚠ WARNING: No programs returned by DAO!");
            } else {
                System.out.println("  Programs found:");
                for (Program p : programs) {
                    System.out.println("    - [" + p.getCodigoPrograma() + "] " + p.getNombre());
                }
            }
        } catch (Exception e) {
            System.out.println("✗ DAO query failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testSedeCount() {
        System.out.println("Test 4: Counting sedes in Sede table...");
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM Sede")) {

            if (rs.next()) {
                int count = rs.getInt("total");
                System.out.println("✓ Found " + count + " sedes in database");

                if (count == 0) {
                    System.out.println("  ⚠ WARNING: No sedes found! The table is empty.");
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Query failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testSedeDAO() {
        System.out.println("Test 5: Fetching sedes via SedeDAO...");
        try {
            SedeDAO sedeDAO = new SedeDAO();
            List<Sede> sedes = sedeDAO.findAll();

            System.out.println("✓ SedeDAO returned " + sedes.size() + " sedes");

            if (sedes.isEmpty()) {
                System.out.println("  ⚠ WARNING: No sedes returned by DAO!");
            } else {
                System.out.println("  Sedes found:");
                for (Sede s : sedes) {
                    System.out.println("    - [" + s.getIdSede() + "] " + s.getNombre() + " - " + s.getMunicipio());
                }
            }
        } catch (Exception e) {
            System.out.println("✗ DAO query failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }
}

