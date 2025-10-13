# Quick Start Guide

This guide will help you get the Academic Tracker Software up and running quickly.

## Prerequisites Check

Before starting, ensure you have:
- ✅ Java JDK 17 or higher installed
- ✅ Apache Maven 3.6+ installed
- ✅ Oracle Database 11g or higher installed and running
- ✅ MongoDB installed (optional, for feedback features)

Verify installations:
```bash
java -version      # Should show Java 17+
mvn -version       # Should show Maven 3.6+
```

## Quick Setup (5 Minutes)

### 1. Setup Database (2 minutes)

```bash
# Create database user and schema
sqlplus / as sysdba <<EOF
CREATE USER academictracker IDENTIFIED BY password123;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE TO academictracker;
exit;
EOF

# Load schema
sqlplus academictracker/password123@localhost:1521/xe @src/main/resources/sql/schema.sql

# Load sample data (optional)
sqlplus academictracker/password123@localhost:1521/xe @src/main/resources/sql/sample-data.sql
```

### 2. Configure Database Connection (30 seconds)

Edit `src/main/resources/config/application.properties` if needed:
```properties
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.username=academictracker
db.password=password123
```

### 3. Build Project (1 minute)

```bash
mvn clean compile
```

### 4. Run Application (30 seconds)

```bash
mvn javafx:run
```

### 5. Login (30 seconds)

Use the default admin credentials:
- **Username:** `admin`
- **Password:** `admin123`

## What to Try First

### As Admin:
1. Navigate to the "Students" tab
2. View the list of students (if sample data was loaded)
3. Try the "Programs" tab to see available programs
4. Check the "Courses" tab for course listings

### As Teacher (if sample data loaded):
1. Logout and login as `jsmith` / `teacher123`
2. View your assigned courses
3. See enrolled students

### As Student (if sample data loaded):
1. Logout and login as `agarcia` / `student123`
2. View your enrolled courses
3. Check your grades

## Common Issues

### "Database connection failed"
- Verify Oracle is running: `lsnrctl status`
- Check connection details in `application.properties`
- Ensure user `academictracker` exists

### "Schema script failed"
- Tables may already exist. Drop them first or use fresh database
- Check user has CREATE TABLE privilege

### "JavaFX runtime not found"
- Ensure you have JDK (not just JRE)
- Maven will download JavaFX dependencies automatically

### "Cannot compile - release version not supported"
- Update pom.xml to use Java 17 instead of 23
- Already configured in this version

## Project Structure Quick Reference

```
src/main/java/com/academictracker/
├── MainApp.java                    # Application entry point
├── model/                          # Data entities
├── dao/                            # Database access
├── service/                        # Business logic
├── controller/                     # UI controllers
└── util/                           # Utilities

src/main/resources/
├── fxml/                          # UI layouts
├── css/                           # Stylesheets
├── sql/                           # Database scripts
└── config/                        # Configuration
```

## Next Steps

1. **Explore the Admin Dashboard**: Navigate through different tabs
2. **Add New Records**: Try creating students, teachers, courses
3. **Test Enrollments**: Enroll students in courses
4. **Assign Grades**: Use the teacher dashboard to grade students
5. **Customize**: Modify the code to fit your needs

## Development Tips

### Run in Debug Mode
```bash
mvn javafx:run -X
```

### Rebuild After Changes
```bash
mvn clean compile
```

### Generate Password Hashes
```bash
mvn compile exec:java -Dexec.mainClass="com.academictracker.util.PasswordHashGenerator"
```

### View Logs
Logs are output to console. Check for errors or warnings.

## Documentation

- **README.md**: Complete documentation
- **DATABASE_SETUP.md**: Detailed database setup guide
- **Schema**: `src/main/resources/sql/schema.sql`

## Support

For issues or questions:
1. Check the README.md troubleshooting section
2. Review the database setup guide
3. Check application logs for error messages
4. Verify all prerequisites are met

## Features Overview

✅ User authentication with role-based access
✅ Student management (CRUD operations)
✅ Teacher management
✅ Program and course management
✅ Enrollment system with validation
✅ Grade management
✅ MongoDB feedback module (optional)
✅ Password security with BCrypt
✅ Professional JavaFX UI

Enjoy using Academic Tracker Software! 🎓
