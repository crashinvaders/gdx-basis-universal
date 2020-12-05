@REM A temporary script to assemble natives for web platforms (using Emscripten).
@echo off

call emsdk_env.bat

set SOURCE_DIR=basisu-wrapper\jni
set BUILD_DIR=basisu-wrapper\jni\target\web
call mkdir %BUILD_DIR%

@REM cd %BUILD_DIR%

set CMAKE_TOOLCHAIN_FILE=C:\emsdk\upstream\emscripten\cmake\Modules\Platform\Emscripten.cmake
call emcmake cmake -B%BUILD_DIR% -S%SOURCE_DIR% -H%SOURCE_DIR%
call emmake cmake --build %BUILD_DIR%
@REM call emmake make -C %BUILD_DIR%