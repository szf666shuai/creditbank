@REM Maven Wrapper startup script for Windows
@echo off
setlocal

if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    call "%MAVEN_HOME%\bin\mvn.cmd" %*
    exit /B %ERRORLEVEL%
)

set "MAVEN_PROJECTBASEDIR=%~dp0."
set "WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if not exist "%WRAPPER_JAR%" (
    echo Error: Maven wrapper JAR not found at "%WRAPPER_JAR%"
    exit /B 1
)

java -Xmx512m -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
set "ERROR_CODE=%ERRORLEVEL%"
exit /B %ERROR_CODE%