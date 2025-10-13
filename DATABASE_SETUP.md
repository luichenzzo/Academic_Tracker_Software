# Database Setup Guide

## Oracle Database Setup

### Step 1: Create Database User

Connect to Oracle as SYSDBA and run:

```sql
-- Connect as SYSDBA
sqlplus / as sysdba

-- Create user
CREATE USER academictracker IDENTIFIED BY password123;

-- Grant privileges
GRANT CONNECT, RESOURCE TO academictracker;
GRANT CREATE SESSION TO academictracker;
GRANT CREATE TABLE TO academictracker;
GRANT CREATE VIEW TO academictracker;
GRANT CREATE SEQUENCE TO academictracker;
GRANT CREATE TRIGGER TO academictracker;
GRANT UNLIMITED TABLESPACE TO academictracker;

-- Exit
exit;
```

### Step 2: Create Database Schema

Connect as the academictracker user and run the schema script:

```bash
sqlplus academictracker/password123@localhost:1521/xe @src/main/resources/sql/schema.sql
```

Or from SQL*Plus:

```sql
-- Connect as academictracker
sqlplus academictracker/password123@localhost:1521/xe

-- Run schema
@src/main/resources/sql/schema.sql
```

### Step 3: Load Sample Data (Optional)

To populate the database with test data:

```bash
sqlplus academictracker/password123@localhost:1521/xe @src/main/resources/sql/sample-data.sql
```

## MongoDB Setup (Optional - for Feedback Module)

### Step 1: Install MongoDB

Download and install MongoDB from: https://www.mongodb.com/try/download/community

### Step 2: Start MongoDB

```bash
# On Linux/Mac
mongod --dbpath /data/db

# On Windows
mongod.exe --dbpath C:\data\db
```

### Step 3: Configuration

The application will automatically create the database and collections when needed.
Default connection: `mongodb://localhost:27017`
Database name: `academic_feedback`

## Verify Database Setup

After running the schema and sample data scripts, verify the setup:

```sql
-- Check tables were created
SELECT table_name FROM user_tables ORDER BY table_name;

-- Check user count
SELECT COUNT(*) FROM users;

-- Check default admin user
SELECT username, email, role FROM users WHERE role = 'ADMIN';
```

Expected tables:
- USERS
- STUDENTS
- TEACHERS
- PROGRAMS
- COURSES
- ENROLLMENTS
- GRADES

## Default Credentials

After running the scripts, you can login with:

**Admin:**
- Username: `admin`
- Password: `admin123`

**Teacher (if sample data loaded):**
- Username: `jsmith` or `mjones`
- Password: `teacher123`

**Student (if sample data loaded):**
- Username: `agarcia`, `bwilson`, or `clee`
- Password: `student123`

## Troubleshooting

### Connection Failed
1. Verify Oracle listener is running: `lsnrctl status`
2. Check if database is running: `sqlplus / as sysdba` then `SELECT status FROM v$instance;`
3. Verify connection string in `application.properties`

### Table Creation Errors
1. Ensure user has CREATE TABLE privilege
2. Check if user has sufficient tablespace quota
3. Drop existing tables if needed (script includes DROP statements)

### Cannot Login
1. Verify user exists: `SELECT * FROM users WHERE username = 'admin';`
2. Check password hash is correct
3. Verify user is active: `active = 1`

## Database Connection Configuration

Update `src/main/resources/config/application.properties`:

```properties
# Oracle Database
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.username=academictracker
db.password=password123
db.driver=oracle.jdbc.driver.OracleDriver

# For remote database:
# db.url=jdbc:oracle:thin:@hostname:1521:servicename
```

## Security Notes

⚠️ **Important:** Change default passwords in production!

1. Change database user password
2. Update application.properties
3. Change default admin password after first login
4. Use strong passwords for all accounts
