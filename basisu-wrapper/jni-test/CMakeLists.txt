cmake_minimum_required(VERSION 3.1.0)
project(basisu-wrapper-test VERSION 0.1.0)

set (CMAKE_CXX_STANDARD 11)

add_definitions(-D__DESKTOP_TEST__)
add_definitions(-DBASISU_FORCE_DEVEL_MESSAGES=1)

# add_subdirectory("basis-universal")

include_directories("." "../jni" "../jni/basis-universal/transcoder")

file(GLOB SOURCES
    "./*.cpp"
    "../jni/basis-universal/transcoder/basisu_transcoder.cpp"
    "../jni/basisu_native_utils.cpp"
    "../jni/basisu_wrapper.cpp"
)

add_executable(basisu-wrapper-test ${SOURCES})