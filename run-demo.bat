@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⚡ Building Main Project (FastFileFormat)...
call mvn install -DskipTests -q
if %ERRORLEVEL% NEQ 0 ( echo ❌ Main build failed. & pause & exit /b %ERRORLEVEL% )

echo 🔨 Compiling Demo...
if not exist examples\Demo\target\classes mkdir examples\Demo\target\classes
set CP=target\FastFileFormat-0.1.1.jar
set M2=%USERPROFILE%\.m2\repository\com\github\andrestubbe
set CP=%CP%;%M2%\FastANSI\0.1.3\FastANSI-0.1.3.jar
set CP=%CP%;%M2%\FastBinary\0.1.1\FastBinary-0.1.1.jar
set CP=%CP%;%M2%\fastcore\0.1.0\fastcore-0.1.0.jar
javac -encoding UTF-8 -cp "%CP%" -d "examples\Demo\target\classes" "examples\Demo\src\main\java\fastfileformat\demo\Demo.java"
if %ERRORLEVEL% NEQ 0 ( echo ❌ Demo compile failed. & pause & exit /b %ERRORLEVEL% )

echo 🚀 Running FastFileFormat Interactive Demo...
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -cp "%CP%;examples\Demo\target\classes" fastfileformat.demo.Demo

pause