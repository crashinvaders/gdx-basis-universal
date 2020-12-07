#include <cstdio>
#include <cstring>

#include "basisu_wrapper.h"
#include "basisu_transcoder.h"
#include "basisu_native_utils.h"

using namespace basist;

namespace basisuWrapper {

#define LOG_TAG "basisu_wrapper.cpp"

    static etc1_global_selector_codebook codebook;

    void initBasisu() {
        static bool basisuInitialized;
        if (basisuInitialized)
            return;

        basisuUtils::logInfo(LOG_TAG, (std::string("Basis Universal ") + BASISD_VERSION_STRING).c_str());
        basisuUtils::logInfo(LOG_TAG, "Initializing global basisu parser.");

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
            basisuUtils::logInfo(LOG_TAG, "Failed to obtain file info.");
        }
        return successful;
    }

    bool getImageInfo(basisu_image_info &imageInfo, uint8_t *data, uint32_t dataSize, uint32_t imageIndex) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);
        bool successful = transcoder.get_image_info(data, dataSize, imageInfo, imageIndex);
        if (!successful) {
            basisuUtils::logError(LOG_TAG, "Failed to obtain image info.");
        }
        return successful;
    }

    // Based on https://github.com/BinomialLLC/basis_universal/blob/master/webgl/transcoder/basis_wrappers.cpp
    bool transcode(std::vector<uint8_t> &out, uint8_t *data, uint32_t dataSize,
                   uint32_t imageIndex, uint32_t levelIndex, transcoder_texture_format format) {
        initBasisu();
        basisu_transcoder transcoder(&codebook);

        uint32_t origWidth, origHeight, totalBlocks;
        if (!transcoder.get_image_level_desc(data, dataSize, imageIndex, levelIndex, origWidth, origHeight, totalBlocks)) {
            basisuUtils::logError(LOG_TAG, "Failed to retrieve image level description.");
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


// Emscripten Embind bindings.
#ifdef __EMSCRIPTEN__

#include <vector>
#include <iostream>
#include <emscripten/bind.h>
#include <emscripten/val.h>

using namespace emscripten;

//FIXME Array double copying (Java->JS->C++) is really ugly. Find a way to pass data arrays more easily. Look into GWT types that LibGDX uses.

// From https://github.com/emscripten-core/emscripten/issues/5519#issuecomment-624775352
std::vector<uint8_t> vecFromTypedArray(const val &jsValue) {
    const unsigned length = jsValue["length"].as<unsigned>();
    std::vector<uint8_t> vec(length);
    val memoryView{typed_memory_view(length, vec.data())};
    memoryView.call<void>("set", jsValue);
    return vec;
}

val vecToTypedArray(std::vector<uint8_t> &vec) {
    val jsValue = val::global("Uint8Array").new_(vec.size());
    val memoryView{typed_memory_view(vec.size(), vec.data())};
    jsValue.call<void>("set", memoryView);
    return jsValue;
}

int main(int, char**) {
    basisuUtils::logInfo(LOG_TAG, "LibGDX Basis Universal native library is ready.");
    return 0;
}

bool validateHeader_wrap(const val &jsData) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    return basisuWrapper::validateHeader(data.data(), data.size());
}

bool validateChecksum_wrap(const val &jsData, bool fullValidation) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    return basisuWrapper::validateChecksum(data.data(), data.size(), fullValidation);
}

int getTotalMipMapLevels_wrap(const val &jsData) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    return basisuWrapper::getTotalMipMapLevels(data.data(), data.size());
}

// uintptr_t
basist::basisu_file_info getFileInfo_wrap(const val &jsData) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    basist::basisu_file_info fileInfo;
    if (basisuWrapper::getFileInfo(fileInfo, data.data(), data.size())) {
        basisuUtils::throwException(nullptr, "Failed to obtain file info.");
    }
    return fileInfo;
}

// uintptr_t
basist::basisu_image_info getImageInfo_wrap(const val &jsData, uint32_t imageIndex) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    basist::basisu_image_info imageInfo;
    if (basisuWrapper::getImageInfo(imageInfo, data.data(), data.size(), imageIndex)) {
        basisuUtils::throwException(nullptr, "Failed to obtain image info.");
    }
    return imageInfo;
}

val transcode_wrap(const val &jsData, uint32_t imageIndex, uint32_t levelIndex, uint32_t textureFormatId) {
    std::vector<uint8_t> data = vecFromTypedArray(jsData);
    std::vector<uint8_t> rgba;
    basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);

    if (!basisuWrapper::transcode(rgba, data.data(), data.size(), imageIndex, levelIndex, format)) {
        basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
        basisuUtils::throwException(nullptr, "Error during image transcoding!");
    }

    return vecToTypedArray(rgba);
}

uint8_t fileInfo_texType(basist::basisu_file_info &fileInfo) {
    return (uint8_t)fileInfo.m_tex_type;
}

uint8_t fileInfo_texFormat(basist::basisu_file_info &fileInfo) {
    return (uint8_t)fileInfo.m_tex_format;
}

val fileInfo_imageMipmapLevels(basist::basisu_file_info &fileInfo) {
    std::vector<uint32_t> vec32 = fileInfo.m_image_mipmap_levels;
    std::vector<uint8_t> vec8(vec32.size());
    for (int i = 0; i < vec32.size(); i++) {
        vec8[i] = (uint8_t)vec32[i];
    }
    return vecToTypedArray(vec8);
}

EMSCRIPTEN_BINDINGS(my_module) {

//	register_vector<uint8_t>("Uint8Vector");
	register_vector<uint32_t>("Uint32Vector");

	enum_<basist::basis_texture_type>("TextureType")
	    .value("TexType2D", basist::basis_texture_type::cBASISTexType2D)
	    .value("TexType2DArray", basist::basis_texture_type::cBASISTexType2DArray)
	    .value("TexTypeCubemapArray", basist::basis_texture_type::cBASISTexTypeCubemapArray)
	    .value("TexTypeVideoFrames", basist::basis_texture_type::cBASISTexTypeVideoFrames)
	    .value("TexTypeVolume", basist::basis_texture_type::cBASISTexTypeVolume)
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
//		.property("imageMipmapLevels", &basist::basisu_file_info::m_image_mipmap_levels)        // std::vector<uint32_t>
		.function("getImageMipmapLevels", &fileInfo_imageMipmapLevels)                          // val (Uint8Array)
//		.property("texFormat", &basist::basisu_file_info::m_tex_format)                         // basis_tex_format
		.function("getTexFormat", &fileInfo_texFormat)                                          // uint8_t
//		.property("texType", &basist::basisu_file_info::m_tex_type)                             // basis_texture_type
		.function("getTexType", &fileInfo_texType)                                              // uint8_t
		;

    function("validateHeader", &validateHeader_wrap);
    function("validateChecksum", &validateChecksum_wrap);
    function("getTotalMipMapLevels", &getTotalMipMapLevels_wrap);
    function("getFileInfo", &getFileInfo_wrap);
    function("getImageInfo", &getImageInfo_wrap);
    function("transcode", &transcode_wrap);
}

#endif // __EMSCRIPTEN__
