@echo off
set JRE_HOME=
set OPAL_HOME=

if "%JRE_HOME%" == "" goto JRE_HOME_NOT_SET
if "%OPAL_HOME%" == "" goto OPAL_HOME_NOT_SET

java -Djava.ext.dirs="%JRE_HOME%\lib\ext;%OPAL_HOME%\lib" -cp "%OPAL_HOME%\conf" org.obiba.opal.cli.client.impl.OpalKeyClient %*
goto :END

:JRE_HOME_NOT_SET
echo JRE_HOME not set
exit

:OPAL_HOME_SET
echo OPAL_HOME not set
exit

:END