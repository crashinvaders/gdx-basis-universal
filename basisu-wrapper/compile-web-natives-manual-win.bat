@REM A temporary script to assemble natives for web platforms (using Emscripten).
@echo off

call emsdk_env.bat

set LIB_NAME=gdx-basis-universal
@REM JS function to load the wasm wrapping module.
set MODULE_CREAT_FUNCTIOn=gdx-basis-universal

set SRC_DIR=jni\src
set BUILD_DIR=jni\target\web
set OUT_DIR=libs\web

call mkdir %BUILD_DIR%
call mkdir %OUT_DIR%

set INCLUDES_OPTIONS=-Ijni\include
set COMMON_OPTIONS=-std=c++11 -Wall -flto -fno-exceptions -s MODULARIZE=1 -s EXPORT_NAME=createBasisuGdxModule -s ENVIRONMENT=web -s WASM=1 --bind -mnontrapping-fptoint
set COMPILER_OPTIONS=-c
set OUTPUT_SUFFIX=.js

@REM Compile
call em++ %INCLUDES_OPTIONS% %COMMON_OPTIONS% -o %BUILD_DIR%\basisu_transcoder.o   %COMPILER_OPTIONS% %SRC_DIR%\basisu_transcoder.cpp
call em++ %INCLUDES_OPTIONS% %COMMON_OPTIONS% -o %BUILD_DIR%\basisu_native_utils.o %COMPILER_OPTIONS% %SRC_DIR%\basisu_native_utils.cpp
call em++ %INCLUDES_OPTIONS% %COMMON_OPTIONS% -o %BUILD_DIR%\basisu_wrapper.o      %COMPILER_OPTIONS% %SRC_DIR%\basisu_wrapper.cpp

@REM Link
call em++ %INCLUDES_OPTIONS% %COMMON_OPTIONS% -o %OUT_DIR%\%LIB_NAME%%OUTPUT_SUFFIX%  %BUILD_DIR%\basisu_transcoder.o %BUILD_DIR%\basisu_native_utils.o %BUILD_DIR%\basisu_wrapper.o
