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

        bool getImageLevelInfo(basisu_image_level_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t imageLevel);

        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format);

        // Keeps a single basisu_transcoder alive across multiple info/transcode calls for the
        // same file, so the ETC1S global codebooks are decoded (start_transcoding()) at most once
        // per file instead of once per transcode() call (e.g. once per mipmap level).
        class TranscoderSession {
        public:
            TranscoderSession(uint8_t *data, uint32_t dataSize);
            ~TranscoderSession();

            bool validateHeader();
            bool validateChecksum(bool fullValidation);
            bool getFileInfo(basisu_file_info &fileInfo);
            bool getImageInfo(basisu_image_info &imageInfo, uint32_t imageIndex);
            bool getImageLevelInfo(basisu_image_level_info &imageInfo, uint32_t imageIndex, uint32_t imageLevel);
            bool transcode(basisu::vector<uint8_t> &out, uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format);

            // Transcodes every mipmap level of the image into one contiguous buffer (one allocation
            // for the whole chain instead of one per level). "outLevelOffsets" is filled with
            // totalLevels+1 byte offsets into "out" - level i occupies [outLevelOffsets[i], outLevelOffsets[i+1]).
            bool transcodeAllLevels(basisu::vector<uint8_t> &out, basisu::vector<uint32_t> &outLevelOffsets,
                                     uint32_t imageIndex, transcoder_texture_format format);

        private:
            uint8_t *data;
            uint32_t dataSize;
            basisu_transcoder transcoder;
            bool transcodingStarted;
        };

    } // namespace basis

    namespace ktx2 {

        bool getFileInfo(basisuWrapper::ktx2_file_info& fileInfo, uint8_t *data, uint32_t dataSize);

        bool getImageLevelInfo(ktx2_image_level_info& imageInfo, uint8_t *data, uint32_t dataSize, uint32_t layerIndex, uint32_t levelIndex);

        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t layerIndex, uint32_t levelIndex, transcoder_texture_format format);

        // Same idea as basis::TranscoderSession: init() (header parse) and start_transcoding()
        // (ETC1S global data decompression) are both run at most once per file.
        class TranscoderSession {
        public:
            TranscoderSession(uint8_t *data, uint32_t dataSize);
            ~TranscoderSession();

            bool getFileInfo(basisuWrapper::ktx2_file_info &fileInfo);
            bool getImageLevelInfo(ktx2_image_level_info &imageInfo, uint32_t layerIndex, uint32_t levelIndex);
            bool transcode(basisu::vector<uint8_t> &out, uint32_t layerIndex, uint32_t levelIndex, transcoder_texture_format format);

            // Same idea as basis::TranscoderSession::transcodeAllLevels.
            bool transcodeAllLevels(basisu::vector<uint8_t> &out, basisu::vector<uint32_t> &outLevelOffsets,
                                     uint32_t layerIndex, transcoder_texture_format format);

        private:
            ktx2_transcoder transcoder;
            bool initialized;
            bool transcodingStarted;
        };

    } // namespace ktx

} // namespace basisuWrapper
