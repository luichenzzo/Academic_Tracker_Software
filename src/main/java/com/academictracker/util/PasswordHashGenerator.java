package com.academictracker.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility to generate password hashes for initial setup
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        // Generate hash for admin password
        String adminPassword = "admin123";
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt(10));
        
        System.out.println("=== Password Hash Generator ===");
        System.out.println();
        System.out.println("Admin Password: " + adminPassword);
        System.out.println("Admin Hash: " + adminHash);
        System.out.println();
        System.out.println("Use this hash in the schema.sql file for the default admin user.");
        System.out.println();
        
        // Verify it works
        boolean isValid = BCrypt.checkpw(adminPassword, adminHash);
        System.out.println("Verification test: " + (isValid ? "PASSED" : "FAILED"));
    }
}
