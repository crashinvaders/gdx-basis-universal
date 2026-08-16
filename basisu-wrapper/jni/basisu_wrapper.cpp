#include <cstdio>
#include <cstring>
#include <mutex>

#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

using namespace basist;

namespace basisuWrapper {

#define LOG_TAG "basisu_wrapper.cpp"

    // Computes the required output buffer size (in bytes) and the block/pixel count expected by
    // basisu_transcoder::transcode_image_level() for the given format and level dimensions.
    static uint32_t computeTranscodedLevelSize(transcoder_texture_format format, uint32_t origWidth, uint32_t origHeight,
                                                uint32_t totalBlocks, uint32_t &outBlocksOrPixels) {
        if (basis_transcoder_format_is_uncompressed(format)) {
            const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
            outBlocksOrPixels = origWidth * origHeight;
            return outBlocksOrPixels * bytesPerPixel;
        }

        const uint32_t bytesPerBlock = basis_get_bytes_per_block_or_pixel(format);
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

        outBlocksOrPixels = requiredSize / bytesPerBlock;
        return requiredSize;
    }

    void initBasisu() {
        static std::once_flag basisuInitFlag;
        std::call_once(basisuInitFlag, []() {
            basisuUtils::logInfo(LOG_TAG, (std::string("Basis Universal ") + BASISD_VERSION_STRING).c_str());
            basisuUtils::logInfo(LOG_TAG, "Initializing global basisu parser.");

            basisu_transcoder_init();
        });
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

            if (!transcoder.start_transcoding(data, dataSize)) {
                basisuUtils::logError(LOG_TAG, "Failed to init transcoding for Basis data.");
                return false;
            }

            uint32_t blocksOrPixels;
            out.resize(computeTranscodedLevelSize(format, origWidth, origHeight, totalBlocks, blocksOrPixels));

            bool status;
            if (basis_transcoder_format_is_uncompressed(format)) {
                status = transcoder.transcode_image_level(
                    data, dataSize, imageIndex, levelIndex,
                    out.data(), blocksOrPixels,
                    format, 0, origWidth, nullptr, origHeight);
            } else {
                status = transcoder.transcode_image_level(
                    data, dataSize, imageIndex, levelIndex,
                    out.data(), blocksOrPixels,
                    format, 0);
            }

            transcoder.stop_transcoding();

            return status;
        }

        TranscoderSession::TranscoderSession(uint8_t *data, uint32_t dataSize)
            : data(data), dataSize(dataSize), transcoder(), transcodingStarted(false) {
            initBasisu();
        }

        TranscoderSession::~TranscoderSession() {
            if (transcodingStarted) {
                transcoder.stop_transcoding();
            }
        }

        bool TranscoderSession::validateHeader() {
            return transcoder.validate_header(data, dataSize);
        }

        bool TranscoderSession::validateChecksum(bool fullValidation) {
            return transcoder.validate_file_checksums(data, dataSize, fullValidation);
        }

        bool TranscoderSession::getFileInfo(basisu_file_info &fileInfo) {
            bool successful = transcoder.get_file_info(data, dataSize, fileInfo);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain file info.");
            }
            return successful;
        }

        bool TranscoderSession::getImageInfo(basisu_image_info &imageInfo, uint32_t imageIndex) {
            bool successful = transcoder.get_image_info(data, dataSize, imageInfo, imageIndex);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain image info.");
            }
            return successful;
        }

        bool TranscoderSession::getImageLevelInfo(basisu_image_level_info &imageInfo, uint32_t imageIndex, uint32_t imageLevel) {
            bool successful = transcoder.get_image_level_info(data, dataSize, imageInfo, imageIndex, imageLevel);
            if (!successful) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain image level info.");
            }
            return successful;
        }

        // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
        bool TranscoderSession::transcode(basisu::vector<uint8_t> &out, uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format) {
            uint32_t origWidth, origHeight, totalBlocks;
            if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
                basisuUtils::logError(LOG_TAG, "Failed to retrieve image level description.");
                return false;
            }

            if (!transcodingStarted) {
                if (!transcoder.start_transcoding(data, dataSize)) {
                    basisuUtils::logError(LOG_TAG, "Failed to init transcoding for Basis data.");
                    return false;
                }
                transcodingStarted = true;
            }

            uint32_t blocksOrPixels;
            out.resize(computeTranscodedLevelSize(format, origWidth, origHeight, totalBlocks, blocksOrPixels));

            if (basis_transcoder_format_is_uncompressed(format)) {
                return transcoder.transcode_image_level(
                    data, dataSize, imageIndex, levelIndex,
                    out.data(), blocksOrPixels,
                    format, 0, origWidth, nullptr, origHeight);
            }
            return transcoder.transcode_image_level(
                data, dataSize, imageIndex, levelIndex,
                out.data(), blocksOrPixels,
                format, 0);
        }

        bool TranscoderSession::transcodeAllLevels(basisu::vector<uint8_t> &out, basisu::vector<uint32_t> &outLevelOffsets,
                                                    uint32_t imageIndex, transcoder_texture_format format) {
            basisu_image_info imageInfo;
            if (!transcoder.get_image_info(data, dataSize, imageInfo, imageIndex)) {
                basisuUtils::logError(LOG_TAG, "Failed to obtain image info.");
                return false;
            }
            uint32_t totalLevels = imageInfo.m_total_levels;

            // First pass: compute every level's output size (cheap, header-only) so we can allocate
            // "out" once for the whole mip chain instead of once per level.
            basisu::vector<uint32_t> levelBlocksOrPixels(totalLevels);
            outLevelOffsets.resize(totalLevels + 1);
            uint32_t totalSize = 0;
            for (uint32_t level = 0; level < totalLevels; level++) {
                uint32_t origWidth, origHeight, totalBlocks;
                if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, level, origWidth, origHeight, totalBlocks)) {
                    basisuUtils::logError(LOG_TAG, "Failed to retrieve image level description.");
                    return false;
                }
                outLevelOffsets[level] = totalSize;
                totalSize += computeTranscodedLevelSize(format, origWidth, origHeight, totalBlocks, levelBlocksOrPixels[level]);
            }
            outLevelOffsets[totalLevels] = totalSize;

            out.resize(totalSize);

            if (!transcodingStarted) {
                if (!transcoder.start_transcoding(data, dataSize)) {
                    basisuUtils::logError(LOG_TAG, "Failed to init transcoding for Basis data.");
                    return false;
                }
                transcodingStarted = true;
            }

            for (uint32_t level = 0; level < totalLevels; level++) {
                uint32_t origWidth, origHeight, totalBlocks;
                transcoder.get_image_level_desc(data, dataSize, imageIndex, level, origWidth, origHeight, totalBlocks);
                uint8_t *levelOut = out.data() + outLevelOffsets[level];

                bool status;
                if (basis_transcoder_format_is_uncompressed(format)) {
                    status = transcoder.transcode_image_level(
                        data, dataSize, imageIndex, level,
                        levelOut, levelBlocksOrPixels[level],
                        format, 0, origWidth, nullptr, origHeight);
                } else {
                    status = transcoder.transcode_image_level(
                        data, dataSize, imageIndex, level,
                        levelOut, levelBlocksOrPixels[level],
                        format, 0);
                }
                if (!status) {
                    return false;
                }
            }

            return true;
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

            uint32_t blocksOrPixels;
            out.resize(computeTranscodedLevelSize(format, levelInfo.m_orig_width, levelInfo.m_orig_height, levelInfo.m_total_blocks, blocksOrPixels));

            bool status = transcoder.transcode_image_level(
                levelIndex, layerIndex, 0,
                out.data(), blocksOrPixels,
                format, 0);

            transcoder.clear();

            return status;
        }

        TranscoderSession::TranscoderSession(uint8_t *data, uint32_t dataSize) : transcoder(), transcodingStarted(false) {
            initBasisu();
            initialized = transcoder.init(data, dataSize);
            if (!initialized) {
                basisuUtils::logError(LOG_TAG, "Failed to read KTX2 data.");
            }
        }

        TranscoderSession::~TranscoderSession() {
            if (initialized) {
                transcoder.clear();
            }
        }

        bool TranscoderSession::getFileInfo(basisuWrapper::ktx2_file_info &fileInfo) {
            if (!initialized) {
                return false;
            }

            fileInfo.layers = transcoder.get_layers();
            fileInfo.mipmapLevels = transcoder.get_levels();
            fileInfo.width = transcoder.get_width();
            fileInfo.height = transcoder.get_height();
            fileInfo.hasAlpha = transcoder.get_has_alpha();
            fileInfo.textureFormat = transcoder.get_format();

            return true;
        }

        bool TranscoderSession::getImageLevelInfo(ktx2_image_level_info &imageInfo, uint32_t layerIndex, uint32_t levelIndex) {
            if (!initialized) {
                return false;
            }

            // This value is hardcoded for now as cube-textures aren't support ATM.
            int faceIndex = 0;

            return transcoder.get_image_level_info(imageInfo, levelIndex, layerIndex, faceIndex);
        }

        bool TranscoderSession::transcode(basisu::vector<uint8_t> &out, uint32_t layerIndex, uint32_t levelIndex, transcoder_texture_format format) {
            if (!initialized) {
                return false;
            }

            // This value is hardcoded for now as cube-textures aren't support ATM.
            int faceIndex = 0;

            if (!transcodingStarted) {
                if (!transcoder.start_transcoding()) {
                    basisuUtils::logError(LOG_TAG, "Failed to init transcoding for KTX2 data.");
                    return false;
                }
                transcodingStarted = true;
            }

            ktx2_image_level_info levelInfo = {};
            if (!transcoder.get_image_level_info(levelInfo, levelIndex, layerIndex, faceIndex)) {
                basisuUtils::logError(LOG_TAG, "Failed to read image level info from KTX2 data.");
                return false;
            }

            uint32_t blocksOrPixels;
            out.resize(computeTranscodedLevelSize(format, levelInfo.m_orig_width, levelInfo.m_orig_height, levelInfo.m_total_blocks, blocksOrPixels));

            return transcoder.transcode_image_level(
                levelIndex, layerIndex, 0,
                out.data(), blocksOrPixels,
                format, 0);
        }

        bool TranscoderSession::transcodeAllLevels(basisu::vector<uint8_t> &out, basisu::vector<uint32_t> &outLevelOffsets,
                                                    uint32_t layerIndex, transcoder_texture_format format) {
            if (!initialized) {
                return false;
            }

            // This value is hardcoded for now as cube-textures aren't support ATM.
            int faceIndex = 0;
            uint32_t totalLevels = transcoder.get_levels();

            // First pass: compute every level's output size (cheap, header-only) so we can allocate
            // "out" once for the whole mip chain instead of once per level.
            basisu::vector<uint32_t> levelBlocksOrPixels(totalLevels);
            outLevelOffsets.resize(totalLevels + 1);
            uint32_t totalSize = 0;
            for (uint32_t level = 0; level < totalLevels; level++) {
                ktx2_image_level_info levelInfo = {};
                if (!transcoder.get_image_level_info(levelInfo, level, layerIndex, faceIndex)) {
                    basisuUtils::logError(LOG_TAG, "Failed to read image level info from KTX2 data.");
                    return false;
                }
                outLevelOffsets[level] = totalSize;
                totalSize += computeTranscodedLevelSize(format, levelInfo.m_orig_width, levelInfo.m_orig_height, levelInfo.m_total_blocks, levelBlocksOrPixels[level]);
            }
            outLevelOffsets[totalLevels] = totalSize;

            out.resize(totalSize);

            if (!transcodingStarted) {
                if (!transcoder.start_transcoding()) {
                    basisuUtils::logError(LOG_TAG, "Failed to init transcoding for KTX2 data.");
                    return false;
                }
                transcodingStarted = true;
            }

            for (uint32_t level = 0; level < totalLevels; level++) {
                uint8_t *levelOut = out.data() + outLevelOffsets[level];
                if (!transcoder.transcode_image_level(level, layerIndex, 0, levelOut, levelBlocksOrPixels[level], format, 0)) {
                    return false;
                }
            }

            return true;
        }

    } // namespace ktx2

} // namespace basisuWrapper
