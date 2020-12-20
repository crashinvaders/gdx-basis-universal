# This file is no longer used, but kept in case the native build system needs migration to CMake.
# It was a temporary CMake configuration to assemble natives for the web platform (using Emscripten).
cmake_minimum_required(VERSION 3.1.0)
project(gdx-basis-universal VERSION 0.1.0)

set(CMAKE_CXX_STANDARD 11)
# set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -O2 -g0 -flto -fno-exceptions") # -fno-rtti
if(EMSCRIPTEN)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s MODULARIZE=1 -s EXPORT_NAME=createBasisuGdxModule -s ENVIRONMENT=web -s WASM=1 --closure 1 --bind -mnontrapping-fptoint")
endif()

include_directories("include")

file(GLOB SOURCES
    "src/basisu_native_utils.cpp"
    "src/basisu_transcoder.cpp"
    "src/basisu_wrapper.cpp"
)

add_executable(${CMAKE_PROJECT_NAME} ${SOURCES})

if(EMSCRIPTEN)
    set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../libs/web"
    )
endif()