#include "basisu_native_utils.h"

#ifdef __ANDROID__
    #include <jni.h>
    #include <log.h>

    void basisuUtils::logInfo(const char *tag, const char *message) {
        __android_log_write( android_LogPriority::ANDROID_LOG_INFO, tag, message);
    }

    void basisuUtils::logError(const char *tag, const char *message) {
        __android_log_write( android_LogPriority::ANDROID_LOG_ERROR, tag, message);
    }
#else
    #include <iostream>

    void basisuUtils::logInfo(const char *tag, const char *message) {
        std::cout << "[" << tag << "] INFO: " << message << std::endl;
    }

    void basisuUtils::logError(const char *tag, const char *message) {
        std::cout << "[" << tag << "] ERROR: " << message << std::endl;
    }
#endif

#ifdef __EMSCRIPTEN__ 

    void basisuUtils::throwException(void*, const char *message) {
        //TODO Implement it.
    }

#elif __DESKTOP_TEST__ // Only required for desktop native tests (jni-test dir project).
    #include <iostream>
    #include <stdlib.h>

    void basisuUtils::throwException(void*, const char *message) {
        std::cout << "[EXCEPTION ERROR]: " << message << std::endl;
        exit(1);
    }

#else // General JNI case
    #include <jni.h>

    void basisuUtils::throwException(void *envRaw, const char *message) {
        JNIEnv *env = (JNIEnv*)envRaw;
        env->ThrowNew(env->FindClass("com/crashinvaders/basisu/wrapper/BasisuWrapperException"), message);
    }
#endif