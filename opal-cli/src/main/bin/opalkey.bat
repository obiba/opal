@echo off
set JAVA_HOME=
set OPAL_HOME=

if "%JAVA_HOME%" == "" goto JAVA_HOME_NOT_SET
if "%OPAL_HOME%" == "" goto OPAL_HOME_NOT_SET

java -Djava.ext.dirs="%JAVA_HOME%\jre\lib\ext;%OPAL_HOME%\lib" -cp "%OPAL_HOME%\conf" org.obiba.opal.cli.client.impl.OpalKeyClient %*
goto :END

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
exit

:OPAL_HOME_NOT_SET
echo OPAL_HOME not set
exit

:END