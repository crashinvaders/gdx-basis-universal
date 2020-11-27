#!/bin/bash
./gradlew jnigen && ./gradlew jnigenBuildMacOsX64 && ant -f basisu-wrapper/jni/build.xml -v pack-natives
