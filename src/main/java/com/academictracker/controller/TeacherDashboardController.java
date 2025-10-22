package com.academictracker.controller;

import com.academictracker.dao.DocenteGrupoDAO;
import com.academictracker.dao.TeacherDAO;
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

    private final DocenteGrupoDAO docenteGrupoDAO;
    private final TeacherDAO teacherDAO;
    private Long currentTeacherId;

    public TeacherDashboardController() {
        this.docenteGrupoDAO = new DocenteGrupoDAO();
        this.teacherDAO = new TeacherDAO();
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
                loadTeacherGroups();
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

            logger.info("Loaded {} groups for teacher ID {}", groups.size(), currentTeacherId);
        } catch (SQLException e) {
            logger.error("Error loading teacher groups", e);
            showAlert("Error", "Failed to load your assigned groups: " + e.getMessage());
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
