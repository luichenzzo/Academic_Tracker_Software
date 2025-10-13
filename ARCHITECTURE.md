# Academic Tracker Software - Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                         │
│                        (JavaFX UI)                              │
├─────────────────────────────────────────────────────────────────┤
│  LoginView  │  AdminDashboard  │ TeacherDashboard │ StudentDash │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Controller Layer                           │
│                    (UI Controllers)                             │
├─────────────────────────────────────────────────────────────────┤
│ LoginController │ AdminDashboardController │ etc.              │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Service Layer                             │
│                   (Business Logic)                              │
├─────────────────────────────────────────────────────────────────┤
│  UserService  │  AcademicService  │  FeedbackService           │
│  - Authentication   - CRUD Operations    - NoSQL Integration   │
│  - Authorization    - Validation         - Feedback Management │
│  - Session Mgmt     - Business Rules                           │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DAO Layer                               │
│                    (Data Access)                                │
├─────────────────────────────────────────────────────────────────┤
│ UserDAO │ StudentDAO │ TeacherDAO │ ProgramDAO │ CourseDAO     │
│ EnrollmentDAO │ GradeDAO                                        │
└──────────┬──────────────────────────────┬────────────────────────┘
           │                              │
           ▼                              ▼
┌──────────────────────────┐   ┌────────────────────────────────┐
│    Oracle Database       │   │      MongoDB Database          │
│   (Relational Data)      │   │   (Feedback & NoSQL Data)      │
├──────────────────────────┤   ├────────────────────────────────┤
│ • Users                  │   │ • feedback collection          │
│ • Students               │   │   - userId                     │
│ • Teachers               │   │   - feedbackType               │
│ • Programs               │   │   - message                    │
│ • Courses                │   │   - timestamp                  │
│ • Enrollments            │   │   - status                     │
│ • Grades                 │   │                                │
└──────────────────────────┘   └────────────────────────────────┘
```

## Layer Responsibilities

### 1. Presentation Layer (JavaFX FXML)
- **Purpose**: User interface and user interaction
- **Components**:
  - `login.fxml`: Login screen
  - `admin-dashboard.fxml`: Administrator interface
  - `teacher-dashboard.fxml`: Teacher interface
  - `student-dashboard.fxml`: Student interface
  - `style.css`: Application styling
- **Responsibilities**:
  - Display data to users
  - Capture user input
  - Navigation between views
  - UI validation and feedback

### 2. Controller Layer (JavaFX Controllers)
- **Purpose**: Handle UI events and coordinate between view and service
- **Components**:
  - `LoginController`: Authentication logic
  - `AdminDashboardController`: Admin operations
  - `TeacherDashboardController`: Teacher operations
  - `StudentDashboardController`: Student operations
- **Responsibilities**:
  - Handle button clicks and user actions
  - Call service layer methods
  - Update UI based on service responses
  - Manage view state

### 3. Service Layer (Business Logic)
- **Purpose**: Implement business rules and coordinate operations
- **Components**:
  - `UserService`: User management and authentication
  - `AcademicService`: Academic operations (students, teachers, courses, etc.)
  - `FeedbackService`: Feedback management with MongoDB
- **Responsibilities**:
  - Implement business logic
  - Validate data
  - Coordinate multiple DAO operations
  - Transaction management
  - Security enforcement

### 4. DAO Layer (Data Access Objects)
- **Purpose**: Database operations and data persistence
- **Components**:
  - `UserDAO`: User CRUD operations
  - `StudentDAO`: Student CRUD operations
  - `TeacherDAO`: Teacher CRUD operations
  - `ProgramDAO`: Program CRUD operations
  - `CourseDAO`: Course CRUD operations
  - `EnrollmentDAO`: Enrollment CRUD operations
  - `GradeDAO`: Grade CRUD operations
- **Responsibilities**:
  - Execute SQL queries
  - Map database records to Java objects
  - Handle database connections
  - Implement CRUD operations

### 5. Model Layer (Entities)
- **Purpose**: Represent business entities
- **Components**:
  - `User`: User account information
  - `Student`: Student profile
  - `Teacher`: Teacher profile
  - `Program`: Academic program
  - `Course`: Course information
  - `Enrollment`: Student-course enrollment
  - `Grade`: Student grades
- **Responsibilities**:
  - Define data structure
  - Encapsulate entity logic
  - Provide getters/setters

### 6. Utility Layer
- **Purpose**: Provide common functionality
- **Components**:
  - `DatabaseConnection`: Database connection management
  - `PasswordUtil`: Password hashing and verification
  - `SessionManager`: User session management
  - `ValidationUtil`: Input validation
  - `PasswordHashGenerator`: Generate password hashes
- **Responsibilities**:
  - Database connection pooling
  - Password security
  - Session tracking
  - Data validation
  - Common utilities

## Data Flow Example: Student Login

```
1. User enters credentials in login.fxml
   ↓
2. LoginController.handleLogin() called
   ↓
3. UserService.login(username, password)
   ↓
4. UserDAO.findByUsername(username)
   ↓
5. Oracle Database query
   ↓
6. User object returned
   ↓
7. PasswordUtil.verifyPassword(password, hash)
   ↓
8. SessionManager.setCurrentUser(user)
   ↓
9. Controller loads appropriate dashboard (student-dashboard.fxml)
   ↓
10. StudentDashboardController initializes view with user data
```

## Security Architecture

### Authentication Flow
```
Login Request
    ↓
Validate Input (ValidationUtil)
    ↓
Find User (UserDAO)
    ↓
Verify Password (PasswordUtil with BCrypt)
    ↓
Create Session (SessionManager)
    ↓
Redirect to Role-Based Dashboard
```

### Password Security
- Passwords hashed using BCrypt with salt
- Hash strength: 10 rounds
- Passwords never stored in plain text
- Session-based authentication

### Role-Based Access Control
```
User Login
    ↓
Role Check (SessionManager)
    ↓
┌─────────┬──────────┬──────────┐
│  ADMIN  │ TEACHER  │ STUDENT  │
└─────────┴──────────┴──────────┘
    ↓           ↓          ↓
Full Access  Limited    Read-Only
            Access     + Enroll
```

## Database Schema

### Entity Relationships
```
Users (1) ─────→ (1) Students
Users (1) ─────→ (1) Teachers
Programs (1) ───→ (Many) Courses
Teachers (1) ───→ (Many) Courses
Students (Many) ←→ (Many) Courses via Enrollments
Enrollments (1) ─→ (1) Grades
Teachers (1) ───→ (Many) Grades
```

### Key Constraints
- Foreign keys ensure referential integrity
- Unique constraints on usernames and emails
- Check constraints for valid data ranges
- Cascade deletes for dependent records

## Technology Stack Diagram

```
┌─────────────────────────────────────────────┐
│         Application Layer (JavaFX)          │
├─────────────────────────────────────────────┤
│              Java 17                        │
│              Maven Build                     │
├─────────────────────────────────────────────┤
│         JavaFX 21.0.1 (UI Framework)        │
├─────────────────────────────────────────────┤
│   JDBC Driver          MongoDB Driver       │
│   Oracle 23c           MongoDB 4.11         │
├─────────────────────────────────────────────┤
│   BCrypt              SLF4J (Logging)       │
│   (Security)                                │
├─────────────────────────────────────────────┤
│   Oracle DB           MongoDB               │
│   (Primary Data)      (Feedback Data)       │
└─────────────────────────────────────────────┘
```

## Design Patterns Used

### 1. MVC (Model-View-Controller)
- **Model**: Entity classes (User, Student, etc.)
- **View**: FXML files and CSS
- **Controller**: Controller classes

### 2. DAO (Data Access Object)
- Separates data access logic from business logic
- Each entity has its own DAO

### 3. Singleton
- `SessionManager`: Single instance for session management
- `DatabaseConnection`: Connection management

### 4. Service Layer Pattern
- Business logic separated from controllers
- Reusable across different UI components

### 5. Factory Pattern (Implicit)
- DAO objects created as needed
- Service objects instantiated in controllers

## Deployment Architecture

```
┌────────────────────────────────────────────┐
│          Client Machine                    │
│                                            │
│  ┌──────────────────────────────────┐     │
│  │   Academic Tracker App (JAR)    │     │
│  │   - JavaFX UI                    │     │
│  │   - Business Logic               │     │
│  │   - JDBC Connection              │     │
│  └──────────┬───────────────────────┘     │
│             │                              │
└─────────────┼──────────────────────────────┘
              │
              │ JDBC (1521) / MongoDB (27017)
              │
┌─────────────┴──────────────────────────────┐
│          Database Server(s)                │
│                                            │
│  ┌──────────────┐    ┌─────────────────┐  │
│  │  Oracle DB   │    │    MongoDB      │  │
│  │  Port: 1521  │    │   Port: 27017   │  │
│  └──────────────┘    └─────────────────┘  │
└────────────────────────────────────────────┘
```

## Scalability Considerations

### Current Implementation
- Single-user desktop application
- Direct database connections
- Local session management

### Future Enhancements
- Web-based interface (JavaFX → Spring Boot + REST API)
- Connection pooling (HikariCP)
- Distributed caching (Redis)
- Microservices architecture
- Load balancing
- Containerization (Docker)

## Performance Optimizations

1. **Database Indexing**: Indexes on frequently queried columns
2. **Prepared Statements**: Prevent SQL injection and improve performance
3. **Connection Reuse**: Efficient connection management
4. **Lazy Loading**: Load data only when needed
5. **Caching**: Session-based user caching

## Monitoring and Logging

- SLF4J for logging
- Log levels: INFO, WARN, ERROR
- Database operation logging
- Authentication event logging
- Error tracking in DAO and Service layers
