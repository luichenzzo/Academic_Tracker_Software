# Academic Tracker Software
Proyecto Final de Bases de Datos II

## Overview
Academic Tracker Software is a comprehensive JavaFX-based University Academic Management System that uses Oracle Database for relational data and MongoDB for feedback management. The application follows the MVC (Model-View-Controller) pattern and implements role-based authentication for administrators, teachers, and students.

## Features

### Core Functionality
- **User Authentication & Authorization**: Secure login with role-based access control (Admin, Teacher, Student)
- **Student Management**: Complete CRUD operations for student records
- **Teacher Management**: Full management of teacher profiles and assignments
- **Program Management**: Define and manage academic programs
- **Course Management**: Create and manage courses with teacher assignments
- **Enrollment System**: Student enrollment with validation and capacity checking
- **Grade Management**: Grade assignment and tracking system
- **Feedback Module**: NoSQL-based feedback system using MongoDB

### Technical Features
- **MVC Architecture**: Clean separation of concerns
- **Data Validation**: Comprehensive input validation
- **Security**: Password hashing using BCrypt
- **Session Management**: Secure user session handling
- **Connection Pooling**: Efficient database connection management
- **Modular Structure**: Well-organized package structure

## Technology Stack

- **Java**: Version 17
- **JavaFX**: Version 21.0.1
- **Oracle Database**: JDBC with Oracle 23c driver
- **MongoDB**: Version 4.11.1 (for feedback module)
- **Maven**: Build and dependency management
- **BCrypt**: Password hashing
- **SLF4J**: Logging framework

## Prerequisites

- Java JDK 17 or higher
- Apache Maven 3.6+
- Oracle Database 11g or higher
- MongoDB 4.0 or higher (optional, for feedback module)

## Database Setup

### Oracle Database

1. Create a database user:
```sql
CREATE USER academictracker IDENTIFIED BY password123;
GRANT CONNECT, RESOURCE, DBA TO academictracker;
```

2. Run the schema creation script:
```bash
sqlplus academictracker/password123@localhost:1521/xe @src/main/resources/sql/schema.sql
```

The schema includes:
- Users table (authentication)
- Students table
- Teachers table
- Programs table
- Courses table
- Enrollments table
- Grades table

### MongoDB (Optional)

1. Start MongoDB service:
```bash
mongod --dbpath /path/to/data
```

2. The application will automatically create the `academic_feedback` database and `feedback` collection.

## Configuration

Update the database connection settings in `src/main/resources/config/application.properties`:

```properties
# Oracle Database
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.username=academictracker
db.password=password123
db.driver=oracle.jdbc.driver.OracleDriver

# MongoDB
mongodb.uri=mongodb://localhost:27017
mongodb.database=academic_feedback
```

## Building the Project

```bash
# Compile the project
mvn clean compile

# Package as JAR
mvn clean package

# Run tests (if available)
mvn test
```

## Running the Application

### Using Maven
```bash
mvn javafx:run
```

### Using Java directly
```bash
java -jar target/academic-tracker-software-1.0.0.jar
```

## Default Credentials

**Administrator Account:**
- Username: `admin`
- Password: `admin123`

Note: The default admin password hash needs to be properly generated. Use the PasswordUtil class to generate a valid BCrypt hash.

## Project Structure

```
academic-tracker-software/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── academictracker/
│   │   │           ├── MainApp.java              # Application entry point
│   │   │           ├── model/                    # Entity models
│   │   │           │   ├── User.java
│   │   │           │   ├── Student.java
│   │   │           │   ├── Teacher.java
│   │   │           │   ├── Program.java
│   │   │           │   ├── Course.java
│   │   │           │   ├── Enrollment.java
│   │   │           │   └── Grade.java
│   │   │           ├── dao/                      # Data Access Objects
│   │   │           │   ├── UserDAO.java
│   │   │           │   ├── StudentDAO.java
│   │   │           │   ├── TeacherDAO.java
│   │   │           │   ├── ProgramDAO.java
│   │   │           │   ├── CourseDAO.java
│   │   │           │   ├── EnrollmentDAO.java
│   │   │           │   └── GradeDAO.java
│   │   │           ├── service/                  # Business logic
│   │   │           │   ├── UserService.java
│   │   │           │   ├── AcademicService.java
│   │   │           │   └── FeedbackService.java
│   │   │           ├── controller/               # JavaFX controllers
│   │   │           │   ├── LoginController.java
│   │   │           │   ├── AdminDashboardController.java
│   │   │           │   ├── TeacherDashboardController.java
│   │   │           │   └── StudentDashboardController.java
│   │   │           └── util/                     # Utility classes
│   │   │               ├── DatabaseConnection.java
│   │   │               ├── PasswordUtil.java
│   │   │               ├── SessionManager.java
│   │   │               └── ValidationUtil.java
│   │   └── resources/
│   │       ├── fxml/                            # JavaFX views
│   │       │   ├── login.fxml
│   │       │   ├── admin-dashboard.fxml
│   │       │   ├── teacher-dashboard.fxml
│   │       │   └── student-dashboard.fxml
│   │       ├── css/                             # Stylesheets
│   │       │   └── style.css
│   │       ├── sql/                             # Database scripts
│   │       │   └── schema.sql
│   │       └── config/                          # Configuration files
│   │           └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── academictracker/
└── pom.xml
```

## User Roles & Permissions

### Administrator
- Manage all users (students, teachers, admins)
- Manage programs and courses
- View all enrollments and grades
- Full system access

### Teacher
- View assigned courses
- Manage student enrollments in their courses
- Assign and update grades
- View student information

### Student
- View enrolled courses
- View personal grades
- Enroll in available courses
- Submit feedback

## Data Validation

The system implements comprehensive validation:
- Username: 3-50 alphanumeric characters or underscore
- Password: Minimum 6 characters
- Email: Valid email format
- Phone: 10-digit or formatted (XXX-XXX-XXXX)
- Course Code: Format like CS101, MATH205
- Program Code: 2-6 uppercase letters
- Grade: 0-100 range

## Security Features

1. **Password Security**: BCrypt hashing with salt
2. **Session Management**: User session tracking
3. **Role-Based Access Control**: Different permissions per role
4. **Input Validation**: Prevent SQL injection and invalid data
5. **Database Constraints**: Foreign keys and data integrity

## Future Enhancements

- Advanced reporting and analytics
- Email notifications
- Document upload and management
- Calendar integration for class schedules
- Mobile application
- Real-time chat and collaboration
- Attendance tracking
- Financial management (tuition, payments)

## Troubleshooting

### Database Connection Issues
- Verify Oracle Database is running
- Check connection string in application.properties
- Ensure user has proper privileges

### JavaFX Runtime Issues
- Ensure Java JDK 23 is properly installed
- Verify JavaFX libraries are in the classpath

### Build Errors
- Clean Maven cache: `mvn clean`
- Update dependencies: `mvn dependency:resolve`

## Contributing

This is an academic project. For contributions:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is created for educational purposes as part of Database II course.

## Authors

- Luis Chen - Initial work

## Acknowledgments

- Database II Course instructors
- JavaFX community
- Oracle and MongoDB documentation

