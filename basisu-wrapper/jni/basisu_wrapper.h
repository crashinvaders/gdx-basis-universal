#pragma once

#include <stdint.h>

#include "basisu_transcoder.h"
#include "basisu_containers.h"
#include "basisu_file_headers.h"

#define LOG_INFO "[BASISU_WRAPPER] INFO: "
#define LOG_ERROR "[BASISU_WRAPPER] ERROR: "

using namespace basist;

namespace basisuWrapper {

    struct ktx2_file_info {
        uint32_t layers;
        uint32_t mipmapLevels;
        uint32_t width;
        uint32_t height;
        bool hasAlpha;
        basist::basis_tex_format textureFormat;
    };

    bool isTranscoderTexFormatSupported(transcoder_texture_format transcoderTexFormat, basis_tex_format basisTexFormat);

    namespace basis {

        bool validateHeader(uint8_t *data, uint32_t dataSize);

        bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation);

        bool getFileInfo(basisu_file_info &fileInfo, uint8_t *data, uint32_t dataSize);

        bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex);

        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format);

    } // namespace basis

    namespace ktx2 {

        bool getFileInfo(basisuWrapper::ktx2_file_info& fileInfo, uint8_t *data, uint32_t dataSize);

        bool getImageLevelInfo(ktx2_image_level_info& imageInfo, uint8_t *data, uint32_t dataSize, uint32_t layerIndex, uint32_t levelIndex);

        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t layerIndex, uint32_t levelIndex, transcoder_texture_format format);

    } // namespace ktx

} // namespace basisuWrapper