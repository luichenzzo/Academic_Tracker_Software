package com.academictracker.util;

import java.util.regex.Pattern;

/**
 * Validation utility class
 */
public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9]{10}$|^[0-9]{3}-[0-9]{3}-[0-9]{4}$"
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate that string is not empty
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        return value != null && value.length() >= minLength && value.length() <= maxLength;
    }

    /**
     * Validate numeric range
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * Validate username format (alphanumeric and underscore)
     */
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validate password strength (at least 6 characters)
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validate course code format
     */
    public static boolean isValidCourseCode(String code) {
        return code != null && code.matches("^[A-Z]{2,4}[0-9]{3,4}$");
    }

    /**
     * Validate program code format
     */
    public static boolean isValidProgramCode(String code) {
        return code != null && code.matches("^[A-Z]{2,6}$");
    }
}
