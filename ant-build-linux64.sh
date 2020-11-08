#!/bin/bash
ant -f native-lib/jni/build-linux64.xml -v -Drelease=true clean postcompile && ant -f  native-lib/jni/build.xml -v pack-natives