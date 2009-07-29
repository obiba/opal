@echo off
echo JAVA_HOME=%JAVA_HOME%
echo OPAL_HOME=%OPAL_HOME%

if "%JAVA_HOME%" == "" goto JAVA_HOME_NOT_SET
if "%OPAL_HOME%" == "" goto OPAL_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

call "%OPAL_HOME%\bin\setclasspath.bat"

java -cp "%CLASSPATH%" org.obiba.opal.cli.client.impl.OpalKeyClient %*
goto :END

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:OPAL_HOME_NOT_SET
echo OPAL_HOME not set
goto :END

:END