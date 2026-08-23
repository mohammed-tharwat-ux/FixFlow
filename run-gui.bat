@echo off
title FixFlow - Smart Maintenance & Incident Management System
echo ================================================================================
echo       FIXFLOW - SMART MAINTENANCE & INCIDENT MANAGEMENT DESKTOP APP
echo ================================================================================
echo Starting JavaFX Desktop GUI Application...
echo.

where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn javafx:run
) else (
    if exist "C:\Program Files\Apache\Maven\maven-mvnd-1.0.6-windows-amd64\mvn\bin\mvn.cmd" (
        "C:\Program Files\Apache\Maven\maven-mvnd-1.0.6-windows-amd64\mvn\bin\mvn.cmd" javafx:run
    ) else (
        java -cp "target/classes" com.fixflow.ui.gui.FixFlowDesktopApp
    )
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application terminated or encountered an error.
    pause
)
