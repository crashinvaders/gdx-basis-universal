#!/bin/bash
mkdir build
cd build || exit
cmake .. && cmake --build . && cd .. && ./build/jnigen-test