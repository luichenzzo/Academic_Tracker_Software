package com.academictracker.controller;

import com.academictracker.model.*;
import com.academictracker.service.AcademicService;
import com.academictracker.service.UserService;
import com.academictracker.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Admin Dashboard
 */
public class AdminDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;
    
    // Student Management
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Long> studentIdColumn;
    @FXML private TableColumn<Student, String> studentFirstNameColumn;
    @FXML private TableColumn<Student, String> studentLastNameColumn;
    @FXML private TableColumn<Student, String> studentPhoneColumn;
    
    // Teacher Management
    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, Long> teacherIdColumn;
    @FXML private TableColumn<Teacher, String> teacherFirstNameColumn;
    @FXML private TableColumn<Teacher, String> teacherLastNameColumn;
    @FXML private TableColumn<Teacher, String> teacherDepartmentColumn;
    
    // Program Management
    @FXML private TableView<Program> programTable;
    @FXML private TableColumn<Program, Long> programIdColumn;
    @FXML private TableColumn<Program, String> programCodeColumn;
    @FXML private TableColumn<Program, String> programNameColumn;
    @FXML private TableColumn<Program, Integer> programDurationColumn;
    
    // Course Management
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, Long> courseIdColumn;
    @FXML private TableColumn<Course, String> courseCodeColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, Integer> courseCreditsColumn;
    
    private final UserService userService;
    private final AcademicService academicService;
    
    public AdminDashboardController() {
        this.userService = new UserService();
        this.academicService = new AcademicService();
    }
    
    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Admin)");
        
        setupTables();
        loadData();
    }
    
    private void setupTables() {
        // Student table
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        studentLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        studentPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Teacher table
        teacherIdColumn.setCellValueFactory(new PropertyValueFactory<>("teacherId"));
        teacherFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        teacherLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        teacherDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        
        // Program table
        programIdColumn.setCellValueFactory(new PropertyValueFactory<>("programId"));
        programCodeColumn.setCellValueFactory(new PropertyValueFactory<>("programCode"));
        programNameColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        programDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationYears"));
        
        // Course table
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCreditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
    }
    
    private void loadData() {
        loadStudents();
        loadTeachers();
        loadPrograms();
        loadCourses();
    }
    
    private void loadStudents() {
        try {
            ObservableList<Student> students = FXCollections.observableArrayList(academicService.getAllStudents());
            studentTable.setItems(students);
        } catch (Exception e) {
            logger.error("Error loading students", e);
            showAlert("Error", "Failed to load students: " + e.getMessage());
        }
    }
    
    private void loadTeachers() {
        try {
            ObservableList<Teacher> teachers = FXCollections.observableArrayList(academicService.getAllTeachers());
            teacherTable.setItems(teachers);
        } catch (Exception e) {
            logger.error("Error loading teachers", e);
            showAlert("Error", "Failed to load teachers: " + e.getMessage());
        }
    }
    
    private void loadPrograms() {
        try {
            ObservableList<Program> programs = FXCollections.observableArrayList(academicService.getAllPrograms());
            programTable.setItems(programs);
        } catch (Exception e) {
            logger.error("Error loading programs", e);
            showAlert("Error", "Failed to load programs: " + e.getMessage());
        }
    }
    
    private void loadCourses() {
        try {
            ObservableList<Course> courses = FXCollections.observableArrayList(academicService.getAllCourses());
            courseTable.setItems(courses);
        } catch (Exception e) {
            logger.error("Error loading courses", e);
            showAlert("Error", "Failed to load courses: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Success", "Data refreshed successfully");
    }
    
    @FXML
    private void handleLogout() {
        userService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Academic Tracker - Login");
            stage.setScene(scene);
            stage.setResizable(false);
        } catch (Exception e) {
            logger.error("Error during logout", e);
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
