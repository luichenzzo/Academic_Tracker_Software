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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Admin Dashboard
 */
public class AdminDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    
    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;
    
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

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label valueNode = new Label(value != null ? value : "N/A");
        valueNode.setStyle("-fx-font-size: 13px;");
        valueNode.setWrapText(true);

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
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
