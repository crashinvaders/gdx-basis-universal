# A temporary CMake configuration to assemble natives for web platforms (using Emscripten).
cmake_minimum_required(VERSION 3.1.0)
project(gdx-basis-universal VERSION 0.1.0)

set(CMAKE_CXX_STANDARD 11)
# set(CMAKE_CXX_EXTENSIONS OFF)

set(EM_COMMON_FLAGS "-Wall -O2 -g0 -flto -fno-exceptions") # -fno-rtti
set(EM_EMSCRIPTEN_FLAGS "-s MODULARIZE=1 -s EXPORT_NAME=createBasisuGdxModule -s ENVIRONMENT=web -s WASM=1 --closure 1 --bind -mnontrapping-fptoint")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${EM_COMMON_FLAGS} ${EM_EMSCRIPTEN_FLAGS}")

include_directories("include")

file(GLOB SOURCES
    "src/basisu_native_utils.cpp"
    "src/basisu_transcoder.cpp"
    "src/basisu_wrapper.cpp"
)

add_executable(${CMAKE_PROJECT_NAME} ${SOURCES})

set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../libs/web"
)