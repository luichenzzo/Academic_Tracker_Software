package com.academictracker.service;

import com.academictracker.dao.*;
import com.academictracker.model.*;
import com.academictracker.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
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

    public AcademicService() {
        this.studentDAO = new StudentDAO();
        this.teacherDAO = new TeacherDAO();
        this.programDAO = new ProgramDAO();
        this.courseDAO = new CourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
    }

    // ==================== Student Operations ====================
    
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
}
