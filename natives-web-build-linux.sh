# A temporary script to assemble natives for web platforms (using Emscripten).
# Emscipten SDK dir should be on PATH.

emsdk_env.sh

LIB_NAME=gdx-basis-universal
# JS function to load the wasm wrapping module.
MODULE_CREAT_FUNCTIOn='gdx-basis-universal'

SRC_DIR='basisu-wrapper/jni/src'
BUILD_DIR='basisu-wrapper/jni/target/web'
OUT_DIR='basisu-wrapper/libs/web'

mkdir -p $BUILD_DIR
mkdir -p $OUT_DIR

INCLUDES_OPTIONS='-Ibasisu-wrapper/jni/include'
COMMON_OPTIONS='-std=c++11 -Wall -O2 -g0 -flto -fno-exceptions -s MODULARIZE=1 -s EXPORT_NAME=createBasisuGdxModule -s ENVIRONMENT=web -s WASM=1 --closure 1 --bind -mnontrapping-fptoint'
COMPILER_OPTIONS='-c'
OUTPUT_SUFFIX='.js'

# Compile
em++ $INCLUDES_OPTIONS $COMMON_OPTIONS -o $BUILD_DIR/basisu_transcoder.o   $COMPILER_OPTIONS $SRC_DIR/basisu_transcoder.cpp
em++ $INCLUDES_OPTIONS $COMMON_OPTIONS -o $BUILD_DIR/basisu_native_utils.o $COMPILER_OPTIONS $SRC_DIR/basisu_native_utils.cpp
em++ $INCLUDES_OPTIONS $COMMON_OPTIONS -o $BUILD_DIR/basisu_wrapper.o      $COMPILER_OPTIONS $SRC_DIR/basisu_wrapper.cpp

# Link
em++ $INCLUDES_OPTIONS $COMMON_OPTIONS -o $OUT_DIR/$LIB_NAME$OUTPUT_SUFFIX  $BUILD_DIR/basisu_transcoder.o $BUILD_DIR/basisu_native_utils.o $BUILD_DIR/basisu_wrapper.o
