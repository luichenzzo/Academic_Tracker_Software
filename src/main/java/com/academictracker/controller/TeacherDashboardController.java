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

    @FXML
    private TableView<DocenteGrupoDAO.StudentRow> studentsTable;

    @FXML
    private TableColumn<DocenteGrupoDAO.StudentRow, String> studentTableIdCol;

    @FXML
    private TableColumn<DocenteGrupoDAO.StudentRow, String> studentTableNameCol;

    @FXML
    private TableView<com.academictracker.model.Grade> studentGradesTable;

    @FXML
    private TableColumn<com.academictracker.model.Grade, String> gradeIdCol;

    @FXML
    private TableColumn<com.academictracker.model.Grade, String> gradeValueCol;

    @FXML
    private TableColumn<com.academictracker.model.Grade, String> gradeRuleCol;

    @FXML
    private TableColumn<com.academictracker.model.Grade, String> gradeDateCol;

    @FXML
    private TableColumn<com.academictracker.model.Grade, String> gradeByCol;

    @FXML
    private javafx.scene.control.Button registerGradeButton;

    @FXML
    private javafx.scene.control.Button closeCourseButton;

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
                        // Sync combo selection
                        groupCombo.getSelectionModel().select(newSelection);
                        // loadGradesForGroup handled by combo listener
                    } else {
                        gradesTable.getItems().clear();
                    }
                });

                // When a group is selected in the combo, load its grades
                groupCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldG, newG) -> {
                    if (newG != null) {
                        // Sync table selection
                        groupsTable.getSelectionModel().select(newG);
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

        // Students table columns
        studentTableIdCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStudentId()));
        studentTableNameCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFullName()));

        // Student grades table columns
        gradeIdCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getGradeId() != null ? cellData.getValue().getGradeId().toString() : ""));
        gradeValueCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getGradeValue() != null ? String.format("%.2f", cellData.getValue().getGradeValue()) : ""));
        gradeRuleCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRuleId() != null ? cellData.getValue().getRuleId().toString() : ""));
        gradeDateCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRegisteredAt() != null ? cellData.getValue().getRegisteredAt().toString() : ""));
        gradeByCol.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getGradedBy() != null ? cellData.getValue().getGradedBy().toString() : ""));
    }

    private void setupGroupCombo() {
        // Use a readable string format for the combo items
        groupCombo.setConverter(new javafx.util.StringConverter<>() {
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
            if (!groupsList.isEmpty()) {
                groupCombo.getSelectionModel().selectFirst();
            }

            logger.info("Loaded {} groups for teacher ID {}", groups.size(), currentTeacherId);
        } catch (SQLException e) {
            logger.error("Error loading teacher groups", e);
            showAlert("Error", "Failed to load your assigned groups: " + e.getMessage());
        }
    }

    // New: load students for selected group (by group id)
    private void loadStudentsForGroup(DocenteGrupoDAO.GroupAssignmentDetails group) {
        if (group == null) {
            studentsTable.getItems().clear();
            studentGradesTable.getItems().clear();
            return;
        }

        try {
            List<DocenteGrupoDAO.StudentRow> students = docenteGrupoDAO.getStudentsForGroup(group.idGrupo);
            ObservableList<DocenteGrupoDAO.StudentRow> list = FXCollections.observableArrayList(students);
            studentsTable.setItems(list);

            // When a student is selected, load their grade history
            studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) {
                    try {
                        List<com.academictracker.model.Grade> grades = gradeDAO.findByEnrollment(newS.getEnrollmentId());
                        ObservableList<com.academictracker.model.Grade> gList = FXCollections.observableArrayList(grades);
                        studentGradesTable.setItems(gList);
                    } catch (SQLException e) {
                        logger.error("Error loading student grades", e);
                        showAlert("Error", "No se pudieron cargar las calificaciones del estudiante: " + e.getMessage());
                    }
                } else {
                    studentGradesTable.getItems().clear();
                }
            });

        } catch (SQLException e) {
            logger.error("Error loading students for group", e);
            showAlert("Error", "No se pudieron cargar los estudiantes del grupo: " + e.getMessage());
        }
    }

    // New: load grades for a selected group (courseCode + groupNumber)
    private void loadGradesForGroup(String courseCode, Integer groupNumber) {
        try {
            List<GradeDisplay> grades = gradeDAO.getGradesForGroup(courseCode, groupNumber);
            ObservableList<GradeDisplay> gradesList = FXCollections.observableArrayList(grades);
            gradesTable.setItems(gradesList);
            logger.info("Loaded {} grades for group {} - {}", grades.size(), courseCode, groupNumber);

            // Also load students for this group (synchronize left Students tab)
            // find the currently selected GroupAssignmentDetails in groupCombo/table
            DocenteGrupoDAO.GroupAssignmentDetails currentGroup = groupCombo.getSelectionModel().getSelectedItem();
            if (currentGroup != null) {
                loadStudentsForGroup(currentGroup);
            }
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

    @FXML
    private void handleRegisterGrade() {
        GradeDisplay selected = gradesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Atención", "Seleccione un estudiante en la tabla antes de registrar la nota.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Registrar nota");
        dialog.setHeaderText("Registrar nota para: " + selected.getStudentName());
        dialog.setContentText("Ingrese la nota (0.0 - 5.0):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double value = Double.parseDouble(input.replace(',', '.'));
                if (value < 0.0 || value > 5.0) {
                    showAlert("Error", "La nota debe estar entre 0.0 y 5.0");
                    return;
                }

                // Ensure enrollmentId is present
                if (selected.getEnrollmentId() == null) {
                    showAlert("Error", "No se pudo identificar la inscripción del estudiante (id_detalle). No se puede registrar la nota.");
                    return;
                }

                // Ask for optional comment/label for this evaluation
                TextInputDialog commentDialog = new TextInputDialog();
                commentDialog.setTitle("Etiqueta de evaluación (opcional)");
                commentDialog.setHeaderText("Registrar nota para: " + selected.getStudentName());
                commentDialog.setContentText("Ingrese una etiqueta o descripción para esta evaluación (opcional):");

                commentDialog.showAndWait().ifPresentOrElse(comment -> {
                    try {
                        // Create domain Grade and persist as Calificacion (allow multiple per student)
                        com.academictracker.model.Grade grade = new com.academictracker.model.Grade();
                        grade.setEnrollmentId(selected.getEnrollmentId());
                        grade.setGradeValue(value);
                        grade.setGradedBy(currentTeacherId);
                        if (!comment.isBlank()) {
                            grade.setComments(comment);
                        }

                        com.academictracker.model.Grade created = gradeDAO.createCalificacion(grade);

                        showAlert("Éxito", "Nota registrada correctamente (ID: " + created.getGradeId() + ")");

                        // Reload current group grades
                        DocenteGrupoDAO.GroupAssignmentDetails currentGroup = groupCombo.getSelectionModel().getSelectedItem();
                        if (currentGroup != null) {
                            loadGradesForGroup(currentGroup.codAsignatura, currentGroup.numeroGrupo);
                        } else {
                            // fallback: clear selection
                            gradesTable.getItems().clear();
                        }

                    } catch (Exception e) {
                        logger.error("Error registering calificacion", e);
                        showAlert("Error", "No se pudo registrar la nota: " + e.getMessage());
                    }
                }, () -> {
                    // If user closed the comment dialog without entering text, still create the calificacion
                    try {
                        com.academictracker.model.Grade grade = new com.academictracker.model.Grade();
                        grade.setEnrollmentId(selected.getEnrollmentId());
                        grade.setGradeValue(value);
                        grade.setGradedBy(currentTeacherId);

                        com.academictracker.model.Grade created = gradeDAO.createCalificacion(grade);

                        showAlert("Éxito", "Nota registrada correctamente (ID: " + created.getGradeId() + ")");

                        DocenteGrupoDAO.GroupAssignmentDetails currentGroup = groupCombo.getSelectionModel().getSelectedItem();
                        if (currentGroup != null) {
                            loadGradesForGroup(currentGroup.codAsignatura, currentGroup.numeroGrupo);
                        } else {
                            gradesTable.getItems().clear();
                        }

                    } catch (Exception e) {
                        logger.error("Error registering calificacion (no comment)", e);
                        showAlert("Error", "No se pudo registrar la nota: " + e.getMessage());
                    }
                });

            } catch (NumberFormatException e) {
                showAlert("Error", "Formato de nota inválido.");
            } catch (Exception e) {
                logger.error("Error registering grade", e);
                showAlert("Error", "No se pudo registrar la nota: " + e.getMessage());
            }
        });
    }

    @FXML
    public void handleRefreshStudents() {
        DocenteGrupoDAO.GroupAssignmentDetails group = groupCombo.getSelectionModel().getSelectedItem();
        loadStudentsForGroup(group);
    }

    @FXML
    public void handleCloseCourse() {
        DocenteGrupoDAO.GroupAssignmentDetails currentGroup = groupCombo.getSelectionModel().getSelectedItem();
        if (currentGroup == null) {
            showAlert("Atención", "Seleccione un grupo antes de cerrar el curso.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cierre de curso");
        confirm.setHeaderText("Cerrar curso: " + currentGroup.codAsignatura + " - Grupo " + currentGroup.numeroGrupo);
        confirm.setContentText("Esta acción marcará las notas definitivas como cerradas y no permitirá agregar más calificaciones. ¿Continuar?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // get all enrollments for the group
                    List<DocenteGrupoDAO.StudentRow> students = docenteGrupoDAO.getStudentsForGroup(currentGroup.idGrupo);
                    for (DocenteGrupoDAO.StudentRow s : students) {
                        gradeDAO.closeFinalGradeForEnrollment(s.getEnrollmentId());
                    }

                    showAlert("Éxito", "El curso ha sido cerrado correctamente. No se podrán agregar más notas.");

                    // reload UI
                    loadGradesForGroup(currentGroup.codAsignatura, currentGroup.numeroGrupo);
                    loadStudentsForGroup(currentGroup);
                } catch (Exception e) {
                    logger.error("Error closing course", e);
                    showAlert("Error", "No se pudo cerrar el curso: " + e.getMessage());
                }
            }
        });
    }
}
