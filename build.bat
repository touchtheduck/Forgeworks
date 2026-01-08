@ECHO OFF

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
    cd modpack || exit /b 1
    .\pakkuw %params%
    cd .. || exit /b 1
) ELSE IF "%COMMAND%"=="env" (
    cd modpack || exit /b 1
    .\pakkuw fetch
    cd ..\environment || exit /b 1
    .\gradlew %params%
     cd .. || exit /b 1
) ELSE (
    echo Unknown command %COMMAND%
    echo Usage: %0 {pack|env} [args...]
    EXIT /B 1
)

EXIT /B



