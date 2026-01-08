@ECHO OFF

SET CWD=%cd%
SET SCRIPT_DIR=%~dp0

SET COMMAND=%1
shift
set params=%1
:loop
shift
if [%1]==[] goto afterloop
set params=%params% %1
goto loop
:afterloop

IF "%COMMAND%"=="pack" (
    cd %SCRIPT_DIR%\modpack || exit /b 1
    .\pakkuw %params%
) ELSE IF "%COMMAND%"=="env" (
    cd %SCRIPT_DIR%\modpack || exit /b 1
    .\pakkuw fetch
    cd %SCRIPT_DIR%\environment || exit /b 1
    .\gradlew %params%
) ELSE (
    echo Unknown command %COMMAND%
    echo Usage: %0 {pack|env} [args...]
    EXIT /B 1
)

cd %CWD% || exit /b 1
EXIT /B



