#include "basisu_native_utils.h"


// LOGGING ==============================

#if defined __EMSCRIPTEN__
    #include <emscripten.h>

    void basisuUtils::logInfo(const char *tag, const char *message) {
        emscripten_log(EM_LOG_INFO, "[%s] INFO: %s", tag, message);
    }

    void basisuUtils::logError(const char *tag, const char *message) {
        emscripten_log(EM_LOG_ERROR, "[%s] ERROR: %s", tag, message);
    }

#elif defined __ANDROID__
    #include <jni.h>
    #include <log.h>

    void basisuUtils::logInfo(const char *tag, const char *message) {
        __android_log_write( android_LogPriority::ANDROID_LOG_INFO, tag, message);
    }

    void basisuUtils::logError(const char *tag, const char *message) {
        __android_log_write( android_LogPriority::ANDROID_LOG_ERROR, tag, message);
    }

#else // General JNI case
    #include <iostream>

    void basisuUtils::logInfo(const char *tag, const char *message) {
        std::cout << "[" << tag << "] INFO: " << message << std::endl;
    }

    void basisuUtils::logError(const char *tag, const char *message) {
        std::cout << "[" << tag << "] ERROR: " << message << std::endl;
    }

#endif


// EXCEPTIONS ==============================

#if defined __EMSCRIPTEN__
    #include <emscripten.h>

    void basisuUtils::throwException(void*, const char *message) {
        //TODO Implement it.
    }

#elif defined __DESKTOP_TEST__ // Only required for desktop native tests (jni-test dir project).
    #include <iostream>
    #include <stdlib.h>

    void basisuUtils::throwException(void*, const char *message) {
        std::cout << "[EXCEPTION ERROR]: " << message << std::endl;
        exit(1);
    }

#else // General JNI case
    #include <iostream>
    #include <jni.h>

    void basisuUtils::throwException(void *envRaw, const char *message) {
        JNIEnv *env = (JNIEnv*)envRaw;
        env->ThrowNew(env->FindClass("com/crashinvaders/basisu/wrapper/BasisuWrapperException"), message);
    }
    
#endif