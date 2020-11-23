#include "jni_utils.h"

namespace jniUtils {

    jint throwException(JNIEnv *env, const char *message) {
        return env->ThrowNew(env->FindClass("com/crashinvaders/basisu/BasisuException"), message);
    }

}