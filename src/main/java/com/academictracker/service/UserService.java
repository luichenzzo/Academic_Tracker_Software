package com.academictracker.service;

import com.academictracker.dao.UserDAO;
import com.academictracker.model.User;
import com.academictracker.util.PasswordUtil;
import com.academictracker.util.SessionManager;
import com.academictracker.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for User authentication and management
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticate user and create session
     */
    public boolean login(String username, String password) {
        try {
            if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
                logger.warn("Login attempt with empty credentials");
                return false;
            }

            Optional<User> userOpt = userDAO.findByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("Login attempt with non-existent username: {}", username);
                return false;
            }

            User user = userOpt.get();
            if (!user.isActive()) {
                logger.warn("Login attempt with inactive account: {}", username);
                return false;
            }

            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                SessionManager.getInstance().setCurrentUser(user);
                logger.info("User logged in: {}", username);
                return true;
            }

            logger.warn("Login attempt with incorrect password for user: {}", username);
            return false;
        } catch (SQLException e) {
            logger.error("Error during login: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Logout current user
     */
    public void logout() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
        }
        SessionManager.getInstance().logout();
    }

    /**
     * Create new user with validation
     */
    public User createUser(String username, String password, User.UserRole role, String email) throws Exception {
        // Validate inputs
        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username format. Use 3-50 alphanumeric characters or underscore.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        // Check if username already exists
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // Create user
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(role);
        user.setEmail(email);
        user.setActive(true);

        return userDAO.create(user);
    }

    /**
     * Update user
     */
    public void updateUser(User user) throws Exception {
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        userDAO.update(user);
        logger.info("User updated: {}", user.getUsername());
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) throws Exception {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        if (!ValidationUtil.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.update(user);
        logger.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Delete user
     */
    public void deleteUser(Long userId) throws SQLException {
        userDAO.delete(userId);
        logger.info("User deleted: {}", userId);
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long userId) throws SQLException {
        return userDAO.findById(userId);
    }

    /**
     * Get users by role
     */
    public List<User> getUsersByRole(User.UserRole role) throws SQLException {
        return userDAO.findByRole(role);
    }

    /**
     * Check if current user has admin role
     */
    public boolean isCurrentUserAdmin() {
        return SessionManager.getInstance().isAdmin();
    }

    /**
     * Check if current user has teacher role
     */
    public boolean isCurrentUserTeacher() {
        return SessionManager.getInstance().isTeacher();
    }

    /**
     * Check if current user has student role
     */
    public boolean isCurrentUserStudent() {
        return SessionManager.getInstance().isStudent();
    }
}
