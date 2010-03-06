rem Include Opal's conf directory in the classpath.
set CLASSPATH=%OPAL_HOME%\conf

rem Add all JARs in Opal's lib directory to the classpath. 
for /f %%f in ('dir /b "%OPAL_HOME%\lib\*.jar"') do set CLASSPATH=!CLASSPATH!;%OPAL_HOME%\lib\%%f