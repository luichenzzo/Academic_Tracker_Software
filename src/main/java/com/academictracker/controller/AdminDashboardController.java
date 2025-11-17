package com.academictracker.controller;

import com.academictracker.model.*;
import com.academictracker.service.AcademicService;
import com.academictracker.service.UserService;
import com.academictracker.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.Optional;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Controller for Admin Dashboard
 */
public class AdminDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;

    // Reports UI
    @FXML private ComboBox<String> reportSelector;
    @FXML private Button runReportButton;
    @FXML private TableView<Map<String,Object>> reportTable;

    // Mapping of display name -> DB view name
    private final Map<String, String> reportViewMap = new LinkedHashMap<>();

    // Student Management
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> studentFirstNameColumn;
    @FXML private TableColumn<Student, String> studentLastNameColumn;
    @FXML private TableColumn<Student, String> studentDocumentColumn;
    @FXML private TableColumn<Student, String> studentEmailColumn;
    @FXML private TableColumn<Student, String> studentProgramColumn;
    @FXML private TableColumn<Student, String> studentSedeColumn;
    @FXML private TableColumn<Student, Integer> studentRiesgoColumn;

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

    // Group Management
    @FXML private TableView<Grupo> groupTable;
    @FXML private TableColumn<Grupo, Long> groupIdColumn;
    @FXML private TableColumn<Grupo, String> groupNumberColumn;
    @FXML private TableColumn<Grupo, String> groupCourseColumn;
    @FXML private TableColumn<Grupo, String> groupPeriodColumn;
    @FXML private TableColumn<Grupo, String> groupSedeColumn;
    @FXML private TableColumn<Grupo, String> groupCapacityColumn;
    @FXML private TableColumn<Grupo, Integer> groupEnrolledColumn;

    // Matricula Management
    @FXML private TableView<Matricula> matriculaTable;
    @FXML private TableColumn<Matricula, Long> matriculaIdColumn;
    @FXML private TableColumn<Matricula, String> matriculaStudentColumn;
    @FXML private TableColumn<Matricula, String> matriculaStudentNameColumn;
    @FXML private TableColumn<Matricula, String> matriculaPeriodColumn;
    @FXML private TableColumn<Matricula, String> matriculaDateColumn;
    @FXML private TableColumn<Matricula, Integer> matriculaCreditsColumn;
    @FXML private TableColumn<Matricula, String> matriculaStatusColumn;

    // User / Account Activation Management: show Students and Teachers and allow "Activate"
    @FXML private TableView<AccountCandidate> userTable;
    @FXML private TableColumn<AccountCandidate, String> userTypeColumn;
    @FXML private TableColumn<AccountCandidate, String> userRefColumn;
    @FXML private TableColumn<AccountCandidate, String> userNameColumn;
    @FXML private TableColumn<AccountCandidate, String> userEmailColumn;
    @FXML private TableColumn<AccountCandidate, String> userStatusColumn;

    @FXML private javafx.scene.control.Button evaluateRisksButton;

    private final UserService userService;
    private final AcademicService academicService;
    
    public AdminDashboardController() {
        this.userService = new UserService();
        this.academicService = new AcademicService();
    }
    
    // Lightweight wrapper to present students and teachers in a single table
    public static class AccountCandidate {
        private final String type; // "Student" or "Teacher"
        private final String referenceId; // cod_estudiante or id_docente
        private final String name;
        private final String email;
        private final boolean active;

        public AccountCandidate(String type, String referenceId, String name, String email, boolean active) {
            this.type = type;
            this.referenceId = referenceId;
            this.name = name;
            this.email = email;
            this.active = active;
        }

        public String getType() { return type; }
        public String getReferenceId() { return referenceId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public boolean isActive() { return active; }
    }

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Admin)");
        
        setupTables();
        loadData();

        setupReports();
    }

    private void setupReports() {
        // Populate mapping (display name -> view name). Keep same order as requirement list.
        reportViewMap.put("1. Matrícula y carga por periodo", "vw_report_matricula_carga");
        reportViewMap.put("2. Ocupación y top grupos", "vw_report_ocupacion_top_grupos");
        reportViewMap.put("3. Intentos fallidos de matrícula", "vw_report_intentos_fallidos_matricula");
        reportViewMap.put("4. Rendimiento por asignatura", "vw_report_rendimiento_asignatura");
        reportViewMap.put("5. Distribución de notas", "vw_report_distribucion_notas");
        reportViewMap.put("6. Evolución de promedio por estudiante", "vw_report_evolucion_promedio_estudiante");
        reportViewMap.put("7. Riesgo académico por periodo", "vw_report_riesgo_academico_periodo");
        reportViewMap.put("8. Intentos por asignatura", "vw_report_intentos_por_asignatura");
        reportViewMap.put("9. Trayectoria por cohorte", "vw_report_trayectoria_cohorte");
        reportViewMap.put("10. Mapa de prerrequisitos", "vw_report_mapa_prerrequisitos");
        reportViewMap.put("11. Impacto de prerrequisitos", "vw_report_impacto_prerrequisitos");
        reportViewMap.put("12. Reglas de evaluación incompletas", "vw_report_reglas_evaluacion_incompletas");
        reportViewMap.put("13. Reprobación por ítem de evaluación", "vw_report_reprobacion_por_item");
        reportViewMap.put("14. Avance en créditos vs plan", "vw_report_avance_creditos_vs_plan");
        reportViewMap.put("15. Opinión estudiantil consolidada", "vw_report_opinion_consolidada");
        reportViewMap.put("16. Cruce de opiniones y desempeño", "vw_report_cruce_opiniones_desempeno");
        reportViewMap.put("17. Asignaturas 'cuello de botella'", "vw_report_cuello_botella");
        reportViewMap.put("18. Calidad de datos", "vw_report_calidad_datos");

        reportSelector.setItems(FXCollections.observableArrayList(reportViewMap.keySet()));
        reportSelector.getSelectionModel().selectFirst();

        // Attach button handler
        runReportButton.setOnAction(evt -> handleRunReport());
    }

    @FXML
    private void handleRunReport() {
        String displayName = reportSelector.getSelectionModel().getSelectedItem();
        if (displayName == null) return;
        String viewName = reportViewMap.get(displayName);
        try {
            List<Map<String,Object>> rows = academicService.runReport(viewName);
            populateReportTable(rows);
        } catch (Exception e) {
            logger.error("Error running report {}", viewName, e);
            showAlert("Error", "Failed to run report: " + e.getMessage());
        }
    }

    private void populateReportTable(List<Map<String,Object>> rows) {
        reportTable.getItems().clear();
        reportTable.getColumns().clear();
        if (rows == null || rows.isEmpty()) return;

        // Get column names from first row in insertion order
        Map<String,Object> first = rows.get(0);
        List<String> columns = new ArrayList<>(first.keySet());

        for (String colName : columns) {
            TableColumn<Map<String,Object>, Object> col = new TableColumn<>(colName);
            col.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().get(colName)));
            reportTable.getColumns().add(col);
        }

        ObservableList<Map<String,Object>> items = FXCollections.observableArrayList(rows);
        reportTable.setItems(items);
    }

    private void setupTables() {
        // Student table - updated to match the correct Student model fields
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("codEstudiante"));
        studentFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        studentLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        studentDocumentColumn.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("correoInstitucional"));
        studentProgramColumn.setCellValueFactory(cellData -> {
            Program program = cellData.getValue().getPrograma();
            return new javafx.beans.property.SimpleStringProperty(
                program != null ? program.getNombre() : "N/A"
            );
        });
        studentSedeColumn.setCellValueFactory(cellData -> {
            Sede sede = cellData.getValue().getSede();
            return new javafx.beans.property.SimpleStringProperty(
                sede != null ? sede.getNombre() : "N/A"
            );
        });
        studentRiesgoColumn.setCellValueFactory(new PropertyValueFactory<>("nivelRiesgo"));

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

        // Group table
        groupIdColumn.setCellValueFactory(new PropertyValueFactory<>("idGrupo"));
        groupNumberColumn.setCellValueFactory(new PropertyValueFactory<>("numeroGrupo"));
        groupCourseColumn.setCellValueFactory(cellData -> {
            Asignatura asignatura = cellData.getValue().getAsignatura();
            return new javafx.beans.property.SimpleStringProperty(
                asignatura != null ? asignatura.getNombre() : cellData.getValue().getCodAsignatura()
            );
        });
        groupPeriodColumn.setCellValueFactory(cellData -> {
            PeriodoAcademico periodo = cellData.getValue().getPeriodoAcademico();
            return new javafx.beans.property.SimpleStringProperty(
                periodo != null ? periodo.getNombre() : cellData.getValue().getCodPeriodo()
            );
        });
        groupSedeColumn.setCellValueFactory(cellData -> {
            Sede sede = cellData.getValue().getSede();
            return new javafx.beans.property.SimpleStringProperty(
                sede != null ? sede.getNombre() : "N/A"
            );
        });
        groupCapacityColumn.setCellValueFactory(cellData -> {
            Grupo g = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                g.getCupoOcupado() + " / " + g.getCupoMaximo()
            );
        });
        groupEnrolledColumn.setCellValueFactory(new PropertyValueFactory<>("cupoOcupado"));

        // Matricula table
        matriculaIdColumn.setCellValueFactory(new PropertyValueFactory<>("idMatricula"));
        matriculaStudentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getEstudiante() != null ? cellData.getValue().getEstudiante().getCodEstudiante() : "N/A"
        ));
        matriculaStudentNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getEstudiante() != null ? cellData.getValue().getEstudiante().getNombres() + " " + cellData.getValue().getEstudiante().getApellidos() : "N/A"
        ));
        matriculaPeriodColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getPeriodoAcademico() != null ? cellData.getValue().getPeriodoAcademico().getNombre() : "N/A"
        ));
        matriculaDateColumn.setCellValueFactory(new PropertyValueFactory<>("fechaMatricula"));
        matriculaCreditsColumn.setCellValueFactory(new PropertyValueFactory<>("totalCreditos"));
        matriculaStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getEstado()
        ));

        // User/account candidate table setup
        userTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        userRefColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReferenceId()));
        userNameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        userEmailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        userStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().isActive() ? "Has account" : "No account"));
    }
    
    private void loadData() {
        loadStudents();
        loadTeachers();
        loadPrograms();
        loadCourses();
        loadGroups();
        loadAccountCandidates();
        loadMatriculas();
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

    private void loadGroups() {
        try {
            ObservableList<Grupo> groups = FXCollections.observableArrayList(academicService.getAllGrupos());
            groupTable.setItems(groups);
        } catch (Exception e) {
            logger.error("Error loading groups", e);
            showAlert("Error", "Failed to load groups: " + e.getMessage());
        }
    }

    private void loadMatriculas() {
        try {
            ObservableList<Matricula> matriculas = FXCollections.observableArrayList(academicService.getAllMatriculas());
            matriculaTable.setItems(matriculas);
        } catch (Exception e) {
            logger.error("Error loading matriculas", e);
            showAlert("Error", "Failed to load matriculas: " + e.getMessage());
        }
    }

    private void loadAccountCandidates() {
        try {
            List<Student> students = academicService.getAllStudents();
            List<Teacher> teachers = academicService.getAllTeachers();

            ObservableList<AccountCandidate> items = FXCollections.observableArrayList();

            // Students: cod_estudiante is the reference id, email is correoInstitucional
            for (Student s : students) {
                boolean hasAccount = false;
                try {
                    if (s.getCorreoInstitucional() != null) {
                        hasAccount = userService.existsByUsername(s.getCorreoInstitucional());
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to check user existence for student {}: {}", s.getCodEstudiante(), ex.getMessage());
                }
                items.add(new AccountCandidate("Student", s.getCodEstudiante(), s.getNombres() + " " + s.getApellidos(), s.getCorreoInstitucional(), hasAccount));
            }

            // Teachers: id_docente -> idDocente, email -> correoInstitucional
            for (Teacher t : teachers) {
                boolean hasAccount = false;
                try {
                    if (t.getCorreoInstitucional() != null) {
                        hasAccount = userService.existsByUsername(t.getCorreoInstitucional());
                    }
                } catch (Exception ex) {
                    logger.warn("Failed to check user existence for teacher {}: {}", t.getIdDocente(), ex.getMessage());
                }
                items.add(new AccountCandidate("Teacher", t.getIdDocente() != null ? t.getIdDocente().toString() : "", t.getNombres() + " " + t.getApellidos(), t.getCorreoInstitucional(), hasAccount));
            }

            userTable.setItems(items);
        } catch (Exception e) {
            logger.error("Error loading account candidates", e);
            showAlert("Error", "Failed to load account candidates: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStudent() {
        try {
            // Create the dialog
            Dialog<Student> dialog = new Dialog<>();
            dialog.setTitle("Add New Student");
            dialog.setHeaderText("Enter student information");

            // Set the button types
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField codEstudiante = new TextField();
            codEstudiante.setPromptText("Student Code");
            TextField numeroDocumento = new TextField();
            numeroDocumento.setPromptText("Document Number");
            ComboBox<String> tipoDocumento = new ComboBox<>();
            tipoDocumento.getItems().addAll("CC", "TI", "CE", "PA");
            tipoDocumento.setValue("CC");
            TextField nombres = new TextField();
            nombres.setPromptText("Names");
            TextField apellidos = new TextField();
            apellidos.setPromptText("Last Names");
            TextField correoInstitucional = new TextField();
            correoInstitucional.setPromptText("Institutional Email");
            TextField telefono = new TextField();
            telefono.setPromptText("Phone");
            DatePicker fechaIngreso = new DatePicker();
            fechaIngreso.setValue(LocalDate.now());
            ComboBox<Program> programCombo = new ComboBox<>();
            ComboBox<Sede> sedeCombo = new ComboBox<>();

            // Load programs and sedes
            try {
                List<Program> programs = academicService.getAllPrograms();
                List<Sede> sedes = academicService.getAllSedes();

                logger.info("Loaded {} programs from database", programs.size());
                logger.info("Loaded {} sedes from database", sedes.size());

                // Debug: Print each program
                programs.forEach(p -> logger.info("Program: {} - {}", p.getCodigoPrograma(), p.getNombre()));

                programCombo.setItems(FXCollections.observableArrayList(programs));
                sedeCombo.setItems(FXCollections.observableArrayList(sedes));

                // Set cell factory to display program names properly
                programCombo.setButtonCell(new ListCell<Program>() {
                    @Override
                    protected void updateItem(Program item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCodigoPrograma() + " - " + item.getNombre());
                        }
                    }
                });

                programCombo.setCellFactory(lv -> new ListCell<Program>() {
                    @Override
                    protected void updateItem(Program item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCodigoPrograma() + " - " + item.getNombre());
                        }
                    }
                });

                // Set cell factory for Sede combobox
                sedeCombo.setButtonCell(new ListCell<Sede>() {
                    @Override
                    protected void updateItem(Sede item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getNombre());
                        }
                    }
                });

                sedeCombo.setCellFactory(lv -> new ListCell<Sede>() {
                    @Override
                    protected void updateItem(Sede item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getNombre());
                        }
                    }
                });

            } catch (Exception e) {
                logger.error("Error loading data for comboboxes", e);
                showAlert("Error", "Failed to load programs or sedes: " + e.getMessage());
                return;
            }

            grid.add(new Label("Student Code:"), 0, 0);
            grid.add(codEstudiante, 1, 0);
            grid.add(new Label("Document Number:"), 0, 1);
            grid.add(numeroDocumento, 1, 1);
            grid.add(new Label("Document Type:"), 0, 2);
            grid.add(tipoDocumento, 1, 2);
            grid.add(new Label("Names:"), 0, 3);
            grid.add(nombres, 1, 3);
            grid.add(new Label("Last Names:"), 0, 4);
            grid.add(apellidos, 1, 4);
            grid.add(new Label("Email:"), 0, 5);
            grid.add(correoInstitucional, 1, 5);
            grid.add(new Label("Phone:"), 0, 6);
            grid.add(telefono, 1, 6);
            grid.add(new Label("Entry Date:"), 0, 7);
            grid.add(fechaIngreso, 1, 7);
            grid.add(new Label("Program:"), 0, 8);
            grid.add(programCombo, 1, 8);
            grid.add(new Label("Sede:"), 0, 9);
            grid.add(sedeCombo, 1, 9);

            dialog.getDialogPane().setContent(grid);

            // Convert the result when the add button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    Student student = new Student();
                    student.setCodEstudiante(codEstudiante.getText());
                    student.setNumeroDocumento(numeroDocumento.getText());
                    student.setTipoDocumento(tipoDocumento.getValue());
                    student.setNombres(nombres.getText());
                    student.setApellidos(apellidos.getText());
                    student.setCorreoInstitucional(correoInstitucional.getText());
                    student.setTelefono(telefono.getText());
                    student.setFechaIngreso(fechaIngreso.getValue());
                    student.setPrograma(programCombo.getValue());
                    student.setSede(sedeCombo.getValue());
                    student.setActivo(true);
                    student.setNivelRiesgo(0);
                    return student;
                }
                return null;
            });

            Optional<Student> result = dialog.showAndWait();
            result.ifPresent(student -> {
                try {
                    // Validate required fields
                    if (student.getCodEstudiante() == null || student.getCodEstudiante().trim().isEmpty() ||
                        student.getNumeroDocumento() == null || student.getNumeroDocumento().trim().isEmpty() ||
                        student.getNombres() == null || student.getNombres().trim().isEmpty() ||
                        student.getApellidos() == null || student.getApellidos().trim().isEmpty() ||
                        student.getCorreoInstitucional() == null || student.getCorreoInstitucional().trim().isEmpty() ||
                        student.getPrograma() == null || student.getSede() == null) {

                        showAlert("Validation Error", "Please fill in all required fields.");
                        return;
                    }

                    // Add student through the service
                    academicService.addStudent(student);
                    showAlert("Success", "Student added successfully!");
                    loadStudents(); // Refresh the table
                } catch (Exception e) {
                    logger.error("Error adding student", e);
                    showAlert("Error", "Failed to add student: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error in handleAddStudent", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to edit.");
            return;
        }

        try {
            // Create the dialog
            Dialog<Student> dialog = new Dialog<>();
            dialog.setTitle("Edit Student");
            dialog.setHeaderText("Edit student information");

            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField codEstudiante = new TextField(selectedStudent.getCodEstudiante());
            codEstudiante.setDisable(true); // Can't change student code
            TextField numeroDocumento = new TextField(selectedStudent.getNumeroDocumento());
            ComboBox<String> tipoDocumento = new ComboBox<>();
            tipoDocumento.getItems().addAll("CC", "TI", "CE", "PA");
            tipoDocumento.setValue(selectedStudent.getTipoDocumento());
            TextField nombres = new TextField(selectedStudent.getNombres());
            TextField apellidos = new TextField(selectedStudent.getApellidos());
            TextField correoInstitucional = new TextField(selectedStudent.getCorreoInstitucional());
            TextField telefono = new TextField(selectedStudent.getTelefono());
            DatePicker fechaIngreso = new DatePicker(selectedStudent.getFechaIngreso());
            ComboBox<Program> programCombo = new ComboBox<>();
            ComboBox<Sede> sedeCombo = new ComboBox<>();

            // Load programs and sedes
            try {
                List<Program> programs = academicService.getAllPrograms();
                List<Sede> sedes = academicService.getAllSedes();

                programCombo.setItems(FXCollections.observableArrayList(programs));
                sedeCombo.setItems(FXCollections.observableArrayList(sedes));

                // Set current values
                programCombo.setValue(selectedStudent.getPrograma());
                sedeCombo.setValue(selectedStudent.getSede());

                // Set cell factories
                programCombo.setButtonCell(new ListCell<Program>() {
                    @Override
                    protected void updateItem(Program item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getCodigoPrograma() + " - " + item.getNombre());
                    }
                });

                programCombo.setCellFactory(lv -> new ListCell<Program>() {
                    @Override
                    protected void updateItem(Program item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getCodigoPrograma() + " - " + item.getNombre());
                    }
                });

                sedeCombo.setButtonCell(new ListCell<Sede>() {
                    @Override
                    protected void updateItem(Sede item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getNombre());
                    }
                });

                sedeCombo.setCellFactory(lv -> new ListCell<Sede>() {
                    @Override
                    protected void updateItem(Sede item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getNombre());
                    }
                });

            } catch (Exception e) {
                logger.error("Error loading data for comboboxes", e);
                showAlert("Error", "Failed to load programs or sedes: " + e.getMessage());
                return;
            }

            grid.add(new Label("Student Code:"), 0, 0);
            grid.add(codEstudiante, 1, 0);
            grid.add(new Label("Document Number:"), 0, 1);
            grid.add(numeroDocumento, 1, 1);
            grid.add(new Label("Document Type:"), 0, 2);
            grid.add(tipoDocumento, 1, 2);
            grid.add(new Label("Names:"), 0, 3);
            grid.add(nombres, 1, 3);
            grid.add(new Label("Last Names:"), 0, 4);
            grid.add(apellidos, 1, 4);
            grid.add(new Label("Email:"), 0, 5);
            grid.add(correoInstitucional, 1, 5);
            grid.add(new Label("Phone:"), 0, 6);
            grid.add(telefono, 1, 6);
            grid.add(new Label("Entry Date:"), 0, 7);
            grid.add(fechaIngreso, 1, 7);
            grid.add(new Label("Program:"), 0, 8);
            grid.add(programCombo, 1, 8);
            grid.add(new Label("Sede:"), 0, 9);
            grid.add(sedeCombo, 1, 9);

            dialog.getDialogPane().setContent(grid);

            // Convert the result when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    selectedStudent.setNumeroDocumento(numeroDocumento.getText());
                    selectedStudent.setTipoDocumento(tipoDocumento.getValue());
                    selectedStudent.setNombres(nombres.getText());
                    selectedStudent.setApellidos(apellidos.getText());
                    selectedStudent.setCorreoInstitucional(correoInstitucional.getText());
                    selectedStudent.setTelefono(telefono.getText());
                    selectedStudent.setFechaIngreso(fechaIngreso.getValue());
                    selectedStudent.setPrograma(programCombo.getValue());
                    selectedStudent.setSede(sedeCombo.getValue());

                    // CRITICAL FIX: Set the IDs needed for the database update
                    if (programCombo.getValue() != null) {
                        selectedStudent.setCodPrograma(programCombo.getValue().getCodPrograma());
                    }
                    if (sedeCombo.getValue() != null) {
                        selectedStudent.setIdSede(sedeCombo.getValue().getIdSede());
                    }

                    return selectedStudent;
                }
                return null;
            });

            Optional<Student> result = dialog.showAndWait();
            result.ifPresent(student -> {
                try {
                    // Update student through the service
                    academicService.updateStudent(student);
                    showAlert("Success", "Student updated successfully!");
                    loadStudents(); // Refresh the table
                } catch (Exception e) {
                    logger.error("Error updating student", e);
                    showAlert("Error", "Failed to update student: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error in handleEditStudent", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteStudent() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Student");
        confirmAlert.setContentText("Are you sure you want to delete student: " +
            selectedStudent.getNombres() + " " + selectedStudent.getApellidos() + "?\n\n" +
            "This will deactivate the student account.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                academicService.deleteStudent(selectedStudent.getCodEstudiante());
                showAlert("Success", "Student deactivated successfully!");
                loadStudents(); // Refresh the table
            } catch (Exception e) {
                logger.error("Error deleting student", e);
                showAlert("Error", "Failed to delete student: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewStudentDetails() {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to view details.");
            return;
        }

        // Create detailed view dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Details");
        dialog.setHeaderText("Complete Student Information");

        // Set the button type
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Create the content
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #f8f9fa;");

        int row = 0;

        // Add student information
        addDetailRow(grid, row++, "Student Code:", selectedStudent.getCodEstudiante());
        addDetailRow(grid, row++, "Document Number:", selectedStudent.getNumeroDocumento());
        addDetailRow(grid, row++, "Document Type:", selectedStudent.getTipoDocumento());
        addDetailRow(grid, row++, "Full Name:", selectedStudent.getNombres() + " " + selectedStudent.getApellidos());
        addDetailRow(grid, row++, "Email:", selectedStudent.getCorreoInstitucional());
        addDetailRow(grid, row++, "Phone:", selectedStudent.getTelefono());
        addDetailRow(grid, row++, "Entry Date:", selectedStudent.getFechaIngreso() != null ? selectedStudent.getFechaIngreso().toString() : "N/A");
        addDetailRow(grid, row++, "Program:", selectedStudent.getPrograma() != null ? selectedStudent.getPrograma().getNombre() : "N/A");
        addDetailRow(grid, row++, "Sede:", selectedStudent.getSede() != null ? selectedStudent.getSede().getNombre() : "N/A");
        addDetailRow(grid, row++, "Risk Level:", String.valueOf(selectedStudent.getNivelRiesgo()));
        addDetailRow(grid, row++, "Status:", selectedStudent.isActivo() ? "Active" : "Inactive");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    // ==================== Teacher UI Handlers ====================

    @FXML
    private void handleAddTeacher() {
        try {
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle("Add New Teacher");
            dialog.setHeaderText("Enter teacher information");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField numeroDocumento = new TextField();
            numeroDocumento.setPromptText("Document Number");
            ComboBox<String> tipoDocumento = new ComboBox<>();
            tipoDocumento.getItems().addAll("CC", "TI", "CE", "PA");
            tipoDocumento.setValue("CC");
            TextField nombres = new TextField();
            nombres.setPromptText("Names");
            TextField apellidos = new TextField();
            apellidos.setPromptText("Last Names");
            TextField correoInstitucional = new TextField();
            correoInstitucional.setPromptText("Institutional Email");
            TextField telefono = new TextField();
            telefono.setPromptText("Phone");

            grid.add(new Label("Document Number:"), 0, 0);
            grid.add(numeroDocumento, 1, 0);
            grid.add(new Label("Document Type:"), 0, 1);
            grid.add(tipoDocumento, 1, 1);
            grid.add(new Label("Names:"), 0, 2);
            grid.add(nombres, 1, 2);
            grid.add(new Label("Last Names:"), 0, 3);
            grid.add(apellidos, 1, 3);
            grid.add(new Label("Email:"), 0, 4);
            grid.add(correoInstitucional, 1, 4);
            grid.add(new Label("Phone:"), 0, 5);
            grid.add(telefono, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        Teacher created = academicService.createTeacher(
                                numeroDocumento.getText(),
                                tipoDocumento.getValue(),
                                nombres.getText(),
                                apellidos.getText(),
                                correoInstitucional.getText(),
                                telefono.getText()
                        );
                        return created;
                    } catch (Exception e) {
                        logger.error("Error creating teacher", e);
                        showAlert("Error", "Failed to create teacher: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Teacher> result = dialog.showAndWait();
            result.ifPresent(teacher -> {
                showAlert("Success", "Teacher added successfully!");
                loadTeachers();
            });

        } catch (Exception e) {
            logger.error("Error in handleAddTeacher", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a teacher to edit.");
            return;
        }

        try {
            Dialog<Teacher> dialog = new Dialog<>();
            dialog.setTitle("Edit Teacher");
            dialog.setHeaderText("Edit teacher information");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField numeroDocumento = new TextField(selected.getNumeroDocumento());
            ComboBox<String> tipoDocumento = new ComboBox<>();
            tipoDocumento.getItems().addAll("CC", "TI", "CE", "PA");
            tipoDocumento.setValue(selected.getTipoDocumento() != null ? selected.getTipoDocumento() : "CC");
            TextField nombres = new TextField(selected.getNombres());
            TextField apellidos = new TextField(selected.getApellidos());
            TextField correoInstitucional = new TextField(selected.getCorreoInstitucional());
            TextField telefono = new TextField(selected.getTelefono());

            grid.add(new Label("Document Number:"), 0, 0);
            grid.add(numeroDocumento, 1, 0);
            grid.add(new Label("Document Type:"), 0, 1);
            grid.add(tipoDocumento, 1, 1);
            grid.add(new Label("Names:"), 0, 2);
            grid.add(nombres, 1, 2);
            grid.add(new Label("Last Names:"), 0, 3);
            grid.add(apellidos, 1, 3);
            grid.add(new Label("Email:"), 0, 4);
            grid.add(correoInstitucional, 1, 4);
            grid.add(new Label("Phone:"), 0, 5);
            grid.add(telefono, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    selected.setNumeroDocumento(numeroDocumento.getText());
                    selected.setTipoDocumento(tipoDocumento.getValue());
                    selected.setNombres(nombres.getText());
                    selected.setApellidos(apellidos.getText());
                    selected.setCorreoInstitucional(correoInstitucional.getText());
                    selected.setTelefono(telefono.getText());

                    try {
                        academicService.updateTeacher(selected);
                    } catch (Exception e) {
                        logger.error("Error updating teacher", e);
                        showAlert("Error", "Failed to update teacher: " + e.getMessage());
                    }
                    return selected;
                }
                return null;
            });

            Optional<Teacher> result = dialog.showAndWait();
            result.ifPresent(t -> {
                showAlert("Success", "Teacher updated successfully!");
                loadTeachers();
            });

        } catch (Exception e) {
            logger.error("Error in handleEditTeacher", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTeacher() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a teacher to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate Teacher");
        confirm.setContentText("Are you sure you want to deactivate teacher: " +
                selected.getNombres() + " " + selected.getApellidos() + "?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                academicService.deleteTeacher(selected.getIdDocente());
                showAlert("Success", "Teacher deactivated successfully!");
                loadTeachers();
            } catch (Exception e) {
                logger.error("Error deleting teacher", e);
                showAlert("Error", "Failed to delete teacher: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewTeacherDetails() {
        Teacher selected = teacherTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a teacher to view details.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Teacher Details");
        dialog.setHeaderText("Complete Teacher Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        addDetailRow(grid, row++, "Teacher ID:", selected.getIdDocente() != null ? selected.getIdDocente().toString() : "N/A");
        addDetailRow(grid, row++, "Document Number:", selected.getNumeroDocumento());
        addDetailRow(grid, row++, "Document Type:", selected.getTipoDocumento());
        addDetailRow(grid, row++, "Full Name:", selected.getNombres() + " " + selected.getApellidos());
        addDetailRow(grid, row++, "Email:", selected.getCorreoInstitucional());
        addDetailRow(grid, row++, "Phone:", selected.getTelefono());
        addDetailRow(grid, row++, "Assigned Hours:", selected.getHorasAsignadas() != null ? selected.getHorasAsignadas().toString() : "0");
        addDetailRow(grid, row++, "Status:", selected.isActivo() ? "Active" : "Inactive");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.showAndWait();
    }

    // ==================== Program UI Handlers ====================

    @FXML
    private void handleAddProgram() {
        try {
            Dialog<Program> dialog = new Dialog<>();
            dialog.setTitle("Add New Program");
            dialog.setHeaderText("Enter program information");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField codigoPrograma = new TextField();
            codigoPrograma.setPromptText("Program Code (e.g. ING-SIS)");
            TextField nombre = new TextField();
            nombre.setPromptText("Program Name");
            TextField creditos = new TextField();
            creditos.setPromptText("Credits (e.g. 160)");
            TextField durationYears = new TextField();
            durationYears.setPromptText("Duration in years (e.g. 5)");

            grid.add(new Label("Code:"), 0, 0);
            grid.add(codigoPrograma, 1, 0);
            grid.add(new Label("Name:"), 0, 1);
            grid.add(nombre, 1, 1);
            grid.add(new Label("Credits:"), 0, 2);
            grid.add(creditos, 1, 2);
            grid.add(new Label("Duration (years):"), 0, 3);
            grid.add(durationYears, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        int creditsInt = Integer.parseInt(creditos.getText().trim());
                        int yearsInt = Integer.parseInt(durationYears.getText().trim());

                        Program created = academicService.createProgram(
                                codigoPrograma.getText(),
                                nombre.getText(),
                                "",
                                yearsInt,
                                creditsInt
                        );
                        return created;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter valid numeric values for credits and duration.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error creating program", e);
                        showAlert("Error", "Failed to create program: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Program> result = dialog.showAndWait();
            result.ifPresent(program -> {
                showAlert("Success", "Program added successfully!");
                loadPrograms();
            });

        } catch (Exception e) {
            logger.error("Error in handleAddProgram", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a program to edit.");
            return;
        }

        try {
            Dialog<Program> dialog = new Dialog<>();
            dialog.setTitle("Edit Program");
            dialog.setHeaderText("Edit program information");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField codigoPrograma = new TextField(selected.getCodigoPrograma());
            TextField nombre = new TextField(selected.getNombre());
            TextField creditos = new TextField(selected.getCreditosTotales() != null ? selected.getCreditosTotales().toString() : "0");
            TextField durationYears = new TextField(selected.getDuracionSemestres() != null ? String.valueOf(selected.getDuracionSemestres() / 2) : "0");

            grid.add(new Label("Code:"), 0, 0);
            grid.add(codigoPrograma, 1, 0);
            grid.add(new Label("Name:"), 0, 1);
            grid.add(nombre, 1, 1);
            grid.add(new Label("Credits:"), 0, 2);
            grid.add(creditos, 1, 2);
            grid.add(new Label("Duration (years):"), 0, 3);
            grid.add(durationYears, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        int creditsInt = Integer.parseInt(creditos.getText().trim());
                        int yearsInt = Integer.parseInt(durationYears.getText().trim());

                        selected.setCodigoPrograma(codigoPrograma.getText());
                        selected.setNombre(nombre.getText());
                        selected.setCreditosTotales(creditsInt);
                        selected.setDuracionSemestres(yearsInt * 2);

                        academicService.updateProgram(selected);
                        return selected;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter valid numeric values for credits and duration.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error updating program", e);
                        showAlert("Error", "Failed to update program: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Program> result = dialog.showAndWait();
            result.ifPresent(p -> {
                showAlert("Success", "Program updated successfully!");
                loadPrograms();
            });

        } catch (Exception e) {
            logger.error("Error in handleEditProgram", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteProgram() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a program to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Program");
        confirm.setContentText("Are you sure you want to delete program: " + selected.getNombre() + "?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                academicService.deleteProgram(selected.getCodPrograma());
                showAlert("Success", "Program deleted successfully!");
                loadPrograms();
            } catch (Exception e) {
                logger.error("Error deleting program", e);
                showAlert("Error", "Failed to delete program: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewProgramDetails() {
        Program selected = programTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a program to view details.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Program Details");
        dialog.setHeaderText("Complete Program Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        addDetailRow(grid, row++, "Program ID:", selected.getCodPrograma() != null ? selected.getCodPrograma().toString() : "N/A");
        addDetailRow(grid, row++, "Program Code:", selected.getCodigoPrograma());
        addDetailRow(grid, row++, "Name:", selected.getNombre());
        addDetailRow(grid, row++, "Credits:", selected.getCreditosTotales() != null ? selected.getCreditosTotales().toString() : "0");
        addDetailRow(grid, row++, "Duration (semesters):", selected.getDuracionSemestres() != null ? selected.getDuracionSemestres().toString() : "0");
        addDetailRow(grid, row++, "Faculty ID:", selected.getIdFacultad() != null ? selected.getIdFacultad().toString() : "N/A");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    // ==================== User UI Handlers ====================

    @FXML
    private void handleActivateAccount() {
        AccountCandidate selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a student or teacher to activate.");
            return;
        }

        try {
            String username = selected.getEmail();

            // Generate password: lowercase first name + "123"
            String fullName = selected.getName();
            String firstName = fullName.split(" ")[0]; // Get first name from full name
            String password = firstName.toLowerCase() + "123";

            User.UserRole role = selected.getType().equalsIgnoreCase("Teacher") ? User.UserRole.DOCENTE : User.UserRole.ESTUDIANTE;

            // idReferencia should be the reference id (for students it's cod_estudiante, for teachers the id_docente)
            String idReferencia = selected.getReferenceId();
            String tipoReferencia = selected.getType().equalsIgnoreCase("Teacher") ? "Docente" : "Estudiante";

            // Create user via service (will validate duplicates)
            userService.createUser(username, password, role, idReferencia, tipoReferencia);

            showAlert("Success", "Account created for " + selected.getName() + " (" + username + ") with password '" + password + "'.");
            loadAccountCandidates();
        } catch (Exception e) {
            logger.error("Error activating account", e);
            showAlert("Error", "Failed to activate account: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAccountDetails() {
        AccountCandidate selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a student or teacher to view details.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Account Candidate Details");
        dialog.setHeaderText("Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        addDetailRow(grid, row++, "Type:", selected.getType());
        addDetailRow(grid, row++, "Reference ID:", selected.getReferenceId());
        addDetailRow(grid, row++, "Full Name:", selected.getName());
        addDetailRow(grid, row++, "Email:", selected.getEmail());
        addDetailRow(grid, row++, "Status:", selected.isActive() ? "Has account" : "No account");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        loadData();
        showAlert("Success", "Data refreshed successfully");
    }
    
    @FXML
    private void handleAssignTeacher() {
        try {
            // Get all courses and teachers
            List<Course> courses = academicService.getAllCourses();
            List<Teacher> teachers = academicService.getAllTeachers();
            List<Grupo> grupos = academicService.getAllGrupos();

            if (courses.isEmpty()) {
                showAlert("No Courses", "No courses available. Please add courses first.");
                return;
            }

            if (teachers.isEmpty()) {
                showAlert("No Teachers", "No teachers available. Please add teachers first.");
                return;
            }

            // Create dialog
            Dialog<Boolean> dialog = new Dialog<>();
            dialog.setTitle("Assign Teacher to Course");
            dialog.setHeaderText("Select a course and teacher to assign");

            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Course selection
            ComboBox<Course> courseCombo = new ComboBox<>();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            courseCombo.setPromptText("Select a course");

            // Set cell factory for course display
            courseCombo.setButtonCell(new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });
            courseCombo.setCellFactory(lv -> new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });

            // Group selection (will be populated based on course selection)
            ComboBox<Grupo> grupoCombo = new ComboBox<>();
            grupoCombo.setPromptText("Select a group");
            grupoCombo.setDisable(true);

            // Set cell factory for group display
            grupoCombo.setButtonCell(new ListCell<Grupo>() {
                @Override
                protected void updateItem(Grupo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "Group " + item.getNumeroGrupo());
                }
            });
            grupoCombo.setCellFactory(lv -> new ListCell<Grupo>() {
                @Override
                protected void updateItem(Grupo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : "Group " + item.getNumeroGrupo());
                }
            });

            // When course is selected, load its groups
            courseCombo.setOnAction(e -> {
                Course selectedCourse = courseCombo.getValue();
                if (selectedCourse != null) {
                    try {
                        List<Grupo> courseGroups = academicService.getGruposByAsignatura(selectedCourse.getCourseCode());
                        grupoCombo.setItems(FXCollections.observableArrayList(courseGroups));
                        grupoCombo.setDisable(courseGroups.isEmpty());
                        if (courseGroups.isEmpty()) {
                            showAlert("No Groups", "This course has no groups. Groups are needed to assign teachers.");
                        }
                    } catch (Exception ex) {
                        logger.error("Error loading groups for course", ex);
                        showAlert("Error", "Failed to load groups: " + ex.getMessage());
                    }
                }
            });

            // Teacher selection
            ComboBox<Teacher> teacherCombo = new ComboBox<>();
            teacherCombo.setItems(FXCollections.observableArrayList(teachers));
            teacherCombo.setPromptText("Select a teacher");

            // Set cell factory for teacher display
            teacherCombo.setButtonCell(new ListCell<Teacher>() {
                @Override
                protected void updateItem(Teacher item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombres() + " " + item.getApellidos());
                }
            });
            teacherCombo.setCellFactory(lv -> new ListCell<Teacher>() {
                @Override
                protected void updateItem(Teacher item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombres() + " " + item.getApellidos());
                }
            });

            // Hours per week
            TextField horasField = new TextField("3.0");
            horasField.setPromptText("Hours per week");

            // Principal teacher checkbox
            CheckBox principalCheckBox = new CheckBox("Main Teacher");
            principalCheckBox.setSelected(true);

            grid.add(new Label("Course:"), 0, 0);
            grid.add(courseCombo, 1, 0);
            grid.add(new Label("Group:"), 0, 1);
            grid.add(grupoCombo, 1, 1);
            grid.add(new Label("Teacher:"), 0, 2);
            grid.add(teacherCombo, 1, 2);
            grid.add(new Label("Hours per week:"), 0, 3);
            grid.add(horasField, 1, 3);
            grid.add(principalCheckBox, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    if (courseCombo.getValue() == null || grupoCombo.getValue() == null || teacherCombo.getValue() == null) {
                        showAlert("Validation Error", "Please select a course, group, and teacher.");
                        return false;
                    }

                    try {
                        double horas = Double.parseDouble(horasField.getText());
                        if (horas <= 0 || horas > 40) {
                            showAlert("Validation Error", "Hours must be between 0 and 40.");
                            return false;
                        }

                        Grupo grupo = grupoCombo.getValue();
                        Teacher teacher = teacherCombo.getValue();
                        boolean esPrincipal = principalCheckBox.isSelected();

                        academicService.assignTeacherToGroup(teacher.getIdDocente(), grupo.getIdGrupo(), horas, esPrincipal);

                        showAlert("Success",
                            "Teacher " + teacher.getNombres() + " " + teacher.getApellidos() +
                            " assigned to " + courseCombo.getValue().getCourseName() +
                            " (Group " + grupo.getNumeroGrupo() + ")");

                        return true;
                    } catch (NumberFormatException ex) {
                        showAlert("Validation Error", "Please enter a valid number for hours.");
                        return false;
                    } catch (Exception ex) {
                        logger.error("Error assigning teacher", ex);
                        showAlert("Error", "Failed to assign teacher: " + ex.getMessage());
                        return false;
                    }
                }
                return false;
            });

            dialog.showAndWait();

        } catch (Exception e) {
            logger.error("Error in handleAssignTeacher", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
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

    // ==================== Group UI Handlers ====================

    @FXML
    private void handleAddGroup() {
        try {
            // Load necessary data
            List<Course> courses = academicService.getAllCourses();
            List<PeriodoAcademico> periodos = academicService.getAllPeriodos();
            List<Sede> sedes = academicService.getAllSedes();

            if (courses.isEmpty()) {
                showAlert("No Courses", "No courses available. Please add courses first.");
                return;
            }

            if (periodos.isEmpty()) {
                showAlert("No Periods", "No academic periods available. Please contact the administrator.");
                return;
            }

            if (sedes.isEmpty()) {
                showAlert("No Sedes", "No sedes available. Please contact the administrator.");
                return;
            }

            Dialog<Grupo> dialog = new Dialog<>();
            dialog.setTitle("Add New Group");
            dialog.setHeaderText("Create a new course group");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Course selection
            ComboBox<Course> courseCombo = new ComboBox<>();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            courseCombo.setPromptText("Select a course");
            courseCombo.setButtonCell(new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });
            courseCombo.setCellFactory(lv -> new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });

            // Period selection
            ComboBox<PeriodoAcademico> periodoCombo = new ComboBox<>();
            periodoCombo.setItems(FXCollections.observableArrayList(periodos));
            periodoCombo.setPromptText("Select academic period");
            periodoCombo.setButtonCell(new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });
            periodoCombo.setCellFactory(lv -> new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });

            // Sede selection
            ComboBox<Sede> sedeCombo = new ComboBox<>();
            sedeCombo.setItems(FXCollections.observableArrayList(sedes));
            sedeCombo.setPromptText("Select sede");
            sedeCombo.setButtonCell(new ListCell<Sede>() {
                @Override
                protected void updateItem(Sede item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });
            sedeCombo.setCellFactory(lv -> new ListCell<Sede>() {
                @Override
                protected void updateItem(Sede item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });

            TextField numeroGrupo = new TextField();
            numeroGrupo.setPromptText("Group number (e.g., 1, 2, 3)");
            TextField cupoMaximo = new TextField();
            cupoMaximo.setPromptText("Maximum capacity (e.g., 30)");

            grid.add(new Label("Course:"), 0, 0);
            grid.add(courseCombo, 1, 0);
            grid.add(new Label("Academic Period:"), 0, 1);
            grid.add(periodoCombo, 1, 1);
            grid.add(new Label("Sede:"), 0, 2);
            grid.add(sedeCombo, 1, 2);
            grid.add(new Label("Group Number:"), 0, 3);
            grid.add(numeroGrupo, 1, 3);
            grid.add(new Label("Maximum Capacity:"), 0, 4);
            grid.add(cupoMaximo, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        if (courseCombo.getValue() == null) {
                            showAlert("Validation Error", "Please select a course.");
                            return null;
                        }
                        if (periodoCombo.getValue() == null) {
                            showAlert("Validation Error", "Please select an academic period.");
                            return null;
                        }
                        if (sedeCombo.getValue() == null) {
                            showAlert("Validation Error", "Please select a sede.");
                            return null;
                        }
                        if (numeroGrupo.getText().trim().isEmpty()) {
                            showAlert("Validation Error", "Please enter a group number.");
                            return null;
                        }
                        if (cupoMaximo.getText().trim().isEmpty()) {
                            showAlert("Validation Error", "Please enter maximum capacity.");
                            return null;
                        }

                        int groupNumber = Integer.parseInt(numeroGrupo.getText().trim());
                        int maxCapacity = Integer.parseInt(cupoMaximo.getText().trim());

                        if (groupNumber < 1) {
                            showAlert("Validation Error", "Group number must be positive.");
                            return null;
                        }
                        if (maxCapacity < 1) {
                            showAlert("Validation Error", "Maximum capacity must be positive.");
                            return null;
                        }

                        Grupo created = academicService.createGrupo(
                                groupNumber,
                                maxCapacity,
                                courseCombo.getValue().getCourseCode(),
                                periodoCombo.getValue().getCodPeriodo(),
                                sedeCombo.getValue().getIdSede()
                        );
                        return created;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter valid numeric values.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error creating group", e);
                        showAlert("Error", "Failed to create group: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Grupo> result = dialog.showAndWait();
            result.ifPresent(g -> {
                showAlert("Success", "Group created successfully!");
                loadGroups();
            });

        } catch (Exception e) {
            logger.error("Error in handleAddGroup", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditGroup() {
        Grupo selected = groupTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a group to edit.");
            return;
        }

        try {
            List<Course> courses = academicService.getAllCourses();
            List<PeriodoAcademico> periodos = academicService.getAllPeriodos();
            List<Sede> sedes = academicService.getAllSedes();

            Dialog<Grupo> dialog = new Dialog<>();
            dialog.setTitle("Edit Group");
            dialog.setHeaderText("Edit group information");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Course selection
            ComboBox<Course> courseCombo = new ComboBox<>();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            // Find and set current course
            for (Course c : courses) {
                if (c.getCourseCode().equals(selected.getCodAsignatura())) {
                    courseCombo.setValue(c);
                    break;
                }
            }
            courseCombo.setButtonCell(new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });
            courseCombo.setCellFactory(lv -> new ListCell<Course>() {
                @Override
                protected void updateItem(Course item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCourseCode() + " - " + item.getCourseName());
                }
            });

            // Period selection
            ComboBox<PeriodoAcademico> periodoCombo = new ComboBox<>();
            periodoCombo.setItems(FXCollections.observableArrayList(periodos));
            // Find and set current period
            for (PeriodoAcademico p : periodos) {
                if (p.getCodPeriodo().equals(selected.getCodPeriodo())) {
                    periodoCombo.setValue(p);
                    break;
                }
            }
            periodoCombo.setButtonCell(new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });
            periodoCombo.setCellFactory(lv -> new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });

            // Sede selection
            ComboBox<Sede> sedeCombo = new ComboBox<>();
            sedeCombo.setItems(FXCollections.observableArrayList(sedes));
            // Find and set current sede
            for (Sede s : sedes) {
                if (s.getIdSede().equals(selected.getIdSede())) {
                    sedeCombo.setValue(s);
                    break;
                }
            }
            sedeCombo.setButtonCell(new ListCell<Sede>() {
                @Override
                protected void updateItem(Sede item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });
            sedeCombo.setCellFactory(lv -> new ListCell<Sede>() {
                @Override
                protected void updateItem(Sede item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });

            TextField numeroGrupo = new TextField(selected.getNumeroGrupo().toString());
            TextField cupoMaximo = new TextField(selected.getCupoMaximo().toString());

            grid.add(new Label("Course:"), 0, 0);
            grid.add(courseCombo, 1, 0);
            grid.add(new Label("Academic Period:"), 0, 1);
            grid.add(periodoCombo, 1, 1);
            grid.add(new Label("Sede:"), 0, 2);
            grid.add(sedeCombo, 1, 2);
            grid.add(new Label("Group Number:"), 0, 3);
            grid.add(numeroGrupo, 1, 3);
            grid.add(new Label("Maximum Capacity:"), 0, 4);
            grid.add(cupoMaximo, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        selected.setNumeroGrupo(Integer.parseInt(numeroGrupo.getText().trim()));
                        selected.setCupoMaximo(Integer.parseInt(cupoMaximo.getText().trim()));
                        if (courseCombo.getValue() != null) {
                            selected.setCodAsignatura(courseCombo.getValue().getCourseCode());
                        }
                        if (periodoCombo.getValue() != null) {
                            selected.setCodPeriodo(periodoCombo.getValue().getCodPeriodo());
                        }
                        if (sedeCombo.getValue() != null) {
                            selected.setIdSede(sedeCombo.getValue().getIdSede());
                        }

                        academicService.updateGrupo(selected);
                        return selected;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter valid numeric values.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error updating group", e);
                        showAlert("Error", "Failed to update group: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Grupo> result = dialog.showAndWait();
            result.ifPresent(g -> {
                showAlert("Success", "Group updated successfully!");
                loadGroups();
            });

        } catch (Exception e) {
            logger.error("Error in handleEditGroup", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteGroup() {
        Grupo selected = groupTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a group to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Group");
        confirm.setContentText("Are you sure you want to delete Group " + selected.getNumeroGrupo() +
                " for " + (selected.getAsignatura() != null ? selected.getAsignatura().getNombre() : selected.getCodAsignatura()) + "?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                academicService.deleteGrupo(selected.getIdGrupo());
                showAlert("Success", "Group deleted successfully!");
                loadGroups();
            } catch (Exception e) {
                logger.error("Error deleting group", e);
                showAlert("Error", "Failed to delete group: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewGroupDetails() {
        Grupo selected = groupTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a group to view details.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Group Details");
        dialog.setHeaderText("Complete Group Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        addDetailRow(grid, row++, "Group ID:", selected.getIdGrupo() != null ? selected.getIdGrupo().toString() : "N/A");
        addDetailRow(grid, row++, "Group Number:", selected.getNumeroGrupo() != null ? selected.getNumeroGrupo().toString() : "N/A");
        addDetailRow(grid, row++, "Course:", selected.getAsignatura() != null ?
                selected.getAsignatura().getNombre() : selected.getCodAsignatura());
        addDetailRow(grid, row++, "Course Code:", selected.getCodAsignatura());
        addDetailRow(grid, row++, "Academic Period:", selected.getPeriodoAcademico() != null ?
                selected.getPeriodoAcademico().getNombre() : selected.getCodPeriodo());
        addDetailRow(grid, row++, "Sede:", selected.getSede() != null ?
                selected.getSede().getNombre() : "N/A");
        addDetailRow(grid, row++, "Maximum Capacity:", selected.getCupoMaximo() != null ? selected.getCupoMaximo().toString() : "0");
        addDetailRow(grid, row++, "Enrolled Students:", selected.getCupoOcupado() != null ? selected.getCupoOcupado().toString() : "0");
        addDetailRow(grid, row++, "Available Spots:", String.valueOf(selected.getCuposDisponibles()));
        addDetailRow(grid, row++, "Status:", selected.isActivo() ? "Active" : "Inactive");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    // ==================== Matricula UI Handlers ====================

    @FXML
    private void handleCreateMatricula() {
        try {
            List<Student> students = academicService.getAllStudents();
            List<PeriodoAcademico> periodos = academicService.getAllPeriodos();

            if (students.isEmpty()) {
                showAlert("No Students", "No students available. Please add students first.");
                return;
            }

            if (periodos.isEmpty()) {
                showAlert("No Periods", "No academic periods available. Please contact the administrator.");
                return;
            }

            Dialog<Matricula> dialog = new Dialog<>();
            dialog.setTitle("Create Matricula");
            dialog.setHeaderText("Create a new student enrollment in a period");

            ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Student selection
            ComboBox<Student> studentCombo = new ComboBox<>();
            studentCombo.setItems(FXCollections.observableArrayList(students));
            studentCombo.setPromptText("Select a student");
            studentCombo.setButtonCell(new ListCell<Student>() {
                @Override
                protected void updateItem(Student item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null :
                        item.getCodEstudiante() + " - " + item.getNombres() + " " + item.getApellidos());
                }
            });
            studentCombo.setCellFactory(lv -> new ListCell<Student>() {
                @Override
                protected void updateItem(Student item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null :
                        item.getCodEstudiante() + " - " + item.getNombres() + " " + item.getApellidos());
                }
            });

            // Period selection
            ComboBox<PeriodoAcademico> periodoCombo = new ComboBox<>();
            periodoCombo.setItems(FXCollections.observableArrayList(periodos));
            periodoCombo.setPromptText("Select academic period");
            periodoCombo.setButtonCell(new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });
            periodoCombo.setCellFactory(lv -> new ListCell<PeriodoAcademico>() {
                @Override
                protected void updateItem(PeriodoAcademico item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getCodPeriodo() + " - " + item.getNombre());
                }
            });

            // Status selection
            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("activa", "inactiva", "cancelada", "completada");
            statusCombo.setValue("activa");

            TextField creditosField = new TextField("0");
            creditosField.setPromptText("Total credits");

            grid.add(new Label("Student:"), 0, 0);
            grid.add(studentCombo, 1, 0);
            grid.add(new Label("Academic Period:"), 0, 1);
            grid.add(periodoCombo, 1, 1);
            grid.add(new Label("Status:"), 0, 2);
            grid.add(statusCombo, 1, 2);
            grid.add(new Label("Total Credits:"), 0, 3);
            grid.add(creditosField, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == createButtonType) {
                    try {
                        if (studentCombo.getValue() == null) {
                            showAlert("Validation Error", "Please select a student.");
                            return null;
                        }
                        if (periodoCombo.getValue() == null) {
                            showAlert("Validation Error", "Please select an academic period.");
                            return null;
                        }

                        int creditos = Integer.parseInt(creditosField.getText().trim());

                        Matricula created = academicService.createMatricula(
                                studentCombo.getValue().getCodEstudiante(),
                                periodoCombo.getValue().getCodPeriodo(),
                                creditos,
                                statusCombo.getValue()
                        );
                        return created;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter a valid number for credits.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error creating matricula", e);
                        showAlert("Error", "Failed to create matricula: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Matricula> result = dialog.showAndWait();
            result.ifPresent(m -> {
                showAlert("Success", "Matricula created successfully!");
                loadMatriculas();
            });

        } catch (Exception e) {
            logger.error("Error in handleCreateMatricula", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditMatricula() {
        Matricula selected = matriculaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a matricula to edit.");
            return;
        }

        try {
            Dialog<Matricula> dialog = new Dialog<>();
            dialog.setTitle("Edit Matricula");
            dialog.setHeaderText("Edit matricula information");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            Label studentLabel = new Label(selected.getCodEstudiante() +
                (selected.getEstudiante() != null ? " - " + selected.getEstudiante().getNombres() + " " +
                selected.getEstudiante().getApellidos() : ""));
            Label periodLabel = new Label(selected.getCodPeriodo() +
                (selected.getPeriodoAcademico() != null ? " - " + selected.getPeriodoAcademico().getNombre() : ""));

            ComboBox<String> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll("activa", "inactiva", "cancelada", "completada");
            statusCombo.setValue(selected.getEstado() != null ? selected.getEstado() : "activa");

            TextField creditosField = new TextField(selected.getTotalCreditos() != null ?
                selected.getTotalCreditos().toString() : "0");

            grid.add(new Label("Student:"), 0, 0);
            grid.add(studentLabel, 1, 0);
            grid.add(new Label("Academic Period:"), 0, 1);
            grid.add(periodLabel, 1, 1);
            grid.add(new Label("Status:"), 0, 2);
            grid.add(statusCombo, 1, 2);
            grid.add(new Label("Total Credits:"), 0, 3);
            grid.add(creditosField, 1, 3);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        selected.setEstado(statusCombo.getValue());
                        selected.setTotalCreditos(Integer.parseInt(creditosField.getText().trim()));

                        academicService.updateMatricula(selected);
                        return selected;
                    } catch (NumberFormatException nfe) {
                        showAlert("Validation Error", "Please enter a valid number for credits.");
                        return null;
                    } catch (Exception e) {
                        logger.error("Error updating matricula", e);
                        showAlert("Error", "Failed to update matricula: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            Optional<Matricula> result = dialog.showAndWait();
            result.ifPresent(m -> {
                showAlert("Success", "Matricula updated successfully!");
                loadMatriculas();
            });

        } catch (Exception e) {
            logger.error("Error in handleEditMatricula", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteMatricula() {
        Matricula selected = matriculaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a matricula to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Matricula");
        confirm.setContentText("Are you sure you want to delete matricula ID " + selected.getIdMatricula() +
                " for student " + selected.getCodEstudiante() + "?");

        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                academicService.deleteMatricula(selected.getIdMatricula());
                showAlert("Success", "Matricula deleted successfully!");
                loadMatriculas();
            } catch (Exception e) {
                logger.error("Error deleting matricula", e);
                showAlert("Error", "Failed to delete matricula: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleViewMatriculaDetails() {
        Matricula selected = matriculaTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a matricula to view details.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Matricula Details");
        dialog.setHeaderText("Complete Matricula Information");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        int row = 0;
        addDetailRow(grid, row++, "Matricula ID:", selected.getIdMatricula() != null ? selected.getIdMatricula().toString() : "N/A");
        addDetailRow(grid, row++, "Student Code:", selected.getCodEstudiante());
        addDetailRow(grid, row++, "Student Name:", selected.getEstudiante() != null ?
                selected.getEstudiante().getNombres() + " " + selected.getEstudiante().getApellidos() : "N/A");
        addDetailRow(grid, row++, "Academic Period:", selected.getCodPeriodo());
        addDetailRow(grid, row++, "Period Name:", selected.getPeriodoAcademico() != null ?
                selected.getPeriodoAcademico().getNombre() : "N/A");
        addDetailRow(grid, row++, "Enrollment Date:", selected.getFechaMatricula() != null ?
                selected.getFechaMatricula().toString() : "N/A");
        addDetailRow(grid, row++, "Total Credits:", selected.getTotalCreditos() != null ?
                selected.getTotalCreditos().toString() : "0");
        addDetailRow(grid, row++, "Status:", selected.getEstado() != null ? selected.getEstado() : "N/A");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setStyle("-fx-font-size: 13px;");
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEvaluateRisks() {
        try {
            AcademicService.RiskRunInfo info = academicService.evaluateRisks();
            String message = String.format("Evaluación de riesgos ejecutada en %s. Estudiantes actualizados: %d. Posibles expulsiones: %d.",
                    info.runTimestamp != null ? info.runTimestamp.toString() : "(sin timestamp)", info.updatedCount, info.expelledCount);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Resultado evaluación de riesgos");
            alert.setHeaderText("Resultado del proceso de evaluación de riesgos");
            alert.setContentText(message);

            // Show the alert and then offer to open a dialog with recent logs
            alert.showAndWait();

            // Logs are output via DBMS_OUTPUT in the database procedure; reload students table to reflect updates
            loadStudents();

        } catch (Exception e) {
            logger.error("Error evaluating risks", e);
            showAlert("Error", "No se pudo ejecutar la evaluación de riesgos: " + e.getMessage());
        }
    }
}
