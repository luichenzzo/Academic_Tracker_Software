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
    
    public Student createStudent(Long userId, String firstName, String lastName, 
                                LocalDate dateOfBirth, String phone, String address) throws Exception {
        if (!ValidationUtil.isNotEmpty(firstName) || !ValidationUtil.isNotEmpty(lastName)) {
            throw new IllegalArgumentException("First name and last name are required");
        }
        
        if (phone != null && !phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        
        Student student = new Student();
        student.setUserId(userId);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setDateOfBirth(dateOfBirth);
        student.setPhone(phone);
        student.setAddress(address);
        student.setEnrollmentDate(LocalDate.now());
        
        return studentDAO.create(student);
    }
    
    public void updateStudent(Student student) throws Exception {
        if (student.getStudentId() == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (!ValidationUtil.isNotEmpty(student.getFirstName()) || 
            !ValidationUtil.isNotEmpty(student.getLastName())) {
            throw new IllegalArgumentException("First name and last name are required");
        }
        
        studentDAO.update(student);
    }
    
    public void deleteStudent(Long studentId) throws SQLException {
        studentDAO.delete(studentId);
    }
    
    public Optional<Student> getStudentById(Long studentId) throws SQLException {
        return studentDAO.findById(studentId);
    }
    
    public Optional<Student> getStudentByUserId(Long userId) throws SQLException {
        return studentDAO.findByUserId(userId);
    }
    
    public List<Student> getAllStudents() throws SQLException {
        return studentDAO.findAll();
    }

    // ==================== Teacher Operations ====================
    
    public Teacher createTeacher(Long userId, String firstName, String lastName, 
                                String department, String phone) throws Exception {
        if (!ValidationUtil.isNotEmpty(firstName) || !ValidationUtil.isNotEmpty(lastName)) {
            throw new IllegalArgumentException("First name and last name are required");
        }
        
        if (phone != null && !phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        
        Teacher teacher = new Teacher();
        teacher.setUserId(userId);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setDepartment(department);
        teacher.setPhone(phone);
        teacher.setHireDate(LocalDate.now());
        
        return teacherDAO.create(teacher);
    }
    
    public void updateTeacher(Teacher teacher) throws Exception {
        if (teacher.getTeacherId() == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        
        teacherDAO.update(teacher);
    }
    
    public void deleteTeacher(Long teacherId) throws SQLException {
        teacherDAO.delete(teacherId);
    }
    
    public Optional<Teacher> getTeacherById(Long teacherId) throws SQLException {
        return teacherDAO.findById(teacherId);
    }
    
    public Optional<Teacher> getTeacherByUserId(Long userId) throws SQLException {
        return teacherDAO.findByUserId(userId);
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
        return courseDAO.findByProgramId(programId);
    }
    
    public List<Course> getCoursesByTeacherId(Long teacherId) throws SQLException {
        return courseDAO.findByTeacherId(teacherId);
    }

    // ==================== Enrollment Operations ====================
    
    public Enrollment enrollStudent(Long studentId, Long courseId) throws Exception {
        // Check if already enrolled
        Optional<Enrollment> existing = enrollmentDAO.findByStudentAndCourse(studentId, courseId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }
        
        // Check course capacity
        List<Enrollment> courseEnrollments = enrollmentDAO.findByCourseId(courseId);
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
        enrollment.setStudentId(studentId);
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
    
    public List<Enrollment> getEnrollmentsByStudentId(Long studentId) throws SQLException {
        return enrollmentDAO.findByStudentId(studentId);
    }
    
    public List<Enrollment> getEnrollmentsByCourseId(Long courseId) throws SQLException {
        return enrollmentDAO.findByCourseId(courseId);
    }

    // ==================== Grade Operations ====================
    
    public Grade assignGrade(Long enrollmentId, Double gradeValue, String comments, 
                           Long gradedBy) throws Exception {
        if (!ValidationUtil.isInRange(gradeValue, 0, 100)) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
        
        // Check if grade already exists for this enrollment
        Optional<Grade> existing = gradeDAO.findByEnrollmentId(enrollmentId);
        if (existing.isPresent()) {
            // Update existing grade
            Grade grade = existing.get();
            grade.setGradeValue(gradeValue);
            grade.setComments(comments);
            grade.setGradedBy(gradedBy);
            gradeDAO.update(grade);
            return grade;
        } else {
            // Create new grade
            Grade grade = new Grade();
            grade.setEnrollmentId(enrollmentId);
            grade.setGradeValue(gradeValue);
            grade.setComments(comments);
            grade.setGradedBy(gradedBy);
            
            return gradeDAO.create(grade);
        }
    }
    
    public void updateGrade(Grade grade) throws Exception {
        if (grade.getGradeId() == null) {
            throw new IllegalArgumentException("Grade ID cannot be null");
        }
        
        if (grade.getGradeValue() != null && !ValidationUtil.isInRange(grade.getGradeValue(), 0, 100)) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
        
        gradeDAO.update(grade);
    }
    
    public void deleteGrade(Long gradeId) throws SQLException {
        gradeDAO.delete(gradeId);
    }
    
    public Optional<Grade> getGradeById(Long gradeId) throws SQLException {
        return gradeDAO.findById(gradeId);
    }
    
    public Optional<Grade> getGradeByEnrollmentId(Long enrollmentId) throws SQLException {
        return gradeDAO.findByEnrollmentId(enrollmentId);
    }
    
    public List<Grade> getGradesByTeacherId(Long teacherId) throws SQLException {
        return gradeDAO.findByGradedBy(teacherId);
    }
}
