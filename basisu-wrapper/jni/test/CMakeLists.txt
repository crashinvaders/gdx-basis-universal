cmake_minimum_required(VERSION 3.1.0)
project(basisu-wrapper-test VERSION 0.2.0)

set (CMAKE_CXX_STANDARD 11)

add_definitions(-D__DESKTOP_TEST__)
add_definitions(-DBASISD_SUPPORT_FXT1=0)
add_definitions(-DBASISD_SUPPORT_PVRTC1=0)
add_definitions(-DBASISU_FORCE_DEVEL_MESSAGES=1)

# add_subdirectory("basis-universal")

include_directories(
    "."
    ".."
    "../basis-universal/transcoder"
    "../basis-universal/zstd"
)

file(GLOB SOURCES
    "./*.cpp"
    "../basis-universal/transcoder/basisu_transcoder.cpp"
    "../basis-universal/zstd/zstddeclib.c"
    "../basisu_native_utils.cpp"
    "../basisu_wrapper.cpp"
)

add_executable(basisu-wrapper-test ${SOURCES})