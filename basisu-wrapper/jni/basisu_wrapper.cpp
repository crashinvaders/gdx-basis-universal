#include <cstdio>
#include <cstring>

#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

using namespace basist;

namespace basisuWrapper {

#define LOG_TAG "basisu_wrapper.cpp"

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized)
            return;

        basisuUtils::logInfo(LOG_TAG, (std::string("Basis Universal ") + BASISD_VERSION_STRING).c_str());
        basisuUtils::logInfo(LOG_TAG, "Initializing global basisu parser.");

        basisuInitialized = true;

        basisu_transcoder_init();
    }

    bool isTranscoderTexFormatSupported(transcoder_texture_format transcoderTexFormat, basis_tex_format basisTexFormat) {
        return basis_is_format_supported(transcoderTexFormat, basisTexFormat);
    }

    namespace basis {

        bool validateHeader(uint8_t *data, uint32_t dataSize) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.validate_header(data, dataSize);
        }

        bool validateChecksum(uint8_t *data, uint32_t dataSize, bool fullValidation) {
            initBasisu();
            basisu_transcoder transcoder = {};
            return transcoder.validate_file_checksums(data, dataSize, fullValidation);
        }

        bool getFileInfo(basisu_file_info &fileInfo, uint8_t *data, uint32_t dataSize)  {
            initBasisu();
            basisu_transcoder transcoder = {};
            bool successful = transcoder.get_file_info(data, dataSize, fileInfo);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain file info.");
            }
            return successful;
        }

        bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex) {
            initBasisu();
            basisu_transcoder transcoder = {};
            bool successful = transcoder.get_image_info(data, dataSize, imageInfo, imageIndex);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain image info.");
            }
            return successful;
        }
        
        bool getImageLevelInfo(basisu_image_level_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex, uint32_t imageLevel) {
            initBasisu();
            basisu_transcoder transcoder = {};
            bool successful = transcoder.get_image_level_info(data, dataSize, imageInfo, imageIndex, imageLevel);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain image level info.");
            }
            return successful;
        }

        // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format) {
            initBasisu();
            basisu_transcoder transcoder = {};

            uint32_t origWidth, origHeight, totalBlocks;
            if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
                basisuUtils::logError(LOG_TAG, "Failed to retrieve image level description.");
                return false;
            }

            uint32_t flags = 0;

            bool status;

            if (!transcoder.start_transcoding(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to init transcoding for Basis data.");
                return false;
            }

            if (basis_transcoder_format_is_uncompressed(format)) {

                const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
                const uint32_t bytesPerLine = origWidth * bytesPerPixel;
                const uint32_t bytesPerSlice = bytesPerLine * origHeight;

                out.resize(bytesPerSlice);

                status = transcoder.transcode_image_level(
                    data, dataSize, imageIndex, levelIndex,
                    out.data(), origWidth * origHeight,
//                    format,
                    static_cast<basist::transcoder_texture_format>(format),
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

    } // namespace basis

    namespace ktx2 {

        bool getFileInfo(basisuWrapper::ktx2_file_info& fileInfo, uint8_t *data, uint32_t dataSize) {
            initBasisu();

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return false;
            }

            fileInfo.layers = transcoder.get_layers();
            fileInfo.mipmapLevels = transcoder.get_levels();
            fileInfo.width = transcoder.get_width();
            fileInfo.height = transcoder.get_height();
            fileInfo.hasAlpha = transcoder.get_has_alpha();
            fileInfo.textureFormat = transcoder.get_format();

            transcoder.clear();

            return true;
        }

        bool getImageLevelInfo(ktx2_image_level_info& imageInfo, uint8_t *data, uint32_t dataSize, uint32_t layerIndex, uint32_t levelIndex) {
            initBasisu();

            // This value is hardcoded for now as cube-textures aren't support ATM.
            int faceIndex = 0;

            ktx2_transcoder transcoder = {};
            if (!transcoder.init(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
                return false;
            }

            transcoder.get_image_level_info(imageInfo, levelIndex, layerIndex, faceIndex);
            transcoder.clear();
            return true;
        }

        bool transcode(basisu::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                       uint32_t layerIndex, uint32_t levelIndex, transcoder_texture_format format) {

            initBasisu();
            ktx2_transcoder transcoder = {};

            // This value is hardcoded for now as cube-textures aren't support ATM.
            int faceIndex = 0;

            if (!transcoder.init(data, dataSize)) {
               basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
               return false;
            }

            if (!transcoder.start_transcoding()) {
                basisuUtils::logError(LOG_TAG, "Failed to init transcoding for KTX2 data.");
                return false;
            }

            ktx2_image_level_info levelInfo = {};
            if (!transcoder.get_image_level_info(levelInfo, levelIndex, layerIndex, faceIndex)) {
                basisuUtils::logError(LOG_TAG, "Failed to read image level info from KTX2 data.");
                return false;
            }

            uint32_t origWidth = levelInfo.m_orig_width;
            uint32_t origHeight = levelInfo.m_orig_height;
            uint32_t totalBlocks = levelInfo.m_total_blocks;
            uint32_t outBufSize = 0;
            uint32_t outBufBlocks = 0;
            uint32_t decodeFlags = 0;
            bool status;

            // Compute the output buffer size and total block/pixel amount.
            if (basis_transcoder_format_is_uncompressed(format)) {

                const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
                const uint32_t bytesPerLine = origWidth * bytesPerPixel;
                const uint32_t bytesPerSlice = bytesPerLine * origHeight;

                outBufSize = bytesPerSlice;
                outBufBlocks = origWidth * origHeight;

            } else {

                uint32_t bytesPerBlock = basis_get_bytes_per_block_or_pixel(format);
                uint32_t requiredSize = totalBlocks * bytesPerBlock;

                if (format == transcoder_texture_format::cTFPVRTC1_4_RGB ||
                    format == transcoder_texture_format::cTFPVRTC1_4_RGBA) {
                   // For PVRTC1, Basis only writes (or requires) total_blocks * bytes_per_block. But GL requires extra padding for very small textures:
                   // https://www.khronos.org/registry/OpenGL/extensions/IMG/IMG_texture_compression_pvrtc.txt
                   // The transcoder will clear the extra bytes followed the used blocks to 0.
                   const uint32_t width = (origWidth + 3) & ~3;
                   const uint32_t height = (origHeight + 3) & ~3;
                   requiredSize = (std::max(8U, width) * std::max(8U, height) * 4 + 7) / 8;
                   assert(requiredSize >= totalBlocks * bytesPerBlock);
                }

                outBufSize = requiredSize;
                outBufBlocks = requiredSize / bytesPerBlock;
            }

            out.resize(outBufSize);

            status = transcoder.transcode_image_level(
                levelIndex, layerIndex, 0,
                out.data(), outBufBlocks,
                static_cast<basist::transcoder_texture_format>(format),
                decodeFlags);

            transcoder.clear();

            return status;
        }

    } // namespace ktx2

} // namespace basisuWrapper
