package com.academictracker.service;

import com.academictracker.dao.*;
import com.academictracker.model.*;
import com.academictracker.util.ValidationUtil;
import com.academictracker.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for academic operations (students, teachers, programs, courses, enrollments, grades)
 */
public class AcademicService {
    private static final Logger logger = LoggerFactory.getLogger(AcademicService.class);
    
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final ProgramDAO programDAO;
    private final CourseDAO courseDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final SedeDAO sedeDAO;
    private final GrupoDAO grupoDAO;
    private final DocenteGrupoDAO docenteGrupoDAO;
    private final PeriodoAcademicoDAO periodoAcademicoDAO;
    private final RiskDAO riskDAO = new RiskDAO();

    public AcademicService() {
        this.studentDAO = new StudentDAO();
        this.teacherDAO = new TeacherDAO();
        this.programDAO = new ProgramDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.sedeDAO = new SedeDAO();
        this.grupoDAO = new GrupoDAO();
        this.docenteGrupoDAO = new DocenteGrupoDAO();
        this.periodoAcademicoDAO = new PeriodoAcademicoDAO();
    }

    // ==================== Student Operations ====================
    
    /**
     * Add a new student to the system
     */
    public Student addStudent(Student student) throws Exception {
        // Validate required fields
        if (student.getCodEstudiante() == null || student.getCodEstudiante().trim().isEmpty()) {
            throw new IllegalArgumentException("Student code is required");
        }

        if (student.getNumeroDocumento() == null || student.getNumeroDocumento().trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required");
        }

        if (!ValidationUtil.isNotEmpty(student.getNombres()) || !ValidationUtil.isNotEmpty(student.getApellidos())) {
            throw new IllegalArgumentException("Names and last names are required");
        }

        if (!ValidationUtil.isNotEmpty(student.getCorreoInstitucional())) {
            throw new IllegalArgumentException("Institutional email is required");
        }

        if (student.getCodPrograma() == null) {
            throw new IllegalArgumentException("Program is required");
        }

        if (student.getIdSede() == null) {
            throw new IllegalArgumentException("Sede is required");
        }

        // Check if student code already exists
        Optional<Student> existing = studentDAO.findById(student.getCodEstudiante());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Student with this code already exists");
        }

        // Check if document number already exists
        Optional<Student> existingByDoc = studentDAO.findByDocumento(student.getNumeroDocumento());
        if (existingByDoc.isPresent()) {
            throw new IllegalArgumentException("Student with this document number already exists");
        }

        return studentDAO.create(student);
    }

    public Student createStudent(String codEstudiante, String numeroDocumento, String tipoDocumento,
                                String nombres, String apellidos, String correoInstitucional,
                                LocalDate fechaIngreso, Long codPrograma, Long idSede) throws Exception {
        if (!ValidationUtil.isNotEmpty(nombres) || !ValidationUtil.isNotEmpty(apellidos)) {
            throw new IllegalArgumentException("Nombres y apellidos son requeridos");
        }
        
        if (!ValidationUtil.isNotEmpty(correoInstitucional)) {
            throw new IllegalArgumentException("Correo institucional es requerido");
        }
        
        Student student = new Student(codEstudiante, numeroDocumento, tipoDocumento, nombres, apellidos,
                                    correoInstitucional, fechaIngreso, codPrograma, idSede);

        return studentDAO.create(student);
    }
    
    public void updateStudent(Student student) throws Exception {
        if (student.getCodEstudiante() == null || student.getCodEstudiante().isEmpty()) {
            throw new IllegalArgumentException("Código de estudiante no puede estar vacío");
        }
        
        if (!ValidationUtil.isNotEmpty(student.getNombres()) ||
            !ValidationUtil.isNotEmpty(student.getApellidos())) {
            throw new IllegalArgumentException("Nombres y apellidos son requeridos");
        }
        
        studentDAO.update(student);
    }
    
    public void deleteStudent(String codEstudiante) throws SQLException {
        studentDAO.delete(codEstudiante);
    }
    
    public Optional<Student> getStudentById(String codEstudiante) throws SQLException {
        return studentDAO.findById(codEstudiante);
    }
    
    public Optional<Student> getStudentByDocumento(String numeroDocumento) throws SQLException {
        return studentDAO.findByDocumento(numeroDocumento);
    }
    
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.findAll();
    }

    public List<Student> getStudentsByProgram(Long codPrograma) throws SQLException {
        return studentDAO.findByProgram(codPrograma);
    }

    // ==================== Teacher Operations ====================
    
    public Teacher createTeacher(String numeroDocumento, String tipoDocumento,
                                String nombres, String apellidos, String correoInstitucional,
                                String telefono) throws Exception {
        if (!ValidationUtil.isNotEmpty(nombres) || !ValidationUtil.isNotEmpty(apellidos)) {
            throw new IllegalArgumentException("Nombres y apellidos son requeridos");
        }
        
        if (!ValidationUtil.isNotEmpty(correoInstitucional)) {
            throw new IllegalArgumentException("Correo institucional es requerido");
        }
        
        Teacher teacher = new Teacher();
        teacher.setNumeroDocumento(numeroDocumento);
        teacher.setTipoDocumento(tipoDocumento != null ? tipoDocumento : "CC");
        teacher.setNombres(nombres);
        teacher.setApellidos(apellidos);
        teacher.setCorreoInstitucional(correoInstitucional);
        teacher.setTelefono(telefono);
        teacher.setActivo(true);

        return teacherDAO.create(teacher);
    }
    
    public void updateTeacher(Teacher teacher) throws Exception {
        if (teacher.getIdDocente() == null) {
            throw new IllegalArgumentException("ID de docente no puede estar vacío");
        }
        
        teacherDAO.update(teacher);
    }
    
    public void deleteTeacher(Long idDocente) throws SQLException {
        teacherDAO.delete(idDocente);
    }
    
    public Optional<Teacher> getTeacherById(Long idDocente) throws SQLException {
        return teacherDAO.findById(idDocente);
    }
    
    public Optional<Teacher> getTeacherByDocumento(String numeroDocumento) throws SQLException {
        return teacherDAO.findByDocumento(numeroDocumento);
    }
    
    public List<Teacher> getAllTeachers() throws SQLException {
        return teacherDAO.findAll();
    }

    // ==================== Program Operations ====================
    
    public Program createProgram(String programCode, String programName, String description,
                                int durationYears, int creditsRequired) throws Exception {
        if (!ValidationUtil.isValidProgramCode(programCode)) {
            throw new IllegalArgumentException("Invalid program code format. Use 2-6 uppercase letters.");
        }
        
        if (!ValidationUtil.isNotEmpty(programName)) {
            throw new IllegalArgumentException("Program name is required");
        }
        
        if (durationYears < 1 || durationYears > 10) {
            throw new IllegalArgumentException("Duration must be between 1 and 10 years");
        }
        
        if (creditsRequired < 1 || creditsRequired > 500) {
            throw new IllegalArgumentException("Credits required must be between 1 and 500");
        }
        
        Program program = new Program();
        program.setProgramCode(programCode);
        program.setProgramName(programName);
        program.setDescription(description);
        program.setDurationYears(durationYears);
        program.setCreditsRequired(creditsRequired);
        
        return programDAO.create(program);
    }
    
    public void updateProgram(Program program) throws Exception {
        if (program.getProgramId() == null) {
            throw new IllegalArgumentException("Program ID cannot be null");
        }
        
        programDAO.update(program);
    }
    
    public void deleteProgram(Long programId) throws SQLException {
        programDAO.delete(programId);
    }
    
    public Optional<Program> getProgramById(Long programId) throws SQLException {
        return programDAO.findById(programId);
    }
    
    public List<Program> getAllPrograms() throws SQLException {
        return programDAO.findAll();
    }

    // ==================== Course Operations ====================
    
    public Course createCourse(String courseCode, String courseName, String description,
                              int credits, Long programId, Long teacherId, 
                              String semester, int maxStudents) throws Exception {
        if (!ValidationUtil.isValidCourseCode(courseCode)) {
            throw new IllegalArgumentException("Invalid course code format. Use format like CS101.");
        }
        
        if (!ValidationUtil.isNotEmpty(courseName)) {
            throw new IllegalArgumentException("Course name is required");
        }
        
        if (credits < 1 || credits > 10) {
            throw new IllegalArgumentException("Credits must be between 1 and 10");
        }
        
        if (maxStudents < 1 || maxStudents > 100) {
            throw new IllegalArgumentException("Max students must be between 1 and 100");
        }
        
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setDescription(description);
        course.setCredits(credits);
        course.setProgramId(programId);
        course.setTeacherId(teacherId);
        course.setSemester(semester);
        course.setMaxStudents(maxStudents);
        
        return courseDAO.create(course);
    }
    
    public void updateCourse(Course course) throws Exception {
        if (course.getCourseId() == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }
        
        courseDAO.update(course);
    }
    
    public void deleteCourse(Long courseId) throws SQLException {
        courseDAO.delete(courseId);
    }
    
    public Optional<Course> getCourseById(Long courseId) throws SQLException {
        return courseDAO.findById(courseId);
    }
    
    public List<Course> getAllCourses() throws SQLException {
        return courseDAO.findAll();
    }
    
    public List<Course> getCoursesByProgramId(Long programId) throws SQLException {
        return courseDAO.findByProgram(programId);
    }
    
    public List<Course> getCoursesByTeacherId(Long teacherId) throws SQLException {
        // Since findByTeacherId doesn't exist in CourseDAO, return all courses for now
        // This method needs to be implemented in CourseDAO if teacher assignment is needed
        return courseDAO.findAll();
    }

    // ==================== Enrollment Operations ====================
    
    public Enrollment enrollStudent(Long studentId, Long courseId) throws Exception {
        // Convert studentId to string for our new database structure
        String codEstudiante = studentId.toString();

        // Check if already enrolled
        List<Enrollment> studentEnrollments = enrollmentDAO.findByStudent(codEstudiante);
        Optional<Enrollment> existing = studentEnrollments.stream()
            .filter(e -> e.getCourseId() != null && e.getCourseId().equals(courseId))
            .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
            .findFirst();

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }
        
        // Check course capacity
        List<Enrollment> courseEnrollments = enrollmentDAO.findByCourse(courseId);
        Optional<Course> courseOpt = courseDAO.findById(courseId);
        
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            long activeEnrollments = courseEnrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                .count();
            
            if (activeEnrollments >= course.getMaxStudents()) {
                throw new IllegalArgumentException("Course is full");
            }
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(codEstudiante);
        enrollment.setCourseId(courseId);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        
        return enrollmentDAO.create(enrollment);
    }
    
    public void updateEnrollment(Enrollment enrollment) throws Exception {
        if (enrollment.getEnrollmentId() == null) {
            throw new IllegalArgumentException("Enrollment ID cannot be null");
        }
        
        enrollmentDAO.update(enrollment);
    }
    
    public void deleteEnrollment(Long enrollmentId) throws SQLException {
        enrollmentDAO.delete(enrollmentId);
    }

    public Optional<Enrollment> getEnrollmentById(Long enrollmentId) throws SQLException {
        return enrollmentDAO.findById(enrollmentId);
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentId) throws SQLException {
        return enrollmentDAO.findByStudent(studentId);
    }

    public List<Enrollment> getEnrollmentsByCourse(Long courseId) throws SQLException {
        return enrollmentDAO.findByCourse(courseId);
    }

    // ==================== Grade Operations ====================

    public Grade createGrade(Long enrollmentId, Double gradeValue, String comments, Long gradedBy) throws Exception {
        if (gradeValue < 0 || gradeValue > 5.0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 5.0");
        }

        Grade grade = new Grade();
        grade.setEnrollmentId(enrollmentId);
        grade.setGradeValue(gradeValue);
        grade.setComments(comments);
        grade.setGradedBy(gradedBy);

        return gradeDAO.create(grade);
    }

    public void updateGrade(Grade grade) throws Exception {
        if (grade.getGradeId() == null) {
            throw new IllegalArgumentException("Grade ID cannot be null");
        }

        if (grade.getGradeValue() < 0 || grade.getGradeValue() > 5.0) {
            throw new IllegalArgumentException("Grade value must be between 0 and 5.0");
        }

        gradeDAO.update(grade);
    }

    public void deleteGrade(Long gradeId) throws SQLException {
        gradeDAO.delete(gradeId);
    }

    public Optional<Grade> getGradeById(Long gradeId) throws SQLException {
        return gradeDAO.findById(gradeId);
    }

    public List<Grade> getGradesByEnrollment(Long enrollmentId) throws SQLException {
        return gradeDAO.findByEnrollment(enrollmentId);
    }

    public List<Grade> getGradesByStudent(String studentId) throws SQLException {
        return gradeDAO.findByStudent(studentId);
    }

    // ==================== Academic Analytics ====================

    public double calculateStudentGPA(String studentId) throws SQLException {
        List<Grade> grades = gradeDAO.findByStudent(studentId);

        if (grades.isEmpty()) {
            return 0.0;
        }

        double totalPoints = grades.stream()
            .filter(g -> g.getGradeValue() != null)
            .mapToDouble(Grade::getGradeValue)
            .sum();

        return totalPoints / grades.size();
    }

    public long getActiveEnrollmentCount(Long courseId) throws SQLException {
        return enrollmentDAO.findByCourse(courseId).stream()
            .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
            .count();
    }

    public boolean isStudentEnrolledInCourse(String studentId, Long courseId) throws SQLException {
        List<Enrollment> enrollments = enrollmentDAO.findByStudent(studentId);
        return enrollments.stream()
            .anyMatch(e -> e.getCourseId() != null &&
                          e.getCourseId().equals(courseId) &&
                          e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE);
    }

    // ==================== Sede Operations ====================

    /**
     * Get all sedes
     */
    public List<Sede> getAllSedes() throws SQLException {
        return sedeDAO.findAll();
    }

    public Optional<Sede> getSedeById(Long idSede) throws SQLException {
        return sedeDAO.findById(idSede);
    }

    // ==================== Grupo Operations ====================

    /**
     * Get all course groups
     */
    public List<Grupo> getAllGrupos() throws SQLException {
        return grupoDAO.findAll();
    }

    /**
     * Get groups for a specific course (asignatura)
     */
    public List<Grupo> getGruposByAsignatura(String codAsignatura) throws SQLException {
        return grupoDAO.findByAsignatura(codAsignatura);
    }

    /**
     * Create a new group
     */
    public Grupo createGrupo(int numeroGrupo, int cupoMaximo, String codAsignatura, String codPeriodo, Long idSede) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setNumeroGrupo(numeroGrupo);
        grupo.setCupoMaximo(cupoMaximo);
        grupo.setCupoOcupado(0);
        grupo.setCodAsignatura(codAsignatura);
        grupo.setCodPeriodo(codPeriodo);
        grupo.setIdSede(idSede);
        grupo.setActivo(true);

        return grupoDAO.create(grupo);
    }

    /**
     * Update an existing group
     */
    public boolean updateGrupo(Grupo grupo) throws SQLException {
        return grupoDAO.update(grupo);
    }

    /**
     * Delete a group
     */
    public boolean deleteGrupo(Long idGrupo) throws SQLException {
        return grupoDAO.delete(idGrupo);
    }

    // ==================== Matricula Operations ====================

    /**
     * Get all matriculas
     */
    public List<Matricula> getAllMatriculas() throws SQLException {
        MatriculaDAO matriculaDAO = new MatriculaDAO();
        return matriculaDAO.findAll();
    }

    /**
     * Create a new matricula
     */
    public Matricula createMatricula(String codEstudiante, String codPeriodo, int totalCreditos, String estado) throws SQLException {
        MatriculaDAO matriculaDAO = new MatriculaDAO();
        Matricula matricula = new Matricula(codEstudiante, codPeriodo);
        matricula.setTotalCreditos(totalCreditos);
        matricula.setEstado(estado);
        return matriculaDAO.create(matricula);
    }

    /**
     * Update an existing matricula
     */
    public boolean updateMatricula(Matricula matricula) throws SQLException {
        MatriculaDAO matriculaDAO = new MatriculaDAO();
        return matriculaDAO.update(matricula);
    }

    /**
     * Delete a matricula
     */
    public boolean deleteMatricula(Long idMatricula) throws SQLException {
        MatriculaDAO matriculaDAO = new MatriculaDAO();
        return matriculaDAO.delete(idMatricula);
    }

    // ==================== Teacher-Course Assignment Operations ====================

    /**
     * Assign a teacher to a course group
     */
    public void assignTeacherToGroup(Long idDocente, Long idGrupo, double horasGrupo, boolean esPrincipal) throws Exception {
        // Validate teacher exists
        Optional<Teacher> teacher = teacherDAO.findById(idDocente);
        if (!teacher.isPresent()) {
            throw new IllegalArgumentException("Teacher not found");
        }

        // Validate group exists
        Optional<Grupo> grupo = grupoDAO.findById(idGrupo);
        if (!grupo.isPresent()) {
            throw new IllegalArgumentException("Group not found");
        }

        // Check if already assigned
        if (docenteGrupoDAO.isTeacherAssignedToGroup(idDocente, idGrupo)) {
            throw new IllegalArgumentException("Teacher is already assigned to this group");
        }

        docenteGrupoDAO.assignTeacherToGroup(idDocente, idGrupo, horasGrupo, esPrincipal);
        logger.info("Teacher {} assigned to group {}", idDocente, idGrupo);
    }

    /**
     * Remove a teacher from a course group
     */
    public void removeTeacherFromGroup(Long idDocente, Long idGrupo) throws SQLException {
        docenteGrupoDAO.removeTeacherFromGroup(idDocente, idGrupo);
        logger.info("Teacher {} removed from group {}", idDocente, idGrupo);
    }

    /**
     * Get all teachers assigned to a group with details
     */
    public List<DocenteGrupoDAO.TeacherGroupAssignment> getTeacherAssignments(Long idGrupo) throws SQLException {
        return docenteGrupoDAO.getTeacherAssignmentsWithDetails(idGrupo);
    }

    /**
     * Check if a teacher is assigned to a group
     */
    public boolean isTeacherAssignedToGroup(Long idDocente, Long idGrupo) throws SQLException {
        return docenteGrupoDAO.isTeacherAssignedToGroup(idDocente, idGrupo);
    }

    // ==================== Periodo Academico Operations ====================

    /**
     * Get all academic periods
     */
    public List<PeriodoAcademico> getAllPeriodos() throws SQLException {
        return periodoAcademicoDAO.findAll();
    }

    /**
     * Get academic period by code
     */
    public Optional<PeriodoAcademico> getPeriodoById(String codPeriodo) throws SQLException {
        return periodoAcademicoDAO.findById(codPeriodo);
    }

    public RiskRunInfo evaluateRisks() throws SQLException {
        RiskDAO.RiskRunResult res = riskDAO.evaluateRisks();
        return new RiskRunInfo(res.runTimestamp, res.updatedCount, res.expelledCount);
    }

    public static class RiskRunInfo {
        public final java.sql.Timestamp runTimestamp;
        public final int updatedCount;
        public final int expelledCount;

        public RiskRunInfo(java.sql.Timestamp runTimestamp, int updatedCount, int expelledCount) {
            this.runTimestamp = runTimestamp;
            this.updatedCount = updatedCount;
            this.expelledCount = expelledCount;
        }
    }

    /**
     * Run a simple report by selecting all rows from a database view.
     * Returns a list of rows where each row is a LinkedHashMap preserving column order.
     */
    public List<Map<String,Object>> runReport(String viewName) throws SQLException {
        if (viewName == null || viewName.trim().isEmpty()) throw new IllegalArgumentException("viewName is required");
        // Only allow simple identifier names (alphanumeric and underscores) to prevent SQL injection
        if (!viewName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid view name");
        }
        String sql = "SELECT * FROM " + viewName;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            List<Map<String,Object>> rows = new ArrayList<>();
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                for (int i=1;i<=cols;i++) {
                    String colName = md.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(colName, val);
                }
                rows.add(row);
            }
            return rows;
        }
    }
}
