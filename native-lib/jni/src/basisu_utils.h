#pragma once

#include <stdint.h>
#include <vector>

#include "basisu_transcoder.h"

#define LOG_TAG "[BASISU_UTILS] "

using namespace basist;

namespace basisuUtils {

    bool validateHeader(uint8_t* data, size_t size);

    bool transcode(std::vector<uint8_t> &out, uint8_t* basisuData, size_t basisuDataSize,
                   uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format);
    
}