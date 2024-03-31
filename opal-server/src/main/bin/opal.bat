@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo OPAL_HOME=%OPAL_HOME%

if "%OPAL_HOME%" == "" goto OPAL_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set OPAL_DIST=%~dp0..
echo OPAL_DIST=%OPAL_DIST%

set OPAL_LOG=%OPAL_HOME%\logs
IF NOT EXIST "%OPAL_LOG%" mkdir "%OPAL_LOG%"
echo OPAL_LOG=%OPAL_LOG%

rem Java 7 supports wildcard classpaths
rem http://docs.oracle.com/javase/7/docs/technotes/tools/windows/classpath.html
set CLASSPATH=%OPAL_HOME%\conf;%OPAL_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
java %JAVA_OPTS% -cp "%CLASSPATH%" -DOPAL_HOME="%OPAL_HOME%" -DOPAL_DIST=%OPAL_DIST% -DOPAL_LOG=%OPAL_LOG% -Dpolyglot.log.file=%OPAL_LOG%/polyglot.log -Dpolyglot.engine.WarnInterpreterOnly=false org.obiba.opal.server.OpalServer %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xms1G -Xmx2G -XX:+UseG1GC
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:OPAL_HOME_NOT_SET
echo OPAL_HOME not set
goto :END

:END
