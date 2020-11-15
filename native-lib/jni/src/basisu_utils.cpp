#include <iostream>
#include <cstdio>

#include "basisu_utils.h"
#include "basisu_transcoder.h"

using namespace std;
using namespace basist;

namespace basisuUtils {

    static etc1_global_selector_codebook codebook;

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized) return;

        cout << LOG_TAG << "initializing global basisu parser." << endl;
        
        basisuInitialized = true;

        basisu_transcoder_init();
        
        codebook.init(g_global_selector_cb_size, g_global_selector_cb);
    }

    //TODO Simplify it.
    bool validateHeader(uint8_t* data, size_t size) {
        initBasisu();

        // basisu_transcoder_init();
        // etc1_global_selector_codebook codebook(g_global_selector_cb_size, g_global_selector_cb);

        basisu_transcoder transcoder(&codebook);

        if (transcoder.validate_header(data, size)) {
            cout << LOG_TAG << "Header is valid.\n";

            basisu_file_info fileInfo;
            if (transcoder.get_file_info(data, size, fileInfo)) {
                cout << LOG_TAG << "File info retrieved successfully.\n";

                basisu_image_info imageInfo;
                if (transcoder.get_image_info(data, size, imageInfo, 0))
                cout << LOG_TAG << "Image info retrieved successfully.\n";

                cout << LOG_TAG << "Width: " << imageInfo.m_width << endl;
                cout << LOG_TAG << "Height: " << imageInfo.m_height << endl;
                cout << LOG_TAG << "Alpha: " << imageInfo.m_alpha_flag << endl;

                return true;
            }
        }

        return false;
    }

    // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
    bool transcode(std::vector<uint8_t> &out, uint8_t* basisuData, size_t basisuDataSize,
                   uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format) {
        initBasisu();

        basisu_transcoder transcoder(&codebook);

        const int formatOrdinal = static_cast<int>(format);
        if (formatOrdinal >= (int)transcoder_texture_format::cTFTotalTextureFormats) {
            return false;
        }

        uint32_t origWidth, origHeight, totalBlocks;
        if (!transcoder.get_image_level_desc(basisuData, basisuDataSize, 0, 0, origWidth, origHeight, totalBlocks)) {
            return false;
        }

        // uint32_t flags = get_alpha_for_opaque_formats ? cDecodeFlagsTranscodeAlphaDataToOpaqueFormats : 0;
        uint32_t flags = 0;

        bool status;

        if (!transcoder.start_transcoding(basisuData, basisuDataSize)) {
            return 0;
        }

        if (basis_transcoder_format_is_uncompressed(format)) {

            const uint32_t bytesPerPixel = basis_get_uncompressed_bytes_per_pixel(format);
            const uint32_t bytesPerLine = origWidth * bytesPerPixel;
            const uint32_t bytesPerSlice = bytesPerLine * origHeight;

            cout << LOG_TAG << "Uncompressed conversion bytes per pixel: " << bytesPerPixel << endl;

            out.resize(bytesPerSlice);

            status = transcoder.transcode_image_level(
                basisuData, basisuDataSize, imageIndex, levelIndex,
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
                basisuData, basisuDataSize, imageIndex, levelIndex,
                out.data(), out.size() / bytesPerBlock,
                static_cast<basist::transcoder_texture_format>(format),
                flags);
        }

        transcoder.stop_transcoding();

        cout << LOG_TAG << "Successfully transcoded!" << endl;

        return status;
    }
}