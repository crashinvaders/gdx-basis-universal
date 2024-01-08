package com.crashinvaders.basisu.wrapper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

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
        BasisuFileInfo fileInfo = new BasisuFileInfo();
        basisGetFileInfoNative(dataBuffer, dataBuffer.capacity(), fileInfo.addr);
        return fileInfo;
    }
    private static native void basisGetFileInfoNative(Buffer dataBuffer, int dataSize, long fileInfoAddr); /*
        basist::basisu_file_info* fileInfo = (basist::basisu_file_info*)fileInfoAddr;
        if (!basisuWrapper::basis::getFileInfo(*fileInfo, (uint8_t*)dataBuffer, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain Basis file info.");
        }
    */

    /**
     * @return information about the specified image.
     */
    public static BasisuImageInfo basisGetImageInfo(Buffer dataBuffer, int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        basisGetImageInfoNative(dataBuffer, dataBuffer.capacity(), imageInfo.addr, imageIndex);
        return imageInfo;
    }
    private static native void basisGetImageInfoNative(Buffer dataBuffer, int dataSize, long imageInfoAddr, int imageIndex); /*
        basist::basisu_image_info* imageInfo = (basist::basisu_image_info*)imageInfoAddr;
        if (!basisuWrapper::basis::getImageInfo(*imageInfo, (uint8_t*)dataBuffer, dataSize, imageIndex)) {
            basisuUtils::throwException(env, "Failed to obtain Basis image info.");
        }
    */

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer dataBuffer, int imageIndex, int imageLevel) {
        BasisuImageLevelInfo imageInfo = new BasisuImageLevelInfo();
        basisGetImageLevelInfoNative(dataBuffer, dataBuffer.capacity(), imageInfo.addr, imageIndex, imageLevel);
        return imageInfo;
    }
    private static native void basisGetImageLevelInfoNative(Buffer dataBuffer, int dataSize, long imageInfoAddr, int imageIndex, int imageLevel); /*
        basist::basisu_image_level_info* imageInfo = (basist::basisu_image_level_info*)imageInfoAddr;
        if (!basisuWrapper::basis::getImageLevelInfo(*imageInfo, (uint8_t*)dataBuffer, dataSize, imageIndex, imageLevel)) {
            basisuUtils::throwException(env, "Failed to obtain Basis image level info.");
        }
    */

    /** @return information about the KTX2 file. */
    public static Ktx2FileInfo ktx2GetFileInfo(Buffer dataBuffer) {
        Ktx2FileInfo fileInfo = new Ktx2FileInfo();
        ktx2GetFileInfoNative(dataBuffer, dataBuffer.capacity(), fileInfo.addr);
        return fileInfo;
    }
    private static native void ktx2GetFileInfoNative(Buffer dataBuffer, int dataSize, long fileInfoAddr); /*
        basisuWrapper::ktx2_file_info* fileInfo = (basisuWrapper::ktx2_file_info*)fileInfoAddr;
        if (!basisuWrapper::ktx2::getFileInfo(*fileInfo, (uint8_t*)dataBuffer, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 file info.");
        }
    */

    /** @return information about the specified image level. */
    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer dataBuffer, int imageIndex, int imageLevel) {
        Ktx2ImageLevelInfo imageInfo = new Ktx2ImageLevelInfo();
        ktx2GetImageLevelInfoNative(dataBuffer, dataBuffer.capacity(), imageIndex, imageLevel, imageInfo.addr);
        return imageInfo;
    }
    private static native void ktx2GetImageLevelInfoNative(Buffer dataBuffer, int dataSize, int imageIndex, int imageLevel, long imageInfoAddr); /*
        basist::ktx2_image_level_info* imageInfo = (basist::ktx2_image_level_info*)imageInfoAddr;
        if (!basisuWrapper::ktx2::getImageLevelInfo(*imageInfo, (uint8_t*)dataBuffer, dataSize, imageIndex, imageLevel)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 image level info.");
        }
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
}
