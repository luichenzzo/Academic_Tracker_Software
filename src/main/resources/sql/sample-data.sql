-- Sample Data for Academic Tracker Software
-- Run this after schema.sql to populate the database with test data

-- Insert sample programs
INSERT INTO programs (program_code, program_name, description, duration_years, credits_required) 
VALUES ('CS', 'Computer Science', 'Bachelor of Science in Computer Science', 4, 120);

INSERT INTO programs (program_code, program_name, description, duration_years, credits_required) 
VALUES ('ENG', 'Engineering', 'Bachelor of Engineering', 4, 130);

INSERT INTO programs (program_code, program_name, description, duration_years, credits_required) 
VALUES ('BUS', 'Business', 'Bachelor of Business Administration', 4, 110);

-- Insert sample teacher users (password for all: teacher123)
INSERT INTO users (username, password_hash, role, email) 
VALUES ('jsmith', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'TEACHER', 'jsmith@academictracker.com');

INSERT INTO users (username, password_hash, role, email) 
VALUES ('mjones', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'TEACHER', 'mjones@academictracker.com');

-- Insert teachers
INSERT INTO teachers (user_id, first_name, last_name, department, phone) 
VALUES ((SELECT user_id FROM users WHERE username = 'jsmith'), 'John', 'Smith', 'Computer Science', '555-0101');

INSERT INTO teachers (user_id, first_name, last_name, department, phone) 
VALUES ((SELECT user_id FROM users WHERE username = 'mjones'), 'Mary', 'Jones', 'Engineering', '555-0102');

-- Insert sample student users (password for all: student123)
INSERT INTO users (username, password_hash, role, email) 
VALUES ('agarcia', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'STUDENT', 'agarcia@academictracker.com');

INSERT INTO users (username, password_hash, role, email) 
VALUES ('bwilson', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'STUDENT', 'bwilson@academictracker.com');

INSERT INTO users (username, password_hash, role, email) 
VALUES ('clee', '$2a$10$PbpLJh9Uzk1W0HE0/HfuteX7LkGrXVDTLuOROTzVi9FpdeQVakube', 'STUDENT', 'clee@academictracker.com');

-- Insert students
INSERT INTO students (user_id, first_name, last_name, date_of_birth, phone, address) 
VALUES ((SELECT user_id FROM users WHERE username = 'agarcia'), 'Ana', 'Garcia', 
        DATE '2003-05-15', '555-1001', '123 Main St, City, State');

INSERT INTO students (user_id, first_name, last_name, date_of_birth, phone, address) 
VALUES ((SELECT user_id FROM users WHERE username = 'bwilson'), 'Bob', 'Wilson', 
        DATE '2002-08-20', '555-1002', '456 Oak Ave, City, State');

INSERT INTO students (user_id, first_name, last_name, date_of_birth, phone, address) 
VALUES ((SELECT user_id FROM users WHERE username = 'clee'), 'Carol', 'Lee', 
        DATE '2003-12-10', '555-1003', '789 Pine Rd, City, State');

-- Insert courses
INSERT INTO courses (course_code, course_name, description, credits, program_id, teacher_id, semester, max_students) 
VALUES ('CS101', 'Introduction to Programming', 'Basic programming concepts', 3, 
        (SELECT program_id FROM programs WHERE program_code = 'CS'),
        (SELECT teacher_id FROM teachers WHERE first_name = 'John' AND last_name = 'Smith'),
        'Fall 2024', 30);

INSERT INTO courses (course_code, course_name, description, credits, program_id, teacher_id, semester, max_students) 
VALUES ('CS201', 'Data Structures', 'Advanced data structures and algorithms', 4, 
        (SELECT program_id FROM programs WHERE program_code = 'CS'),
        (SELECT teacher_id FROM teachers WHERE first_name = 'John' AND last_name = 'Smith'),
        'Fall 2024', 25);

INSERT INTO courses (course_code, course_name, description, credits, program_id, teacher_id, semester, max_students) 
VALUES ('ENG101', 'Engineering Fundamentals', 'Introduction to engineering principles', 3, 
        (SELECT program_id FROM programs WHERE program_code = 'ENG'),
        (SELECT teacher_id FROM teachers WHERE first_name = 'Mary' AND last_name = 'Jones'),
        'Fall 2024', 30);

INSERT INTO courses (course_code, course_name, description, credits, program_id, teacher_id, semester, max_students) 
VALUES ('BUS101', 'Business Administration', 'Introduction to business concepts', 3, 
        (SELECT program_id FROM programs WHERE program_code = 'BUS'),
        NULL,
        'Fall 2024', 35);

-- Insert enrollments
INSERT INTO enrollments (student_id, course_id, status) 
VALUES ((SELECT student_id FROM students s JOIN users u ON s.user_id = u.user_id WHERE u.username = 'agarcia'),
        (SELECT course_id FROM courses WHERE course_code = 'CS101'),
        'ACTIVE');

INSERT INTO enrollments (student_id, course_id, status) 
VALUES ((SELECT student_id FROM students s JOIN users u ON s.user_id = u.user_id WHERE u.username = 'agarcia'),
        (SELECT course_id FROM courses WHERE course_code = 'CS201'),
        'ACTIVE');

INSERT INTO enrollments (student_id, course_id, status) 
VALUES ((SELECT student_id FROM students s JOIN users u ON s.user_id = u.user_id WHERE u.username = 'bwilson'),
        (SELECT course_id FROM courses WHERE course_code = 'CS101'),
        'ACTIVE');

INSERT INTO enrollments (student_id, course_id, status) 
VALUES ((SELECT student_id FROM students s JOIN users u ON s.user_id = u.user_id WHERE u.username = 'clee'),
        (SELECT course_id FROM courses WHERE course_code = 'ENG101'),
        'ACTIVE');

-- Insert sample grades
INSERT INTO grades (enrollment_id, grade_value, grade_letter, comments, graded_by) 
VALUES ((SELECT e.enrollment_id FROM enrollments e 
         JOIN students s ON e.student_id = s.student_id 
         JOIN users u ON s.user_id = u.user_id 
         JOIN courses c ON e.course_id = c.course_id 
         WHERE u.username = 'agarcia' AND c.course_code = 'CS101'),
        92.5, 'A', 'Excellent work!',
        (SELECT teacher_id FROM teachers WHERE first_name = 'John' AND last_name = 'Smith'));

INSERT INTO grades (enrollment_id, grade_value, grade_letter, comments, graded_by) 
VALUES ((SELECT e.enrollment_id FROM enrollments e 
         JOIN students s ON e.student_id = s.student_id 
         JOIN users u ON s.user_id = u.user_id 
         JOIN courses c ON e.course_id = c.course_id 
         WHERE u.username = 'bwilson' AND c.course_code = 'CS101'),
        85.0, 'B', 'Good progress',
        (SELECT teacher_id FROM teachers WHERE first_name = 'John' AND last_name = 'Smith'));

COMMIT;

-- Display summary
SELECT 'Sample data loaded successfully!' AS status FROM dual;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_students FROM students;
SELECT COUNT(*) AS total_teachers FROM teachers;
SELECT COUNT(*) AS total_programs FROM programs;
SELECT COUNT(*) AS total_courses FROM courses;
SELECT COUNT(*) AS total_enrollments FROM enrollments;
SELECT COUNT(*) AS total_grades FROM grades;
