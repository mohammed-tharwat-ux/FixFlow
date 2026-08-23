@echo off
title FixFlow Automated Demonstration
echo ================================================================================
echo       FIXFLOW - AUTOMATED LIVE DEMONSTRATION SCENARIO
echo ================================================================================
echo Running automated 15-step scenario...
echo.

java -cp "target/classes" com.fixflow.ui.FixFlowApp --demo
pause
