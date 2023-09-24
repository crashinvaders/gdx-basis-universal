#pragma once

#include <stdint.h>

#include "basisu_transcoder.h"
#include "basisu_containers.h"
#include "basisu_file_headers.h"

#define LOG_INFO "[BASISU_WRAPPER] INFO: "
#define LOG_ERROR "[BASISU_WRAPPER] ERROR: "

using namespace basist;

namespace basisuWrapper {

    bool validateHeader(uint8_t *data, uint32_t dataSize);

    bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation);

    int getTotalMipMapLevels(uint8_t *data, uint32_t dataSize);

    bool getFileInfo(basisu_file_info &fileInfo, uint8_t *data, uint32_t dataSize);

    bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex);

    bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format);

    bool isTranscoderTexFormatSupported(transcoder_texture_format transcoderTexFormat, basis_tex_format basisTexFormat); 
} // namespace basisuWrapper