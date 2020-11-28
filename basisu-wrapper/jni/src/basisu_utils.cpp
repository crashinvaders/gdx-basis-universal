#include "basisu_utils.h"

namespace basisuUtils {

    #ifdef __ANDROID__
    #include <android/log.h>

        void logInfo(const char* tag, const char* message) {
            //TODO Implement it.
        }

        void logError(const char* tag, const char* message) {
            //TODO Implement it.
        }
    #else
    // #include <iostream>

        void logInfo(const char* tag, const char* message) {
            // std::cout << "[" << tag << "] INFO: " << message << std::endl;
        }

        void logError(const char* tag, const char* message) {
            // std::cout << "[" << tag << "] ERROR: " << message << std::endl;
        }
    #endif

}