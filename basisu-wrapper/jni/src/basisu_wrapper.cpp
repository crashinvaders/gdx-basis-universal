#include <iostream>
#include <cstdio>

#include "basisu_wrapper.h"
#include "basisu_transcoder.h"

using namespace std;
using namespace basist;

namespace basisuWrapper {

    static etc1_global_selector_codebook codebook;

    void reportError(const char *message) {
        cout << LOG_ERROR << message << endl;
    }

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized)
            return;

        cout << LOG_INFO << "Basis Universal " << BASISD_VERSION_STRING << endl;
        cout << LOG_INFO << "Initializing global basisu parser." << endl;

        basisuInitialized = true;

        basisu_transcoder_init();

        codebook.init(g_global_selector_cb_size, g_global_selector_cb);
    }

    bool validateHeader(uint8_t *data, uint32_t dataSize) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        return transcoder.validate_header(data, dataSize);
    }

    bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        return transcoder.validate_file_checksums(data, dataSize, fullValidation);
    }

    int getTotalMipMapLevels(uint8_t *data, uint32_t dataSize) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        return transcoder.get_total_image_levels(data, dataSize, 0);
    }

    bool getFileInfo(basisu_file_info &fileInfo, uint8_t *data, uint32_t dataSize)  {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        bool successful = transcoder.get_file_info(data, dataSize, fileInfo);
        if (!successful) {
            reportError("Failed to obtain file info.");
        }
        return successful;
    }

    bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        bool successful = transcoder.get_image_info(data, dataSize, imageInfo, imageIndex);
        if (!successful) {
            reportError("Failed to obtain image info.");
        }
        return successful;
    }

    // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
    bool transcode(std::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);

        std::cout << LOG_INFO 
        << "Transcoding basis image (image index: " 
        << imageIndex << ", level index: " << levelIndex 
        << ") into texture format with ID: " << (int)format 
        << std::endl;

        uint32_t origWidth, origHeight, totalBlocks;
        if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
            reportError("Failed to retrieve image level description.");
            return false;
        }

        uint32_t flags = 0;

        bool status;

        if (!transcoder.start_transcoding(data, dataSize)) {
            return false;
        }

        if (basis_transcoder_format_is_uncompressed(format)) {

            const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
            const uint32_t bytesPerLine = origWidth * bytesPerPixel;
            const uint32_t bytesPerSlice = bytesPerLine * origHeight;

            out.resize(bytesPerSlice);

            status = transcoder.transcode_image_level(
                data, dataSize, 0, levelIndex,
                out.data(), origWidth * origHeight,
                format,
                flags,
                origWidth,
                nullptr,
                origHeight);

        } else {

            uint32_t bytesPerBlock = basis_get_bytes_per_block_or_pixel(format);
            uint32_t requiredSize = totalBlocks * bytesPerBlock;

            if (format == transcoder_texture_format::cTFPVRTC1_4_RGB || format == transcoder_texture_format::cTFPVRTC1_4_RGBA) {
                // For PVRTC1, Basis only writes (or requires) total_blocks * bytes_per_block. But GL requires extra padding for very small textures:
                // https://www.khronos.org/registry/OpenGL/extensions/IMG/IMG_texture_compression_pvrtc.txt
                // The transcoder will clear the extra bytes followed the used blocks to 0.
                const uint32_t width = (origWidth + 3) & ~3;
                const uint32_t height = (origHeight + 3) & ~3;
                requiredSize = (std::max(8U, width) * std::max(8U, height) * 4 + 7) / 8;
                assert(requiredSize >= totalBlocks * bytesPerBlock);
            }

            out.resize(requiredSize);

            status = transcoder.transcode_image_level(
                data, dataSize, imageIndex, levelIndex,
                out.data(), out.size() / bytesPerBlock,
                static_cast<basist::transcoder_texture_format>(format),
                flags);
        }

        transcoder.stop_transcoding();

        return status;
    }
} // namespace basisuWrapper