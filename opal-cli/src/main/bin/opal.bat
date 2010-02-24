@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo OPAL_HOME=%OPAL_HOME%

if "%JAVA_HOME%" == "" goto JAVA_HOME_NOT_SET
if "%OPAL_HOME%" == "" goto OPAL_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

call "%OPAL_HOME%\bin\setclasspath.bat"

IF NOT EXIST %OPAL_HOME%\logs mkdir %OPAL_HOME%\logs
java %JAVA_OPTS% -cp "%CLASSPATH%" org.obiba.opal.cli.client.OpalConsole %* 2> %OPAL_HOME%\logs\opal.log
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xmx512M
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:OPAL_HOME_NOT_SET
echo OPAL_HOME not set
goto :END

:END