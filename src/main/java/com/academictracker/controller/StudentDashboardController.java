package com.academictracker.controller;

import com.academictracker.dao.*;
import com.academictracker.model.*;
import com.academictracker.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for Student Dashboard
 */
public class StudentDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(StudentDashboardController.class);

    // Header Labels
    @FXML private Label welcomeLabel;
    @FXML private Label programaLabel;
    @FXML private Label periodoLabel;
    @FXML private Label totalCreditosLabel;
    @FXML private Label promedioLabel;
    @FXML private Label enrollmentMessageLabel;

    // Tab Pane
    @FXML private TabPane mainTabPane;

    // Enrolled Courses Table
    @FXML private TableView<EnrolledCourseDTO> enrolledCoursesTable;
    @FXML private TableColumn<EnrolledCourseDTO, String> colCodAsignatura;
    @FXML private TableColumn<EnrolledCourseDTO, String> colNombreAsignatura;
    @FXML private TableColumn<EnrolledCourseDTO, String> colGrupo;
    @FXML private TableColumn<EnrolledCourseDTO, String> colCreditos;
    @FXML private TableColumn<EnrolledCourseDTO, String> colEstado;
    @FXML private TableColumn<EnrolledCourseDTO, String> colFechaInscripcion;
    @FXML private Button btnCancelEnrollment;

    // Available Groups Table
    @FXML private TableView<GrupoDTO> availableGroupsTable;
    @FXML private TableColumn<GrupoDTO, String> colAvailCodAsignatura;
    @FXML private TableColumn<GrupoDTO, String> colAvailNombreAsignatura;
    @FXML private TableColumn<GrupoDTO, String> colAvailGrupo;
    @FXML private TableColumn<GrupoDTO, String> colAvailCreditos;
    @FXML private TableColumn<GrupoDTO, String> colAvailSemestre;
    @FXML private TableColumn<GrupoDTO, String> colAvailCupos;
    @FXML private TableColumn<GrupoDTO, String> colAvailSede;
    @FXML private ComboBox<String> semestreComboBox;
    @FXML private Button btnEnrollInGroup;

    // Grades Table
    @FXML private TableView<GradeDTO> gradesTable;
    @FXML private TableColumn<GradeDTO, String> colGradeCodAsignatura;
    @FXML private TableColumn<GradeDTO, String> colGradeNombreAsignatura;
    @FXML private TableColumn<GradeDTO, String> colGradeGrupo;
    @FXML private TableColumn<GradeDTO, String> colGradeCreditos;
    @FXML private TableColumn<GradeDTO, String> colNotaDefinitiva;
    @FXML private TableColumn<GradeDTO, String> colGradeEstado;
    @FXML private Label gradesStatusLabel;

    // DAOs
    private final StudentDAO studentDAO = new StudentDAO();
    private final MatriculaDAO matriculaDAO = new MatriculaDAO();
    private final DetalleMatriculaDAO detalleMatriculaDAO = new DetalleMatriculaDAO();
    private final GrupoDAO grupoDAO = new GrupoDAO();
    private final PeriodoAcademicoDAO periodoAcademicoDAO = new PeriodoAcademicoDAO();
    private final GradeDAO gradeDAO = new GradeDAO();

    // Current student and enrollment data
    private Student currentStudent;
    private Matricula currentMatricula;
    private String currentPeriodCode;

    @FXML
    private void initialize() {
        setupTables();
        loadStudentData();
        loadCurrentPeriod();
        loadEnrolledCourses();
        loadGrades();
        loadAvailableGroups();
        setupTableSelectionListeners();
    }

    /**
     * Load all grades associated with the current student and populate the gradesTable.
     */
    @FXML
    private void loadGrades() {
        try {
            logger.debug("loadGrades(): invoked. gradesTable=null? {}", gradesTable == null);
            logger.debug("loadGrades(): columns null? cod={}, nombre={}, grupo={}, creditos={}, nota={}, estado={}",
                colGradeCodAsignatura == null, colGradeNombreAsignatura == null,
                colGradeGrupo == null, colGradeCreditos == null, colNotaDefinitiva == null, colGradeEstado == null);

            if (currentStudent == null) {
                // Attempt to recover currentStudent from SessionManager as a fallback
                try {
                    User cu = SessionManager.getInstance().getCurrentUser();
                    if (cu != null) {
                        logger.info("loadGrades(): attempting fallback loadStudentData from SessionManager: {} / {}", cu.getUsername(), cu.getIdReferencia());
                        Optional<Student> sOpt = Optional.empty();
                        if (cu.getIdReferencia() != null && !cu.getIdReferencia().isEmpty()) {
                            sOpt = studentDAO.findById(cu.getIdReferencia());
                        }
                        if (!sOpt.isPresent()) {
                            sOpt = studentDAO.findByCorreoInstitucional(cu.getUsername());
                        }
                        if (sOpt.isPresent()) {
                            currentStudent = sOpt.get();
                            logger.info("loadGrades(): fallback loaded student {}", currentStudent.getCodEstudiante());
                        }
                    }
                } catch (SQLException sqe) {
                    logger.warn("loadGrades(): fallback student lookup failed", sqe);
                }
                gradesTable.setItems(FXCollections.observableArrayList());
                gradesTable.setPlaceholder(new Label("No hay calificaciones (estudiante no encontrado)"));
                promedioLabel.setText("Promedio: -");
                logger.info("loadGrades(): currentStudent is null, aborting");
                return;
            }

            logger.info("loadGrades(): currentStudent={}, currentMatricula={}", currentStudent.getCodEstudiante(), currentMatricula != null ? currentMatricula.getIdMatricula() : null);

            ObservableList<GradeDTO> gradeList = FXCollections.observableArrayList();
            double sum = 0.0;
            int count = 0;

            List<Matricula> matriculas = matriculaDAO.findByStudent(currentStudent.getCodEstudiante());
            logger.info("loadGrades(): found {} matriculas for student {}", matriculas.size(), currentStudent.getCodEstudiante());

            for (Matricula m : matriculas) {
                List<DetalleMatricula> detalles = detalleMatriculaDAO.findByMatricula(m.getIdMatricula());
                logger.info("loadGrades(): matricula {} has {} detalles", m.getIdMatricula(), detalles.size());
                for (DetalleMatricula detalle : detalles) {
                    GradeDTO dto = new GradeDTO();

                    if (detalle.getGrupo() != null && detalle.getGrupo().getAsignatura() != null) {
                        dto.setCodAsignatura(detalle.getGrupo().getCodAsignatura());
                        dto.setNombreAsignatura(detalle.getGrupo().getAsignatura().getNombre());
                        dto.setGrupo(String.valueOf(detalle.getGrupo().getNumeroGrupo()));
                        dto.setCreditos(String.valueOf(detalle.getGrupo().getAsignatura().getCreditos()));
                    } else {
                        dto.setCodAsignatura("-");
                        dto.setNombreAsignatura("-");
                        dto.setGrupo("-");
                        dto.setCreditos("-");
                    }

                    // Try find final grade in NotaDefinitiva
                    Optional<com.academictracker.model.Grade> finalGradeOpt = gradeDAO.findFinalGradeByEnrollment(detalle.getIdDetalle());
                    Double gradeVal = null;

                    if (finalGradeOpt.isPresent()) {
                        gradeVal = finalGradeOpt.get().getGradeValue();
                        logger.info("loadGrades(): detalle {} -> found NotaDefinitiva = {}", detalle.getIdDetalle(), gradeVal);
                    } else {
                        // Fallback to Calificacion entries for this enrollment
                        List<com.academictracker.model.Grade> califs = gradeDAO.findByEnrollment(detalle.getIdDetalle());
                        if (califs != null && !califs.isEmpty()) {
                            gradeVal = califs.get(0).getGradeValue(); // findByEnrollment returns ordered desc
                            logger.info("loadGrades(): detalle {} -> found Calificacion = {} (count={})", detalle.getIdDetalle(), gradeVal, califs.size());
                        } else {
                            logger.info("loadGrades(): detalle {} -> no grade found", detalle.getIdDetalle());
                        }
                    }

                    if (gradeVal != null) {
                        dto.setNotaDefinitiva(String.format("%.2f", gradeVal));
                        dto.setEstado(gradeVal >= 3.0 ? "Aprobado" : "Reprobado");
                        sum += gradeVal;
                        count++;
                    } else {
                        dto.setNotaDefinitiva("N/A");
                        dto.setEstado(detalle.getEstado() != null ? detalle.getEstado() : "Sin nota");
                    }

                    gradeList.add(dto);
                }
            }

            gradesTable.setItems(gradeList);
            // Force refresh to ensure rows are displayed
            gradesTable.refresh();

            gradesTable.setPlaceholder(new Label("No hay calificaciones registradas"));

            logger.info("loadGrades(): gradeList size = {} (count numeric={})", gradeList.size(), count);

            if (gradesStatusLabel != null) {
                gradesStatusLabel.setText(String.format("Asignaturas: %d — Notas numéricas: %d", gradeList.size(), count));
            }

            if (count > 0) {
                double avg = sum / count;
                promedioLabel.setText(String.format("Promedio: %.2f", avg));
            } else {
                promedioLabel.setText("Promedio: -");
            }

        } catch (SQLException e) {
            logger.error("Error loading grades", e);
            showError("Error", "No se pudieron cargar las calificaciones: " + e.getMessage());
        }
    }

    private void setupTables() {
        // Setup Enrolled Courses Table
        colCodAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodAsignatura()));
        colNombreAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreAsignatura()));
        colGrupo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrupo()));
        colCreditos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreditos()));
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));
        colFechaInscripcion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaInscripcion()));

        // Setup Available Groups Table
        colAvailCodAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodAsignatura()));
        colAvailNombreAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreAsignatura()));
        colAvailGrupo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrupo()));
        colAvailCreditos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreditos()));
        colAvailSemestre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemestre()));
        colAvailCupos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCuposDisponibles()));
        colAvailSede.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSede()));

        // Setup Grades Table
        colGradeCodAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodAsignatura()));
        colGradeNombreAsignatura.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreAsignatura()));
        colGradeGrupo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGrupo()));
        colGradeCreditos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreditos()));
        colNotaDefinitiva.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNotaDefinitiva()));
        colGradeEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));

        // Populate semester filter
        ObservableList<String> semestres = FXCollections.observableArrayList(
            "Todos", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
        );
        semestreComboBox.setItems(semestres);
        semestreComboBox.setValue("Todos");
    }

    private void setupTableSelectionListeners() {
        enrolledCoursesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                btnCancelEnrollment.setDisable(newSelection == null);
            }
        );

        availableGroupsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                btnEnrollInGroup.setDisable(newSelection == null);
            }
        );
    }

    private void loadStudentData() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            logger.info("Loading student data for user: {}, idReferencia: {}",
                currentUser.getUsername(), currentUser.getIdReferencia());

            welcomeLabel.setText("Bienvenido, " + currentUser.getUsername());

            // Load student information using idReferencia (cod_estudiante)
            // If idReferencia is null, try using correo_institucional
            Optional<Student> studentOpt = Optional.empty();

            if (currentUser.getIdReferencia() != null && !currentUser.getIdReferencia().isEmpty()) {
                studentOpt = studentDAO.findById(currentUser.getIdReferencia());
                logger.info("Searching student by idReferencia: {}", currentUser.getIdReferencia());
            }

            // If not found by idReferencia, try by email (username is the email)
            if (!studentOpt.isPresent()) {
                logger.info("Student not found by idReferencia, trying by email: {}", currentUser.getUsername());
                studentOpt = studentDAO.findByCorreoInstitucional(currentUser.getUsername());
            }

            if (studentOpt.isPresent()) {
                currentStudent = studentOpt.get();
                logger.info("Student found: {} {}, Programa: {}",
                    currentStudent.getNombres(), currentStudent.getApellidos(),
                    currentStudent.getCodPrograma());

                if (currentStudent.getPrograma() != null) {
                    programaLabel.setText("Programa: " + currentStudent.getPrograma().getNombre());
                } else {
                    programaLabel.setText("Programa: -");
                }
            } else {
                logger.error("Student not found for user: {}", currentUser.getUsername());
                showError("Error", "No se encontró información del estudiante asociado a este usuario");
            }
        } catch (SQLException e) {
            logger.error("Error loading student data", e);
            showError("Error", "No se pudo cargar la información del estudiante");
        }
    }

    private void loadCurrentPeriod() {
        try {
            // Get the active period
            List<PeriodoAcademico> periodos = periodoAcademicoDAO.findAll();
            Optional<PeriodoAcademico> activePeriod = periodos.stream()
                .filter(PeriodoAcademico::isActivo)
                .findFirst();

            if (activePeriod.isPresent()) {
                currentPeriodCode = activePeriod.get().getCodPeriodo();
                periodoLabel.setText("Período: " + activePeriod.get().getNombre());

                // Load or create matricula for current period
                loadOrCreateMatricula();
            } else {
                periodoLabel.setText("Período: No hay período activo");
                showWarning("Advertencia", "No hay un período académico activo en este momento.");
            }
        } catch (SQLException e) {
            logger.error("Error loading current period", e);
            showError("Error", "No se pudo cargar el período académico actual");
        }
    }

    private void loadOrCreateMatricula() {
        try {
            if (currentStudent == null || currentPeriodCode == null) {
                return;
            }

            List<Matricula> matriculas = matriculaDAO.findByStudent(currentStudent.getCodEstudiante());
            Optional<Matricula> currentMatriculaOpt = matriculas.stream()
                .filter(m -> m.getCodPeriodo().equals(currentPeriodCode))
                .findFirst();

            if (currentMatriculaOpt.isPresent()) {
                currentMatricula = currentMatriculaOpt.get();
            } else {
                // Create new matricula for current period
                currentMatricula = new Matricula(currentStudent.getCodEstudiante(), currentPeriodCode);
                currentMatricula = matriculaDAO.create(currentMatricula);
                logger.info("Created new matricula for student {} in period {}",
                    currentStudent.getCodEstudiante(), currentPeriodCode);
            }
        } catch (SQLException e) {
            logger.error("Error loading or creating matricula", e);
            showError("Error", "No se pudo crear la matrícula para este período");
        }
    }

    @FXML
    private void loadEnrolledCourses() {
        try {
            if (currentMatricula == null) {
                enrolledCoursesTable.setItems(FXCollections.observableArrayList());
                totalCreditosLabel.setText("Créditos: 0");
                return;
            }

            List<DetalleMatricula> detalles = detalleMatriculaDAO.findByMatricula(currentMatricula.getIdMatricula());
            ObservableList<EnrolledCourseDTO> enrolledCourses = FXCollections.observableArrayList();

            int totalCreditos = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (DetalleMatricula detalle : detalles) {
                if (detalle.getGrupo() != null && detalle.getGrupo().getAsignatura() != null) {
                    Asignatura asignatura = detalle.getGrupo().getAsignatura();
                    EnrolledCourseDTO dto = new EnrolledCourseDTO();
                    dto.setIdDetalle(detalle.getIdDetalle());
                    dto.setCodAsignatura(asignatura.getCodAsignatura());
                    dto.setNombreAsignatura(asignatura.getNombre());
                    dto.setGrupo(String.valueOf(detalle.getGrupo().getNumeroGrupo()));
                    dto.setCreditos(String.valueOf(asignatura.getCreditos()));
                    dto.setEstado(detalle.getEstado());
                    dto.setFechaInscripcion(detalle.getFechaInscripcion() != null ?
                        detalle.getFechaInscripcion().format(formatter) : "-");

                    enrolledCourses.add(dto);
                    totalCreditos += asignatura.getCreditos();
                }
            }

            enrolledCoursesTable.setItems(enrolledCourses);
            totalCreditosLabel.setText("Créditos: " + totalCreditos);

        } catch (SQLException e) {
            logger.error("Error loading enrolled courses", e);
            showError("Error", "No se pudieron cargar los cursos inscritos");
        }
    }

    @FXML
    private void loadAvailableGroups() {
        try {
            if (currentStudent == null || currentPeriodCode == null) {
                logger.warn("Cannot load groups: currentStudent={}, currentPeriodCode={}",
                    currentStudent, currentPeriodCode);
                availableGroupsTable.setItems(FXCollections.observableArrayList());
                return;
            }

            // Get all active groups for current period and student's program
            List<Grupo> grupos = grupoDAO.findAll();
            logger.info("Total grupos found: {}", grupos.size());
            logger.info("Filtering for period: {}, program: {}", currentPeriodCode, currentStudent.getCodPrograma());

            // Filter by current period and student's program
            List<Grupo> filteredGrupos = grupos.stream()
                .filter(g -> {
                    boolean periodMatch = g.getCodPeriodo().equals(currentPeriodCode);
                    logger.debug("Grupo {}: period {} matches {}: {}",
                        g.getIdGrupo(), g.getCodPeriodo(), currentPeriodCode, periodMatch);
                    return periodMatch;
                })
                .filter(g -> {
                    boolean hasAsignatura = g.getAsignatura() != null;
                    boolean hasCodPrograma = hasAsignatura && g.getAsignatura().getCodPrograma() != null;
                    boolean programMatch = hasCodPrograma &&
                        g.getAsignatura().getCodPrograma().equals(currentStudent.getCodPrograma());
                    if (hasAsignatura) {
                        logger.debug("Grupo {}: asignatura={}, program={}, match={}",
                            g.getIdGrupo(), g.getAsignatura().getNombre(),
                            g.getAsignatura().getCodPrograma(), programMatch);
                    }
                    return hasAsignatura && hasCodPrograma && programMatch;
                })
                .filter(g -> {
                    boolean hasSpots = g.getCuposDisponibles() > 0;
                    logger.debug("Grupo {}: cupos disponibles={}, has spots={}",
                        g.getIdGrupo(), g.getCuposDisponibles(), hasSpots);
                    return hasSpots;
                })
                .collect(Collectors.toList());

            logger.info("Filtered grupos count: {}", filteredGrupos.size());
            displayGroupsTable(filteredGrupos);

        } catch (SQLException e) {
            logger.error("Error loading available groups", e);
            showError("Error", "No se pudieron cargar los grupos disponibles");
        }
    }

    private void displayGroupsTable(List<Grupo> grupos) {
        ObservableList<GrupoDTO> groupList = FXCollections.observableArrayList();

        for (Grupo grupo : grupos) {
            if (grupo.getAsignatura() != null) {
                GrupoDTO dto = new GrupoDTO();
                dto.setIdGrupo(grupo.getIdGrupo());
                dto.setCodAsignatura(grupo.getCodAsignatura());
                dto.setNombreAsignatura(grupo.getAsignatura().getNombre());
                dto.setGrupo(String.valueOf(grupo.getNumeroGrupo()));
                dto.setCreditos(String.valueOf(grupo.getAsignatura().getCreditos()));
                dto.setSemestre(String.valueOf(grupo.getAsignatura().getSemestreSugerido()));
                dto.setCuposDisponibles(grupo.getCuposDisponibles() + "/" + grupo.getCupoMaximo());
                dto.setSede(grupo.getSede() != null ? grupo.getSede().getNombre() : "-");

                groupList.add(dto);
            }
        }

        availableGroupsTable.setItems(groupList);
    }

    @FXML
    private void handleFilterBySemester() {
        try {
            String selectedSemester = semestreComboBox.getValue();

            if (selectedSemester == null || selectedSemester.equals("Todos")) {
                loadAvailableGroups();
                return;
            }

            // Get all groups and filter by semester
            List<Grupo> grupos = grupoDAO.findAll();

            int semestre = Integer.parseInt(selectedSemester);
            List<Grupo> filteredGrupos = grupos.stream()
                .filter(g -> g.getCodPeriodo().equals(currentPeriodCode))
                .filter(g -> g.getAsignatura() != null &&
                           g.getAsignatura().getCodPrograma() != null &&
                           g.getAsignatura().getCodPrograma().equals(currentStudent.getCodPrograma()))
                .filter(g -> g.getCuposDisponibles() > 0)
                .filter(g -> g.getAsignatura().getSemestreSugerido() == semestre)
                .collect(Collectors.toList());

            displayGroupsTable(filteredGrupos);

        } catch (SQLException e) {
            logger.error("Error filtering groups by semester", e);
            showError("Error", "No se pudo filtrar por semestre");
        } catch (NumberFormatException e) {
            loadAvailableGroups();
        }
    }

    @FXML
    private void handleEnrollInGroup() {
        GrupoDTO selectedGroup = availableGroupsTable.getSelectionModel().getSelectedItem();

        if (selectedGroup == null) {
            showWarning("Advertencia", "Por favor seleccione un grupo para inscribirse");
            return;
        }

        if (currentMatricula == null) {
            showError("Error", "No hay una matrícula activa para este período");
            return;
        }

        try {
            // Check if already enrolled in this group
            if (detalleMatriculaDAO.isStudentEnrolledInGrupo(currentMatricula.getIdMatricula(), selectedGroup.getIdGrupo())) {
                showWarning("Advertencia", "Ya estás inscrito en este grupo");
                return;
            }

            // Check if already enrolled in another group of the same course
            List<DetalleMatricula> currentEnrollments = detalleMatriculaDAO.findByMatricula(currentMatricula.getIdMatricula());
            boolean alreadyEnrolledInCourse = currentEnrollments.stream()
                .anyMatch(d -> d.getGrupo() != null &&
                              d.getGrupo().getCodAsignatura().equals(selectedGroup.getCodAsignatura()) &&
                              d.getEstado().equals("inscrito"));

            if (alreadyEnrolledInCourse) {
                showWarning("Advertencia", "Ya estás inscrito en otro grupo de esta asignatura");
                return;
            }

            // Create new enrollment
            DetalleMatricula nuevoDetalle = new DetalleMatricula(currentMatricula.getIdMatricula(), selectedGroup.getIdGrupo());
            detalleMatriculaDAO.create(nuevoDetalle);

            // Update grupo cupo_ocupado
            Optional<Grupo> grupoOpt = grupoDAO.findById(selectedGroup.getIdGrupo());
            if (grupoOpt.isPresent()) {
                Grupo grupo = grupoOpt.get();
                grupo.setCupoOcupado(grupo.getCupoOcupado() + 1);
                grupoDAO.update(grupo);
            }

            // Update total credits in matricula
            updateMatriculaCredits();

            enrollmentMessageLabel.setText("¡Inscripción exitosa!");
            enrollmentMessageLabel.setStyle("-fx-text-fill: green;");

            // Refresh tables
            loadEnrolledCourses();
            loadAvailableGroups();

            showInfo("Éxito", "Te has inscrito exitosamente en el grupo " + selectedGroup.getGrupo() +
                     " de " + selectedGroup.getNombreAsignatura());

        } catch (SQLException e) {
            logger.error("Error enrolling in group", e);
            showError("Error", "No se pudo completar la inscripción: " + e.getMessage());
            enrollmentMessageLabel.setText("Error en la inscripción");
            enrollmentMessageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancelEnrollment() {
        EnrolledCourseDTO selectedCourse = enrolledCoursesTable.getSelectionModel().getSelectedItem();

        if (selectedCourse == null) {
            showWarning("Advertencia", "Por favor seleccione un curso para cancelar");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Cancelación");
        confirmAlert.setHeaderText("¿Está seguro de cancelar la inscripción?");
        confirmAlert.setContentText("Curso: " + selectedCourse.getNombreAsignatura() +
                                    "\nGrupo: " + selectedCourse.getGrupo());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Get detalle to find the grupo
                Optional<DetalleMatricula> detalleOpt = detalleMatriculaDAO.findById(selectedCourse.getIdDetalle());

                if (detalleOpt.isPresent()) {
                    DetalleMatricula detalle = detalleOpt.get();

                    // Update grupo cupo_ocupado
                    Optional<Grupo> grupoOpt = grupoDAO.findById(detalle.getIdGrupo());
                    if (grupoOpt.isPresent()) {
                        Grupo grupo = grupoOpt.get();
                        grupo.setCupoOcupado(Math.max(0, grupo.getCupoOcupado() - 1));
                        grupoDAO.update(grupo);
                    }

                    // Delete enrollment
                    detalleMatriculaDAO.delete(selectedCourse.getIdDetalle());

                    // Update total credits
                    updateMatriculaCredits();

                    // Refresh tables
                    loadEnrolledCourses();
                    loadAvailableGroups();

                    showInfo("Éxito", "La inscripción ha sido cancelada");
                }
            } catch (SQLException e) {
                logger.error("Error canceling enrollment", e);
                showError("Error", "No se pudo cancelar la inscripción: " + e.getMessage());
            }
        }
    }

    private void updateMatriculaCredits() {
        try {
            if (currentMatricula == null) return;

            List<DetalleMatricula> detalles = detalleMatriculaDAO.findByMatricula(currentMatricula.getIdMatricula());
            int totalCreditos = 0;

            for (DetalleMatricula detalle : detalles) {
                if (detalle.getGrupo() != null && detalle.getGrupo().getAsignatura() != null) {
                    totalCreditos += detalle.getGrupo().getAsignatura().getCreditos();
                }
            }

            currentMatricula.setTotalCreditos(totalCreditos);
            matriculaDAO.update(currentMatricula);

        } catch (SQLException e) {
            logger.error("Error updating matricula credits", e);
        }
    }

    @FXML
    private void handleRefresh() {
        loadEnrolledCourses();
        loadAvailableGroups();
        enrollmentMessageLabel.setText("");
        loadGrades();
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Academic Tracker - Login");

        } catch (Exception e) {
            logger.error("Error during logout", e);
            showError("Error", "No se pudo cerrar sesión");
        }
    }

    // Utility methods
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // DTOs for TableView
    public static class EnrolledCourseDTO {
        private Long idDetalle;
        private String codAsignatura;
        private String nombreAsignatura;
        private String grupo;
        private String creditos;
        private String estado;
        private String fechaInscripcion;

        // Getters and Setters
        public Long getIdDetalle() { return idDetalle; }
        public void setIdDetalle(Long idDetalle) { this.idDetalle = idDetalle; }
        public String getCodAsignatura() { return codAsignatura; }
        public void setCodAsignatura(String codAsignatura) { this.codAsignatura = codAsignatura; }
        public String getNombreAsignatura() { return nombreAsignatura; }
        public void setNombreAsignatura(String nombreAsignatura) { this.nombreAsignatura = nombreAsignatura; }
        public String getGrupo() { return grupo; }
        public void setGrupo(String grupo) { this.grupo = grupo; }
        public String getCreditos() { return creditos; }
        public void setCreditos(String creditos) { this.creditos = creditos; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getFechaInscripcion() { return fechaInscripcion; }
        public void setFechaInscripcion(String fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
    }

    public static class GrupoDTO {
        private Long idGrupo;
        private String codAsignatura;
        private String nombreAsignatura;
        private String grupo;
        private String creditos;
        private String semestre;
        private String cuposDisponibles;
        private String sede;

        // Getters and Setters
        public Long getIdGrupo() { return idGrupo; }
        public void setIdGrupo(Long idGrupo) { this.idGrupo = idGrupo; }
        public String getCodAsignatura() { return codAsignatura; }
        public void setCodAsignatura(String codAsignatura) { this.codAsignatura = codAsignatura; }
        public String getNombreAsignatura() { return nombreAsignatura; }
        public void setNombreAsignatura(String nombreAsignatura) { this.nombreAsignatura = nombreAsignatura; }
        public String getGrupo() { return grupo; }
        public void setGrupo(String grupo) { this.grupo = grupo; }
        public String getCreditos() { return creditos; }
        public void setCreditos(String creditos) { this.creditos = creditos; }
        public String getSemestre() { return semestre; }
        public void setSemestre(String semestre) { this.semestre = semestre; }
        public String getCuposDisponibles() { return cuposDisponibles; }
        public void setCuposDisponibles(String cuposDisponibles) { this.cuposDisponibles = cuposDisponibles; }
        public String getSede() { return sede; }
        public void setSede(String sede) { this.sede = sede; }
    }

    public static class GradeDTO {
        private String codAsignatura;
        private String nombreAsignatura;
        private String grupo;
        private String creditos;
        private String notaDefinitiva;
        private String estado;

        // Getters and Setters
        public String getCodAsignatura() { return codAsignatura; }
        public void setCodAsignatura(String codAsignatura) { this.codAsignatura = codAsignatura; }
        public String getNombreAsignatura() { return nombreAsignatura; }
        public void setNombreAsignatura(String nombreAsignatura) { this.nombreAsignatura = nombreAsignatura; }
        public String getGrupo() { return grupo; }
        public void setGrupo(String grupo) { this.grupo = grupo; }
        public String getCreditos() { return creditos; }
        public void setCreditos(String creditos) { this.creditos = creditos; }
        public String getNotaDefinitiva() { return notaDefinitiva; }
        public void setNotaDefinitiva(String notaDefinitiva) { this.notaDefinitiva = notaDefinitiva; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}

