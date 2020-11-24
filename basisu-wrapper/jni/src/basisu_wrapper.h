#pragma once

#include <stdint.h>
#include <vector>

#include "basisu_transcoder.h"

#define LOG_TAG "[BASISU_WRAPPER] "

using namespace basist;

namespace basisuWrapper {

    bool validateHeader(uint8_t *data, uint32_t dataSize);

    bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation);

    int getTotalMipMapLevels(uint8_t *data, uint32_t dataSize);

    bool getFileInfo(basisu_file_info &fileInfo, uint8_t *data, uint32_t dataSize);

    bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex);

    bool transcode(std::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t levelIndex, transcoder_texture_format format);
} // namespace basisuWrapper