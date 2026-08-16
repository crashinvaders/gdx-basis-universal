#pragma once

#include <stdint.h>

// Forward declaration only - avoids pulling in "basisu_containers.h" here, since that vendor
// header assumes a bunch of standard headers (<climits>, <cstring>, ...) are already transitively
// included by whatever includes it first (normally "basisu_transcoder.h"/"basisu_wrapper.h").
// A reference parameter only needs the template to be known, not its full definition.
namespace basisu {
    template<typename T> class vector;
}

namespace basisuUtils {

    void logInfo(const char* tag, const char* message);

    void logError(const char* tag, const char* message);

    /**
     * Throws an exception in the high-level wrapping code.
     * @param env is "JNIEnv" for JNI implementations and nullptr for other platforms (Emscripten).
     */
    void throwException(void *env, const char *message);

    /**
     * "basisu::vector<uint8_t>" exposes no detach()/release() API for taking ownership of its
     * already-malloc'd buffer without a copy. This steals it by swapping it into a vector that is
     * intentionally never destructed - a few bytes of vector bookkeeping "leaked" once per call,
     * never the (potentially multi-megabyte) buffer itself.
     * <p/>
     * Use this when handing the buffer's ownership off to a caller that will "free()" it
     * independently (e.g. wrapping it in a JNI direct ByteBuffer), instead of allocating and
     * memcpy-ing a second same-size buffer just to get an ownership-transferable pointer.
     */
    uint8_t* detachBuffer(basisu::vector<uint8_t> &vec, uint32_t &outSizeInBytes);

}