@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  microraiden-java startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and MICRORAIDEN_JAVA_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\microraiden-java.jar;%APP_HOME%\lib\httpclient-4.3.4.jar;%APP_HOME%\lib\ethereumj-core-1.4.5-RELEASE.jar;%APP_HOME%\lib\leveldb-0.7.jar;%APP_HOME%\lib\guava-23.0.jar;%APP_HOME%\lib\commons-io-2.5.jar;%APP_HOME%\lib\json-simple-1.1.1.jar;%APP_HOME%\lib\pitaya-0.4.jar;%APP_HOME%\lib\httpcore-4.3.2.jar;%APP_HOME%\lib\java-util-1.8.0.jar;%APP_HOME%\lib\spring-context-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-orm-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-jdbc-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-tx-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-aop-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-beans-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-expression-4.2.0.RELEASE.jar;%APP_HOME%\lib\spring-core-4.2.0.RELEASE.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-codec-1.10.jar;%APP_HOME%\lib\jsr305-3.0.0.jar;%APP_HOME%\lib\error_prone_annotations-2.0.18.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.14.jar;%APP_HOME%\lib\logback-classic-1.1.7.jar;%APP_HOME%\lib\jackson-databind-2.5.1.jar;%APP_HOME%\lib\prov-1.53.0.0.jar;%APP_HOME%\lib\core-1.53.0.0.jar;%APP_HOME%\lib\config-1.2.1.jar;%APP_HOME%\lib\netty-all-4.0.30.Final.jar;%APP_HOME%\lib\commons-collections4-4.0.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\jackson-mapper-asl-1.9.13.jar;%APP_HOME%\lib\leveldbjni-all-1.18.3.jar;%APP_HOME%\lib\solcJ-all-0.4.8.jar;%APP_HOME%\lib\javassist-3.15.0-GA.jar;%APP_HOME%\lib\mapdb-2.0-beta12.jar;%APP_HOME%\lib\slf4j-api-1.7.20.jar;%APP_HOME%\lib\logback-core-1.1.7.jar;%APP_HOME%\lib\json-io-2.4.1.jar;%APP_HOME%\lib\jackson-annotations-2.5.0.jar;%APP_HOME%\lib\jackson-core-2.5.1.jar;%APP_HOME%\lib\jackson-core-asl-1.9.13.jar;%APP_HOME%\lib\leveldb-api-0.7.jar;%APP_HOME%\lib\aopalliance-1.0.jar;%APP_HOME%\lib\junit-4.10.jar;%APP_HOME%\lib\hamcrest-core-1.1.jar

@rem Execute microraiden-java
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %MICRORAIDEN_JAVA_OPTS%  -classpath "%CLASSPATH%" MicroRaiden %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable MICRORAIDEN_JAVA_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%MICRORAIDEN_JAVA_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
