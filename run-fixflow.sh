#!/bin/bash
# ==============================================================================
# FixFlow — Direct Executable Shell Script
# Works on macOS, Linux, and iOS terminal environments (e.g. iSH / a-Shell)
# ==============================================================================

echo "================================================================================"
echo "      FIXFLOW — SMART MAINTENANCE & INCIDENT MANAGEMENT SYSTEM"
echo "================================================================================"
echo "Starting FixFlow..."
echo ""

# Compile if target directory does not exist
if [ ! -d "target/classes" ]; then
    if command -v mvn &> /dev/null; then
        mvn compile -q
    else
        mkdir -p target/classes
        javac -d target/classes $(find src/main/java -name "*.java")
    fi
fi

# Run application
java -cp "target/classes" com.fixflow.ui.FixFlowApp "$@"
