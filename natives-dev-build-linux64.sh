#!/bin/bash
./gradlew jnigen && ./gradlew jnigenBuildLinux64 && ant -f basisu-wrapper/jni/build.xml -v pack-natives
