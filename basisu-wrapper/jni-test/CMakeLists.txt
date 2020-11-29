cmake_minimum_required(VERSION 3.1.0)
project(jnigen-test VERSION 0.1.0)

set (CMAKE_CXX_STANDARD 11)

add_definitions(-D__DESKTOP_TEST__)
add_definitions(-DBASISU_FORCE_DEVEL_MESSAGES=1)

include_directories("." "../jni/include")

file(GLOB SOURCES
    "./*.cpp"
    "../jni/src/basisu_transcoder.cpp"
    "../jni/src/basisu_native_utils.cpp"
    "../jni/src/basisu_wrapper.cpp"
)

add_executable(jnigen-test ${SOURCES})