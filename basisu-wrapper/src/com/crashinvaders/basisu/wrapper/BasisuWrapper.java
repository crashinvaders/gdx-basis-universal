package com.crashinvaders.basisu.wrapper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The wrapper over the native Basis Universal transcoder functionality.
 */
public class BasisuWrapper {

    /*JNI

    #include <cstring>

    #include "basisu_transcoder.h"
    #include "basisu_wrapper.h"
    #include "basisu_native_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu/wrapper

    jobject wrapIntoBuffer(JNIEnv* env, basisu::vector<uint8_t> imageData) {
        uint32_t imageDataSize = imageData.size_in_bytes();
        uint8_t* nativeBuffer = (uint8_t*)malloc(imageDataSize);
        memcpy(nativeBuffer, imageData.data(), imageDataSize);
        return env->NewDirectByteBuffer(nativeBuffer, imageDataSize);
    }

    */

    /**
     * Checks weather the transcoder can transcode to the specified texture format.
     * Some transcoding table are disabled per platform to save up space, so you always
     * should check if the format is supported before transcoding to it.
     * <p/>
     * NOTE: Use {@link BasisuTranscoderTextureFormatSupportIndex#isTextureFormatSupported(BasisuTranscoderTextureFormat, BasisuTextureFormat)}
     * instead for frequent checks as calls to this method are relatively slow.
     * @param transcoderTexFormat the format to check support for
     * @param basisTexFormat the intermediate Basis texture format you want to transcode from
     * @return weather the transcoding is supported for the specified formats
     */
    public static boolean isTranscoderTexFormatSupported(BasisuTranscoderTextureFormat transcoderTexFormat, BasisuTextureFormat basisTexFormat) {
        return isTranscoderTexFormatSupportedNative(transcoderTexFormat.getId(), basisTexFormat.getId());
    }
    private native static boolean isTranscoderTexFormatSupportedNative(int transcoderTexFormatId, int basisTexFormatId); /*
        basist::transcoder_texture_format transcoderTexFormat = static_cast<basist::transcoder_texture_format>(transcoderTexFormatId);
        basist::basis_tex_format basisTexFormat = static_cast<basist::basis_tex_format>(basisTexFormatId);
        return basisuWrapper::isTranscoderTexFormatSupported(transcoderTexFormat, basisTexFormat);
    */

    /**
     * Quick header validation - no crc16 checks.
     */
    public static native boolean basisValidateHeader(Buffer dataBuffer); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::validateHeader((uint8_t*)data, dataSize);
    */

    /**
     * Validates the .basis file. This computes a crc16 over the entire file, so it's slow.
     */
    public static native boolean basisValidateChecksum(Buffer dataBuffer, boolean fullValidation); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        uint32_t dataSize = (uint32_t)env->GetDirectBufferCapacity(dataBuffer);
        return basisuWrapper::basis::validateChecksum(data, dataSize, fullValidation);
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static ByteBuffer basisTranscode(Buffer dataBuffer, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        return basisTranscodeNative(dataBuffer, dataBuffer.capacity(), imageIndex, levelIndex, format);
    }
    private static native ByteBuffer basisTranscodeNative(Buffer dataBuffer, int dataSize, int imageIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::basis::transcode(transcodedData, data, dataSize, imageIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during Basis image transcoding.");
            return 0;
        };

        return wrapIntoBuffer(env, transcodedData);
    */

    /**
     * @return a description of the basis file and low-level information about each slice.
     */
    public static BasisuFileInfo basisGetFileInfo(Buffer dataBuffer) {
        int[] values = new int[BasisuFileInfo.FIELD_COUNT];
        int[] imageMipmapLevels = basisGetFileInfoNative(dataBuffer, dataBuffer.capacity(), values);
        return new BasisuFileInfo(values, imageMipmapLevels);
    }
    // Returns the variable-length mipmap level count array as the return value, while the fixed
    // set of scalar fields is packed into "outValues" - one native call populates the whole object.
    private static native int[] basisGetFileInfoNative(Buffer dataBuffer, int dataSize, int[] outValues); /*
        basist::basisu_file_info fileInfo;
        if (!basisuWrapper::basis::getFileInfo(fileInfo, (uint8_t*)dataBuffer, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain Basis file info.");
            return NULL;
        }

        outValues[0] = (jint)fileInfo.m_version;
        outValues[1] = (jint)fileInfo.m_total_header_size;
        outValues[2] = (jint)fileInfo.m_total_selectors;
        outValues[3] = (jint)fileInfo.m_selector_codebook_size;
        outValues[4] = (jint)fileInfo.m_total_endpoints;
        outValues[5] = (jint)fileInfo.m_endpoint_codebook_size;
        outValues[6] = (jint)fileInfo.m_tables_size;
        outValues[7] = (jint)fileInfo.m_slices_size;
        outValues[8] = (jint)fileInfo.m_us_per_frame;
        outValues[9] = (jint)fileInfo.m_total_images;
        outValues[10] = (jint)fileInfo.m_userdata0;
        outValues[11] = (jint)fileInfo.m_userdata1;
        outValues[12] = fileInfo.m_y_flipped ? 1 : 0;
        outValues[13] = fileInfo.m_etc1s ? 1 : 0;
        outValues[14] = fileInfo.m_has_alpha_slices ? 1 : 0;
        outValues[15] = (jint)fileInfo.m_tex_type;
        outValues[16] = (jint)fileInfo.m_tex_format;

        basisu::vector<uint32_t>& levels = fileInfo.m_image_mipmap_levels;
        jintArray result = env->NewIntArray((jsize)levels.size());
        env->SetIntArrayRegion(result, 0, (jsize)levels.size(), (jint*)levels.data());
        return result;
    */

    /**
     * @return information about the specified image.
     */
    public static BasisuImageInfo basisGetImageInfo(Buffer dataBuffer, int imageIndex) {
        int[] values = new int[BasisuImageInfo.FIELD_COUNT];
        basisGetImageInfoNative(dataBuffer, dataBuffer.capacity(), imageIndex, values);
        return new BasisuImageInfo(values);
    }
    private static native void basisGetImageInfoNative(Buffer dataBuffer, int dataSize, int imageIndex, int[] outValues); /*
        basist::basisu_image_info imageInfo;
        if (!basisuWrapper::basis::getImageInfo(imageInfo, (uint8_t*)dataBuffer, dataSize, imageIndex)) {
            basisuUtils::throwException(env, "Failed to obtain Basis image info.");
            return;
        }

        outValues[0] = (jint)imageInfo.m_image_index;
        outValues[1] = (jint)imageInfo.m_total_levels;
        outValues[2] = (jint)imageInfo.m_orig_width;
        outValues[3] = (jint)imageInfo.m_orig_height;
        outValues[4] = (jint)imageInfo.m_width;
        outValues[5] = (jint)imageInfo.m_height;
        outValues[6] = (jint)imageInfo.m_num_blocks_x;
        outValues[7] = (jint)imageInfo.m_num_blocks_y;
        outValues[8] = (jint)imageInfo.m_total_blocks;
        outValues[9] = (jint)imageInfo.m_first_slice_index;
        outValues[10] = imageInfo.m_alpha_flag ? 1 : 0;
        outValues[11] = imageInfo.m_iframe_flag ? 1 : 0;
    */

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer dataBuffer, int imageIndex, int imageLevel) {
        int[] values = new int[BasisuImageLevelInfo.FIELD_COUNT];
        basisGetImageLevelInfoNative(dataBuffer, dataBuffer.capacity(), imageIndex, imageLevel, values);
        return new BasisuImageLevelInfo(values);
    }
    private static native void basisGetImageLevelInfoNative(Buffer dataBuffer, int dataSize, int imageIndex, int imageLevel, int[] outValues); /*
        basist::basisu_image_level_info levelInfo;
        if (!basisuWrapper::basis::getImageLevelInfo(levelInfo, (uint8_t*)dataBuffer, dataSize, imageIndex, imageLevel)) {
            basisuUtils::throwException(env, "Failed to obtain Basis image level info.");
            return;
        }

        outValues[0] = (jint)levelInfo.m_image_index;
        outValues[1] = (jint)levelInfo.m_level_index;
        outValues[2] = (jint)levelInfo.m_orig_width;
        outValues[3] = (jint)levelInfo.m_orig_height;
        outValues[4] = (jint)levelInfo.m_width;
        outValues[5] = (jint)levelInfo.m_height;
        outValues[6] = (jint)levelInfo.m_num_blocks_x;
        outValues[7] = (jint)levelInfo.m_num_blocks_y;
        outValues[8] = (jint)levelInfo.m_total_blocks;
        outValues[9] = (jint)levelInfo.m_first_slice_index;
        outValues[10] = levelInfo.m_alpha_flag ? 1 : 0;
        outValues[11] = levelInfo.m_iframe_flag ? 1 : 0;
    */

    /** @return information about the KTX2 file. */
    public static Ktx2FileInfo ktx2GetFileInfo(Buffer dataBuffer) {
        int[] values = new int[Ktx2FileInfo.FIELD_COUNT];
        ktx2GetFileInfoNative(dataBuffer, dataBuffer.capacity(), values);
        return new Ktx2FileInfo(values);
    }
    private static native void ktx2GetFileInfoNative(Buffer dataBuffer, int dataSize, int[] outValues); /*
        basisuWrapper::ktx2_file_info fileInfo;
        if (!basisuWrapper::ktx2::getFileInfo(fileInfo, (uint8_t*)dataBuffer, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 file info.");
            return;
        }

        outValues[0] = (jint)fileInfo.layers;
        outValues[1] = (jint)fileInfo.mipmapLevels;
        outValues[2] = (jint)fileInfo.width;
        outValues[3] = (jint)fileInfo.height;
        outValues[4] = fileInfo.hasAlpha ? 1 : 0;
        outValues[5] = (jint)fileInfo.textureFormat;
    */

    /** @return information about the specified image level. */
    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer dataBuffer, int imageIndex, int imageLevel) {
        int[] values = new int[Ktx2ImageLevelInfo.FIELD_COUNT];
        ktx2GetImageLevelInfoNative(dataBuffer, dataBuffer.capacity(), imageIndex, imageLevel, values);
        return new Ktx2ImageLevelInfo(values);
    }
    private static native void ktx2GetImageLevelInfoNative(Buffer dataBuffer, int dataSize, int imageIndex, int imageLevel, int[] outValues); /*
        basist::ktx2_image_level_info levelInfo;
        if (!basisuWrapper::ktx2::getImageLevelInfo(levelInfo, (uint8_t*)dataBuffer, dataSize, imageIndex, imageLevel)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 image level info.");
            return;
        }

        outValues[0] = (jint)levelInfo.m_level_index;
        outValues[1] = (jint)levelInfo.m_layer_index;
        outValues[2] = (jint)levelInfo.m_face_index;
        outValues[3] = (jint)levelInfo.m_orig_width;
        outValues[4] = (jint)levelInfo.m_orig_height;
        outValues[5] = (jint)levelInfo.m_width;
        outValues[6] = (jint)levelInfo.m_height;
        outValues[7] = (jint)levelInfo.m_num_blocks_x;
        outValues[8] = (jint)levelInfo.m_num_blocks_y;
        outValues[9] = (jint)levelInfo.m_total_blocks;
        outValues[10] = levelInfo.m_alpha_flag ? 1 : 0;
        outValues[11] = levelInfo.m_iframe_flag ? 1 : 0;
    */

    public static ByteBuffer ktx2Transcode(Buffer dataBuffer, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        return ktx2TranscodeNative(dataBuffer, dataBuffer.capacity(), layerIndex, levelIndex, format);
    }
    private static native ByteBuffer ktx2TranscodeNative(Buffer dataBuffer, int dataSize, int layerIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::ktx2::transcode(transcodedData, data, dataSize, layerIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during KTX2 image transcoding.");
            return 0;
        };

        return wrapIntoBuffer(env, transcodedData);
    */

    /**
     * A {@link ByteBuffer} returned from any of {@link BasisuWrapper}
     * methods must be disposed using this method only.
     */
    public static native void disposeNativeBuffer(ByteBuffer dataBuffer); /*
        free(dataBuffer);
    */

    /**
     * Releases any native-side state cached for the given encoded data buffer.
     * On JNI platforms calls operate directly on the buffer's pointer and keep no persistent
     * native state, so this is a no-op here (the GWT/Emscripten backend overrides this to free
     * its cached Wasm-side copy of the buffer).
     */
    public static void releaseEncodedData(Buffer dataBuffer) {
        // No-op on JNI platforms.
    }
}
