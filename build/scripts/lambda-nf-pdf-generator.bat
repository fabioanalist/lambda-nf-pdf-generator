@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  lambda-nf-pdf-generator startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and LAMBDA_NF_PDF_GENERATOR_OPTS to pass JVM options to this script.
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

set CLASSPATH=%APP_HOME%\lib\lambda-nf-pdf-generator.jar;%APP_HOME%\lib\aws-lambda-java-core-1.2.0.jar;%APP_HOME%\lib\aws-java-sdk-s3-1.11.603.jar;%APP_HOME%\lib\jasperreports-6.8.0.jar;%APP_HOME%\lib\commons-io-2.6.jar;%APP_HOME%\lib\itext-2.1.7.jar;%APP_HOME%\lib\xalan-2.7.2.jar;%APP_HOME%\lib\barbecue-1.5-beta1.jar;%APP_HOME%\lib\hibernate-core-5.2.10.Final.jar;%APP_HOME%\lib\postgresql-42.1.4.jre7.jar;%APP_HOME%\lib\aws-java-sdk-kms-1.11.603.jar;%APP_HOME%\lib\aws-java-sdk-core-1.11.603.jar;%APP_HOME%\lib\jmespath-java-1.11.603.jar;%APP_HOME%\lib\commons-digester-2.1.jar;%APP_HOME%\lib\commons-beanutils-1.9.3.jar;%APP_HOME%\lib\castor-xml-1.4.1.jar;%APP_HOME%\lib\httpclient-4.5.9.jar;%APP_HOME%\lib\castor-core-1.4.1.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\commons-collections4-4.2.jar;%APP_HOME%\lib\jfreechart-1.0.19.jar;%APP_HOME%\lib\jcommon-1.0.23.jar;%APP_HOME%\lib\ecj-4.4.2.jar;%APP_HOME%\lib\jackson-databind-2.9.8.jar;%APP_HOME%\lib\jackson-dataformat-cbor-2.6.7.jar;%APP_HOME%\lib\jackson-core-2.9.8.jar;%APP_HOME%\lib\jackson-annotations-2.9.8.jar;%APP_HOME%\lib\hibernate-commons-annotations-5.0.1.Final.jar;%APP_HOME%\lib\jboss-logging-3.3.0.Final.jar;%APP_HOME%\lib\hibernate-jpa-2.1-api-1.0.0.Final.jar;%APP_HOME%\lib\javassist-3.20.0-GA.jar;%APP_HOME%\lib\antlr-2.7.7.jar;%APP_HOME%\lib\jboss-transaction-api_1.2_spec-1.0.1.Final.jar;%APP_HOME%\lib\jandex-2.0.3.Final.jar;%APP_HOME%\lib\classmate-1.3.0.jar;%APP_HOME%\lib\dom4j-1.6.1.jar;%APP_HOME%\lib\ion-java-1.0.2.jar;%APP_HOME%\lib\joda-time-2.8.1.jar;%APP_HOME%\lib\commons-collections-3.2.2.jar;%APP_HOME%\lib\commons-lang3-3.4.jar;%APP_HOME%\lib\javax.inject-1.jar;%APP_HOME%\lib\httpcore-4.4.11.jar;%APP_HOME%\lib\commons-codec-1.11.jar

@rem Execute lambda-nf-pdf-generator
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %LAMBDA_NF_PDF_GENERATOR_OPTS%  -classpath "%CLASSPATH%" br.com.actaholding.erpmiddleware.nfpdfgenerator.LambdaHandler %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable LAMBDA_NF_PDF_GENERATOR_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%LAMBDA_NF_PDF_GENERATOR_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
