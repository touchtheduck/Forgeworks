@ECHO OFF

WHERE /Q java
IF ERRORLEVEL 1 (
	ECHO Java is not installed. Please install Java to use Pakku. 1>&2
	EXIT /B 1
)

java -jar pakku.jar %*