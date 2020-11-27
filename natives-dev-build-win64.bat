@echo off
CALL gradlew jnigen
IF %ERRORLEVEL% NEQ 0 goto :eof
CALL gradlew jnigenBuildWin64
IF %ERRORLEVEL% NEQ 0 goto :eof
CALL ant.bat -f basisu-wrapper/jni/build.xml -v pack-natives