@echo off
REM ============================================================
REM  ICU mod - reproducible build script
REM
REM  This machine's environment needs three things that are not
REM  provided by a plain "gradlew build":
REM
REM   1. JAVA_HOME must be JDK 21. Only JDK 25 is installed system
REM      wide, and Gradle 8.14.3 cannot parse Java 25 class files.
REM   2. GRADLE_USER_HOME must point inside D:\DS, because the file
REM      sandbox refuses writes to C:\Users\...\.gradle.
REM   3. TEMP/TMP must point inside D:\DS, because NeoForm's jst tool
REM      creates a temp directory and is denied access to the
REM      system TEMP.
REM
REM  On a normal machine without these restrictions you can simply
REM  run: gradlew.bat build
REM ============================================================

setlocal
cd /d "%~dp0"

set "JAVA_HOME=D:\DS\tools\jdk21"
set "GRADLE_USER_HOME=D:\DS\.gradle-home"
set "TEMP=D:\DS\.tmp-build"
set "TMP=D:\DS\.tmp-build"

echo [ICU] JAVA_HOME       = %JAVA_HOME%
echo [ICU] GRADLE_USER_HOME= %GRADLE_USER_HOME%
echo [ICU] TEMP            = %TEMP%
echo.

call gradlew.bat build --no-daemon --console=plain %*
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
  echo [ICU] BUILD SUCCESSFUL - artifact:
  dir /b "build\libs\*.jar"
) else (
  echo [ICU] BUILD FAILED with exit code %RC%
)
endlocal & exit /b %RC%
