@echo off
set JRE_HOME=C:\Program Files\Java\jdk1.6.0_07\jre
set OPALKEY_HOME=..

if "%JRE_HOME%" == "" goto JRE_HOME_NOT_SET
if "%OPALKEY_HOME%" == "" goto OPALKEY_HOME_NOT_SET

java -Djava.ext.dirs="%JRE_HOME%\lib\ext;%OPALKEY_HOME%\lib" -cp "%OPALKEY_HOME%\conf" org.obiba.opal.cli.client.impl.OpalKeyClient %*
goto :END

:JRE_HOME_NOT_SET
echo JRE_HOME not set
exit

:OPALKEY_HOME_SET
echo OPALKEY_HOME not set
exit

:END