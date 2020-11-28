#pragma once

#include <jni.h>

namespace jniUtils {

    jint throwException(JNIEnv *env, const char *message);

}