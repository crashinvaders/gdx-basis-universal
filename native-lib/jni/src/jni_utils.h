#pragma once

#include <jni.h>

namespace jniUtils {

    jint throwException(JNIEnv* env, char* message);

}