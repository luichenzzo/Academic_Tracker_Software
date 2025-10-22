@echo off
echo === DATABASE DIAGNOSTIC TEST ===
echo.
echo Compiling project...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running database test...
call mvn exec:java -Dexec.mainClass="com.academictracker.util.DatabaseTest"

echo.
echo Test complete!
pause

