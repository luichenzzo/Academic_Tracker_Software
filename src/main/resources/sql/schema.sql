-- Academic Tracker Software - Oracle Database Schema

-- Drop tables if they exist (for clean setup)
DROP TABLE enrollments CASCADE CONSTRAINTS;
DROP TABLE grades CASCADE CONSTRAINTS;
DROP TABLE courses CASCADE CONSTRAINTS;
DROP TABLE programs CASCADE CONSTRAINTS;
DROP TABLE students CASCADE CONSTRAINTS;
DROP TABLE teachers CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;

-- Users table (for authentication)
CREATE TABLE users (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    role VARCHAR2(20) NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    email VARCHAR2(100) UNIQUE NOT NULL,
    active NUMBER(1) DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Teachers table
CREATE TABLE teachers (
    teacher_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    department VARCHAR2(100),
    phone VARCHAR2(20),
    hire_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Students table
CREATE TABLE students (
    student_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    first_name VARCHAR2(50) NOT NULL,
    last_name VARCHAR2(50) NOT NULL,
    date_of_birth DATE,
    phone VARCHAR2(20),
    address VARCHAR2(200),
    enrollment_date DATE DEFAULT SYSDATE,
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Programs table (e.g., Computer Science, Engineering)
CREATE TABLE programs (
    program_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    program_code VARCHAR2(20) UNIQUE NOT NULL,
    program_name VARCHAR2(100) NOT NULL,
    description VARCHAR2(500),
    duration_years NUMBER(2) NOT NULL,
    credits_required NUMBER(3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Courses table
CREATE TABLE courses (
    course_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_code VARCHAR2(20) UNIQUE NOT NULL,
    course_name VARCHAR2(100) NOT NULL,
    description VARCHAR2(500),
    credits NUMBER(2) NOT NULL,
    program_id NUMBER NOT NULL,
    teacher_id NUMBER,
    semester VARCHAR2(20),
    max_students NUMBER(3) DEFAULT 30,
    CONSTRAINT fk_course_program FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE CASCADE,
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

-- Enrollments table (students enrolled in courses)
CREATE TABLE enrollments (
    enrollment_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id NUMBER NOT NULL,
    course_id NUMBER NOT NULL,
    enrollment_date DATE DEFAULT SYSDATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED', 'FAILED')),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT uk_student_course UNIQUE (student_id, course_id)
);

-- Grades table
CREATE TABLE grades (
    grade_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    enrollment_id NUMBER NOT NULL,
    grade_value NUMBER(5,2) CHECK (grade_value >= 0 AND grade_value <= 100),
    grade_letter VARCHAR2(2),
    comments VARCHAR2(500),
    graded_by NUMBER,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_grade_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_teacher FOREIGN KEY (graded_by) REFERENCES teachers(teacher_id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_teacher_user ON teachers(user_id);
CREATE INDEX idx_student_user ON students(user_id);
CREATE INDEX idx_course_program ON courses(program_id);
CREATE INDEX idx_course_teacher ON courses(teacher_id);
CREATE INDEX idx_enrollment_student ON enrollments(student_id);
CREATE INDEX idx_enrollment_course ON enrollments(course_id);
CREATE INDEX idx_grade_enrollment ON grades(enrollment_id);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password_hash, role, email) 
VALUES ('admin', '$2a$10$ZJQY8LG8Z7M.XqJRXqvxfOX7LKx.xZM3VZ.9jXjqRPZ.xZM3VZ.9jX', 'ADMIN', 'admin@academictracker.com');

COMMIT;
