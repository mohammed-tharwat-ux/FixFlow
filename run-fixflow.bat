@echo off
title FixFlow Interactive System
echo ================================================================================
echo       FIXFLOW - SMART MAINTENANCE & INCIDENT MANAGEMENT SYSTEM
echo ================================================================================
echo Starting FixFlow Application...
echo.

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn compile -q
) else (
    if exist "C:\Program Files\Apache\Maven\maven-mvnd-1.0.6-windows-amd64\mvn\bin\mvn.cmd" (
        "C:\Program Files\Apache\Maven\maven-mvnd-1.0.6-windows-amd64\mvn\bin\mvn.cmd" compile -q
    )
)

java -cp "target/classes" com.fixflow.ui.FixFlowApp
pause
