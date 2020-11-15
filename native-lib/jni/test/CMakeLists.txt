cmake_minimum_required(VERSION 3.0.0)
project(jnigen-test VERSION 0.1.0)

# add_definitions(BASISU_FORCE_DEVEL_MESSAGES=1)

include_directories("." "../src")

file(GLOB INCLUDES 
    "./*.h" 
    "../src/*.h"
)
file(GLOB SOURCES 
    "./*.cpp"
    "../src/*.cpp"
)

add_executable(jnigen-test ${INCLUDES} ${SOURCES})