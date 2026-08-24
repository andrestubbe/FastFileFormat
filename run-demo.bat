@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building Main Project (FastFileFormat)...
call mvn install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Main build failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running FastFileFormat Interactive Demo...
java -cp "target\FastFileFormat-0.1.0.jar;examples\Demo\src\main\java;%USERPROFILE%\.m2\repository\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar" fastfileformat.demo.Demo

pause
