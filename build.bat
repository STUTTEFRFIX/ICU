@echo off
REM ============================================================
REM  ICU - 一键构建脚本
REM
REM  在有沙箱限制的本机上，直接 gradlew.bat build 会因为下面
REM  三个原因失败，所以这里统一设置好：
REM
REM   1. JAVA_HOME 必须是 JDK 21。本机系统只装了 JDK 25，
REM      而 Gradle 8.14.3 的 Groovy 无法解析 Java 25 的 class 文件。
REM   2. GRADLE_USER_HOME 必须指向工作区内，因为文件沙箱拒绝
REM      写入 C:\Users\...\.gradle。
REM   3. TEMP/TMP 必须指向工作区内，因为 NeoForm 的 jst 工具
REM      会在临时目录建目录，而它被拒绝访问系统 TEMP。
REM
REM  在没有这些限制的普通机器上，直接运行 gradlew.bat build 即可。
REM ============================================================

setlocal
cd /d "%~dp0"

set "JAVA_HOME=D:\DS\tools\jdk21"
set "GRADLE_USER_HOME=D:\DS\.gradle-home"
set "TEMP=D:\DS\.tmp-build"
set "TMP=D:\DS\.tmp-build"

echo [ICU] JAVA_HOME        = %JAVA_HOME%
echo [ICU] GRADLE_USER_HOME = %GRADLE_USER_HOME%
echo [ICU] TEMP             = %TEMP%
echo.

call gradlew.bat build --no-daemon --console=plain %*
set "RC=%ERRORLEVEL%"

echo.
if "%RC%"=="0" (
  echo [ICU] 构建成功 - 产物:
  dir /b "build\libs\*.jar"
) else (
  echo [ICU] 构建失败，退出码 %RC%
)
endlocal & exit /b %RC%
