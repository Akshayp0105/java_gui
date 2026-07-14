@echo off
echo Compiling Attendance Calculator Pro...
javac -encoding UTF-8 AttendanceCalculator.java
if %errorlevel% equ 0 (
    echo Compilation successful!
    echo Run with: java AttendanceCalculator
) else (
    echo Compilation failed!
    pause
)
