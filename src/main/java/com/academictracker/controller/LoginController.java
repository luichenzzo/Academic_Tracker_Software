package com.academictracker.controller;

import com.academictracker.model.User;
import com.academictracker.service.UserService;
import com.academictracker.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for login screen
 */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    private final UserService userService;
    
    public LoginController() {
        this.userService = new UserService();
    }
    
    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        
        boolean success = userService.login(username, password);
        
        if (success) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            logger.info("Login successful for user: {} with role: {}", username, currentUser.getRole());
            
            try {
                // Load appropriate dashboard based on role
                String fxmlFile = switch (currentUser.getRole()) {
                    case ADMIN -> "/fxml/admin-dashboard.fxml";
                    case TEACHER -> "/fxml/teacher-dashboard.fxml";
                    case STUDENT -> "/fxml/student-dashboard.fxml";
                };
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load(), 1000, 700);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setTitle("Academic Tracker - " + currentUser.getRole() + " Dashboard");
                stage.setScene(scene);
                stage.setMaximized(true);
                
            } catch (Exception e) {
                logger.error("Error loading dashboard: " + e.getMessage(), e);
                showError("Error loading dashboard");
            }
        } else {
            showError("Invalid username or password");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
