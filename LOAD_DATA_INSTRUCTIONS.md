infr# How to Load Sample Data into Your Database

## Problem
The program combobox appears empty when adding a student because the `ProgramaAcademico` table has no data.

## Solution
Run the updated `sample-data.sql` file to populate all required tables.

## Instructions

### Option 1: Using SQL Developer or SQL*Plus

1. Connect to your Oracle database
2. Navigate to: `src/main/resources/sql/`
3. First, run `schema.sql` (if you haven't already)
4. Then run `sample-data.sql`

### Option 2: Using SQL*Plus Command Line

```cmd
sqlplus username/password@database
@C:\Users\Luich\Desktop\Universidad 2025-2\Data Bases 2\Proyecto Final\Academic_Tracker_Software\src\main\resources\sql\sample-data.sql
```

### Option 3: Copy and paste into your SQL tool

Open the file `src/main/resources/sql/sample-data.sql` and execute all the INSERT statements.

## What This Will Add

- 3 Sedes (Campuses)
- 5 Facultades (Faculties)
- 5 TipoPrograma (Program Types)
- 11 ProgramaAcademico (Academic Programs)
- 4 NivelRiesgo (Risk Levels)
- 5 TipoDocumento (Document Types)
- 4 TipoAsignatura (Subject Types)
- Supporting data for institutions, formation levels, and days of the week

## After Loading Data

Restart your application and try adding a student again. The program combobox should now display options like:
- ING-SIS - Ingeniería de Sistemas y Computación
- ING-CIV - Ingeniería Civil
- MED - Medicina
- etc.

