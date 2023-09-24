#!/bin/sh

# To build Windows natives from under a UNIX environment, you need to have mingw64 installed.
# Please read the instructions from basisu-wrapper/README.md

./gradlew jnigen jnigenBuildWindows64 jnigenJarNativesDesktop
