#include "jni_utils.h"

namespace jniUtils {

    jint throwException(JNIEnv* env, char* message) {
        return env->ThrowNew(env->FindClass("com/metaphore/jnigentest/JniGenException"), message);
    }

}