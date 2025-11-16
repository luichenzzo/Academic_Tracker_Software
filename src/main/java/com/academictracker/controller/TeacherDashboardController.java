package com.academictracker.controller;

import com.academictracker.dao.DocenteGrupoDAO;
import com.academictracker.model.GradeDisplay;
import com.academictracker.dao.GradeDAO;
import com.academictracker.model.User;
import com.academictracker.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for Teacher Dashboard
 */
public class TeacherDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDashboardController.class);

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TableView<DocenteGrupoDAO.GroupAssignmentDetails> groupsTable;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> groupNumberColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> courseCodeColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> courseNameColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> periodColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> sedeColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> capacityColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> enrolledColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> hoursColumn;

    @FXML
    private TableColumn<DocenteGrupoDAO.GroupAssignmentDetails, String> isPrincipalColumn;

    // New: grades table and columns
    @FXML
    private TableView<GradeDisplay> gradesTable;

    @FXML
    private TableColumn<GradeDisplay, String> studentIdColumn;

    @FXML
    private TableColumn<GradeDisplay, String> studentNameColumn;

    @FXML
    private TableColumn<GradeDisplay, String> gradeColumn;

    @FXML
    private TableColumn<GradeDisplay, String> statusColumn;

    // New: combo to select group for grades
    @FXML
    private javafx.scene.control.ComboBox<DocenteGrupoDAO.GroupAssignmentDetails> groupCombo;

    private final DocenteGrupoDAO docenteGrupoDAO;
    private final GradeDAO gradeDAO = new GradeDAO();
    private Long currentTeacherId;

    public TeacherDashboardController() {
        this.docenteGrupoDAO = new DocenteGrupoDAO();
    }

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Teacher)");

        // Get the teacher ID from the current user
        try {
            String idReferencia = currentUser.getIdReferencia();
            if (idReferencia != null && !idReferencia.isEmpty()) {
                currentTeacherId = Long.parseLong(idReferencia);
                setupTable();
                setupGradeTable();
                setupGroupCombo();
                loadTeacherGroups();

                // When a group is selected in the table, load its grades
                groupsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        // Use course code and group number to identify group (adjust if you have a dedicated group id)
                        String courseCode = newSelection.codAsignatura;
                        Integer groupNumber = newSelection.numeroGrupo;
                        loadGradesForGroup(courseCode, groupNumber);
                    } else {
                        gradesTable.getItems().clear();
                    }
                });

                // When a group is selected in the combo, load its grades
                groupCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldG, newG) -> {
                    if (newG != null) {
                        loadGradesForGroup(newG.codAsignatura, newG.numeroGrupo);
                    }
                });
            } else {
                logger.error("Teacher ID reference not found in user session");
                showAlert("Error", "Could not identify teacher profile. Please contact administrator.");
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid teacher ID format: {}", currentUser.getIdReferencia(), e);
            showAlert("Error", "Invalid teacher ID format. Please contact administrator.");
        }
    }

    private void setupTable() {
        // Setup table columns
        groupNumberColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().numeroGrupo.toString()));

        courseCodeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().codAsignatura));

        courseNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().asignaturaNombre));

        periodColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().periodoNombre));

        sedeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().sedeNombre));

        capacityColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCapacityDisplay()));

        enrolledColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().cupoOcupado.toString()));

        hoursColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(String.format("%.1f", cellData.getValue().horasGrupo)));

        isPrincipalColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().esPrincipal ? "Yes" : "No"));
    }

    // New: configure grade table columns
    private void setupGradeTable() {
        studentIdColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStudentId()));

        studentNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStudentName()));

        gradeColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getGrade()));

        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    private void setupGroupCombo() {
        // Use a readable string format for the combo items
        groupCombo.setConverter(new javafx.util.StringConverter<DocenteGrupoDAO.GroupAssignmentDetails>() {
            @Override
            public String toString(DocenteGrupoDAO.GroupAssignmentDetails object) {
                if (object == null) return "";
                return String.format("%s - Grupo %d - %s", object.codAsignatura, object.numeroGrupo, object.periodoNombre);
            }

            @Override
            public DocenteGrupoDAO.GroupAssignmentDetails fromString(String string) {
                return null; // Not needed
            }
        });
    }

    private void loadTeacherGroups() {
        if (currentTeacherId == null) {
            return;
        }

        try {
            List<DocenteGrupoDAO.GroupAssignmentDetails> groups =
                docenteGrupoDAO.getGroupsForTeacherWithDetails(currentTeacherId);

            ObservableList<DocenteGrupoDAO.GroupAssignmentDetails> groupsList =
                FXCollections.observableArrayList(groups);

            groupsTable.setItems(groupsList);

            // Populate combo as well
            groupCombo.setItems(groupsList);

            logger.info("Loaded {} groups for teacher ID {}", groups.size(), currentTeacherId);
        } catch (SQLException e) {
            logger.error("Error loading teacher groups", e);
            showAlert("Error", "Failed to load your assigned groups: " + e.getMessage());
        }
    }

    // New: load grades for a selected group (courseCode + groupNumber)
    private void loadGradesForGroup(String courseCode, Integer groupNumber) {
        try {
            List<GradeDisplay> grades = gradeDAO.getGradesForGroup(courseCode, groupNumber);
            ObservableList<GradeDisplay> gradesList = FXCollections.observableArrayList(grades);
            gradesTable.setItems(gradesList);
            logger.info("Loaded {} grades for group {} - {}", grades.size(), courseCode, groupNumber);
        } catch (Exception e) {
            logger.error("Error loading grades for group", e);
            showAlert("Error", "Failed to load grades: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadTeacherGroups();
        showAlert("Success", "Groups refreshed successfully");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            URL cssUrl = getClass().getResource("/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                logger.warn("CSS resource /css/style.css not found; skipping stylesheet load");
            }

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
