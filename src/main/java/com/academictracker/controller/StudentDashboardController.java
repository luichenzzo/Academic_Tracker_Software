package com.academictracker.controller;

import com.academictracker.model.User;
import com.academictracker.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for Student Dashboard
 */
public class StudentDashboardController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Student)");
    }
}
