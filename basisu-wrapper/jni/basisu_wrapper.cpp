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

            //TODO Do we need to call this?
            transcoder.clear();

            return status;
        }

    } // namespace ktx2

} // namespace basisuWrapper


// Emscripten Embind bindings.
#ifdef __EMSCRIPTEN__

#include <iostream>
#include <emscripten/bind.h>
#include <emscripten/val.h>

using namespace emscripten;

//FIXME Array double copying (Java->JS->C++) is really ugly. Find a way to pass data arrays more easily. Look into GWT types that libGDX uses.

// From https://github.com/emscripten-core/emscripten/issues/5519#issuecomment-624775352
basisu::vector<uint8_t> vecFromTypedArray(const val &jsValue) {
    const unsigned length = jsValue["length"].as<unsigned>();
    basisu::vector<uint8_t> vec(length);
    val memoryView{typed_memory_view(length, vec.data())};
    memoryView.call<void>("set", jsValue);
    return vec;
}

val vecToTypedArray(basisu::vector<uint8_t> &vec) {
    val jsValue = val::global("Uint8Array").new_(vec.size());
    val memoryView{typed_memory_view(vec.size(), vec.data())};
    jsValue.call<void>("set", memoryView);
    return jsValue;
}

int main(int, char**) {
    basisuUtils::logInfo(LOG_TAG, "libGDX Basis Universal native library is ready.");
    return 0;
}

bool isTranscoderTexFormatSupported_wrap(uint32_t transcoderTexFormatId, uint32_t basisTexFormatId) {
    basist::transcoder_texture_format transcoderTexFormat = static_cast<basist::transcoder_texture_format>(transcoderTexFormatId);
    basist::basis_tex_format basisTexFormat = static_cast<basist::basis_tex_format>(basisTexFormatId);

    return basisuWrapper::isTranscoderTexFormatSupported(transcoderTexFormat, basisTexFormat);
}

bool basisValidateHeader_wrap(const val &jsData) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    return basisuWrapper::basis::validateHeader(data.data(), data.size());
}

bool basisValidateChecksum_wrap(const val &jsData, bool fullValidation) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    return basisuWrapper::basis::validateChecksum(data.data(), data.size(), fullValidation);
}

basist::basisu_file_info basisGetFileInfo_wrap(const val &jsData) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basist::basisu_file_info fileInfo;
    if (!basisuWrapper::basis::getFileInfo(fileInfo, data.data(), data.size())) {
        basisuUtils::throwException(nullptr, "Failed to obtain Basis file info.");
    }
    return fileInfo;
}

basist::basisu_image_info basisGetImageInfo_wrap(const val &jsData, uint32_t imageIndex) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basist::basisu_image_info imageInfo;
    if (!basisuWrapper::basis::getImageInfo(imageInfo, data.data(), data.size(), imageIndex)) {
        basisuUtils::throwException(nullptr, "Failed to obtain Basis image info.");
    }
    return imageInfo;
}

val basisTranscode_wrap(const val &jsData, uint32_t imageIndex, uint32_t levelIndex, uint32_t textureFormatId) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basisu::vector<uint8_t> output;
    basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);

    if (!basisuWrapper::basis::transcode(output, data.data(), data.size(), imageIndex, levelIndex, format)) {
        basisuUtils::logError(LOG_TAG, "Error during Basis image transcoding!");
        basisuUtils::throwException(nullptr, "Error during basis image transcoding!");
    }

    return vecToTypedArray(output);
}

basisuWrapper::ktx2_file_info ktx2GetFileInfo_wrap(const val &jsData) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basisuWrapper::ktx2_file_info fileInfo;
    if (!basisuWrapper::ktx2::getFileInfo(fileInfo, data.data(), data.size())) {
        basisuUtils::throwException(nullptr, "Failed to obtain KTX2 file info.");
    }
    return fileInfo;
}

basist::ktx2_image_level_info ktx2GetImageLevelInfo_wrap(const val &jsData, uint32_t layerIndex, uint32_t levelIndex) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basist::ktx2_image_level_info imageInfo;
    if (!basisuWrapper::ktx2::getImageLevelInfo(imageInfo, data.data(), data.size(), layerIndex, levelIndex)) {
        basisuUtils::throwException(nullptr, "Failed to obtain KTX2 image level info.");
    }
    return imageInfo;
}

val ktx2Transcode_wrap(const val &jsData, uint32_t layerIndex, uint32_t levelIndex, uint32_t textureFormatId) {
    basisu::vector<uint8_t> data = vecFromTypedArray(jsData);
    basisu::vector<uint8_t> output;
    basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);

    if (!basisuWrapper::ktx2::transcode(output, data.data(), data.size(), layerIndex, levelIndex, format)) {
        basisuUtils::logError(LOG_TAG, "Error during KTX2 image transcoding!");
        basisuUtils::throwException(nullptr, "Error during KTX2 image transcoding!");
    }

    return vecToTypedArray(output);
}

//uint8_t basisFileInfo_texFormat(basist::basisu_file_info &fileInfo) {
//    return (uint8_t)fileInfo.m_tex_format;
//}

val basisFileInfo_imageMipmapLevels(basist::basisu_file_info &fileInfo) {
    basisu::vector<uint32_t> vec32 = fileInfo.m_image_mipmap_levels;
    basisu::vector<uint8_t> vec8(vec32.size());
    for (int i = 0; i < vec32.size(); i++) {
        vec8[i] = (uint8_t)vec32[i];
    }
    return vecToTypedArray(vec8);
}

EMSCRIPTEN_BINDINGS(my_module) {

	enum_<basist::basis_texture_type>("TextureType")
	    .value("TexType2D", basist::basis_texture_type::cBASISTexType2D)
	    .value("TexType2DArray", basist::basis_texture_type::cBASISTexType2DArray)
	    .value("TexTypeCubemapArray", basist::basis_texture_type::cBASISTexTypeCubemapArray)
	    .value("TexTypeVideoFrames", basist::basis_texture_type::cBASISTexTypeVideoFrames)
	    .value("TexTypeVolume", basist::basis_texture_type::cBASISTexTypeVolume)
	    ;

	enum_<basist::basis_tex_format>("TextureFormat")
	    .value("ETC1S", basist::basis_tex_format::cETC1S)
	    .value("UASTC", basist::basis_tex_format::cUASTC4x4)
	    ;

    value_object<basist::basisu_image_info>("ImageInfo")
        .field("imageIndex", &basist::basisu_image_info::m_image_index)             // uint32_t
        .field("totalLevels", &basist::basisu_image_info::m_total_levels)           // uint32_t
        .field("origWidth", &basist::basisu_image_info::m_orig_width)               // uint32_t
        .field("origHeight", &basist::basisu_image_info::m_orig_height)             // uint32_t
        .field("width", &basist::basisu_image_info::m_width)                        // uint32_t
        .field("height", &basist::basisu_image_info::m_height)                      // uint32_t
        .field("numBlocksX", &basist::basisu_image_info::m_num_blocks_x)            // uint32_t
        .field("numBlocksY", &basist::basisu_image_info::m_num_blocks_y)            // uint32_t
        .field("totalBlocks", &basist::basisu_image_info::m_total_blocks)           // uint32_t
        .field("firstSliceIndex", &basist::basisu_image_info::m_first_slice_index)  // uint32_t
        .field("alphaFlag", &basist::basisu_image_info::m_alpha_flag)               // bool
        .field("iframeFlag", &basist::basisu_image_info::m_iframe_flag)             // bool
        ;

    // Use "class_" mapping instead of "value_object" to define custom enum getter functions.
    class_<basist::basisu_file_info>("FileInfo")
		.property("version", &basist::basisu_file_info::m_version)                              // uint32_t
		.property("totalHeaderSize", &basist::basisu_file_info::m_total_header_size)            // uint32_t
		.property("totalSelectors", &basist::basisu_file_info::m_total_selectors)               // uint32_t
		.property("selectorCodebookSize", &basist::basisu_file_info::m_selector_codebook_size)  // uint32_t
		.property("totalEndpoints", &basist::basisu_file_info::m_total_endpoints)               // uint32_t
		.property("endpointCodebookSize", &basist::basisu_file_info::m_endpoint_codebook_size)  // uint32_t
		.property("tablesSize", &basist::basisu_file_info::m_tables_size)                       // uint32_t
		.property("slicesSize", &basist::basisu_file_info::m_slices_size)                       // uint32_t
		.property("usPerFrame", &basist::basisu_file_info::m_us_per_frame)                      // uint32_t
		.property("totalImages", &basist::basisu_file_info::m_total_images)                     // uint32_t
		.property("userdata0", &basist::basisu_file_info::m_userdata0)                          // uint32_t
		.property("userdata1", &basist::basisu_file_info::m_userdata1)                          // uint32_t
		.property("yFlipped", &basist::basisu_file_info::m_y_flipped)                           // bool
		.property("etc1s", &basist::basisu_file_info::m_etc1s)                                  // bool
		.property("hasAlphaSlices", &basist::basisu_file_info::m_has_alpha_slices)              // bool
		.property("textureFormat", &basist::basisu_file_info::m_tex_format)                     // TextureFormat
        .property("textureType", &basist::basisu_file_info::m_tex_type)                         // TextureType
		// Properties simulated through functions (to return more JS friendly type).
		.function("getImageMipmapLevels", &basisFileInfo_imageMipmapLevels)                     // val (Uint8Array)
//		.function("getTexFormat", &fileInfo_texFormat)                                          // uint8_t
		;

    value_object<basisuWrapper::ktx2_file_info>("Ktx2FileInfo")
        .field("layers", &basisuWrapper::ktx2_file_info::layers)                // uint32_t
        .field("mipmapLevels", &basisuWrapper::ktx2_file_info::mipmapLevels)    // uint32_t
        .field("width", &basisuWrapper::ktx2_file_info::width)                  // uint32_t
        .field("height", &basisuWrapper::ktx2_file_info::height)                // uint32_t
        .field("hasAlpha", &basisuWrapper::ktx2_file_info::hasAlpha)            // bool
        .field("textureFormat", &basisuWrapper::ktx2_file_info::textureFormat)  // TextureFormat
        ;

    value_object<basist::ktx2_image_level_info>("Ktx2ImageLayerInfo")
        .field("levelIndex", &basist::ktx2_image_level_info::m_level_index)     // uint32_t
        .field("layerIndex", &basist::ktx2_image_level_info::m_layer_index)     // uint32_t
        .field("faceIndex", &basist::ktx2_image_level_info::m_face_index)       // uint32_t
        .field("origWidth", &basist::ktx2_image_level_info::m_orig_width)       // uint32_t
        .field("origHeight", &basist::ktx2_image_level_info::m_orig_height)     // uint32_t
        .field("width", &basist::ktx2_image_level_info::m_width)                // uint32_t
        .field("height", &basist::ktx2_image_level_info::m_height)              // uint32_t
        .field("numBlocksX", &basist::ktx2_image_level_info::m_num_blocks_x)    // uint32_t
        .field("numBlocksY", &basist::ktx2_image_level_info::m_num_blocks_y)    // uint32_t
        .field("totalBlocks", &basist::ktx2_image_level_info::m_total_blocks)   // uint32_t
        .field("alphaFlag", &basist::ktx2_image_level_info::m_alpha_flag)       // bool
        .field("iframeFlag", &basist::ktx2_image_level_info::m_iframe_flag)     // bool
        ;

    function("isTranscoderTexFormatSupported", &isTranscoderTexFormatSupported_wrap);
    function("basisValidateHeader", &basisValidateHeader_wrap);
    function("basisValidateChecksum", &basisValidateChecksum_wrap);
    function("basisGetFileInfo", &basisGetFileInfo_wrap);
    function("basisGetImageInfo", &basisGetImageInfo_wrap);
    function("basisTranscode", &basisTranscode_wrap);
    function("ktx2GetFileInfo", &ktx2GetFileInfo_wrap);
    function("ktx2GetImageLevelInfo", &ktx2GetImageLevelInfo_wrap);
    function("ktx2Transcode", &ktx2Transcode_wrap);
}

#endif // __EMSCRIPTEN__
