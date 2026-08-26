@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building Main Project (FastFileFormat)...
call mvn install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Main build failed. & pause & exit /b %ERRORLEVEL% )

echo 🔨 Compiling Demo...
if not exist examples\Demo\target\classes mkdir examples\Demo\target\classes
javac -cp "target\FastFileFormat-0.1.1.jar" -d "examples\Demo\target\classes" "examples\Demo\src\main\java\fastfileformat\demo\Demo.java"
if %ERRORLEVEL% NEQ 0 ( echo ❌ Demo compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running FastFileFormat Interactive Demo...
java -cp "target\FastFileFormat-0.1.1.jar;examples\Demo\target\classes" fastfileformat.demo.Demo

pause