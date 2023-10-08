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
    public static boolean basisValidateHeader(Buffer data) {
        return basisValidateHeaderNative(data, data.capacity());
    }
    private static native boolean basisValidateHeaderNative(Buffer data, int dataSize); /*
        return basisuWrapper::basis::validateHeader((uint8_t*)data, dataSize);
    */

    /**
     * Validates the .basis file. This computes a crc16 over the entire file, so it's slow.
     */
    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        return basisValidateChecksumNative(data, data.capacity(), fullValidation);
    }
    private static native boolean basisValidateChecksumNative(Buffer data, int dataSize, boolean fullValidation); /*
        return basisuWrapper::basis::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public static ByteBuffer basisTranscode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        byte[] transcodedData = basisTranscodeNative(data, data.capacity(), imageIndex, levelIndex, format);

        // Seems like allocating and filling a DirectByteBuffer
        // is faster on Java side rather than on the native one
        // (Even with receiving extra Java primitive array from the native code).
        ByteBuffer buffer = ByteBuffer.allocateDirect(transcodedData.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(transcodedData);
        ((Buffer)buffer).position(0);
        ((Buffer)buffer).limit(buffer.capacity());
        return buffer;
    }
    private static native byte[] basisTranscodeNative(Buffer dataRaw, int dataSize, int imageIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::basis::transcode(transcodedData, data, dataSize, imageIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during Basis image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    */

    /**
     * @return a description of the basis file and low-level information about each slice.
     */
    public static BasisuFileInfo basisGetFileInfo(Buffer data) {
        BasisuFileInfo fileInfo = new BasisuFileInfo();
        basisGetFileInfoNative(data, data.capacity(), fileInfo.addr);
        return fileInfo;
    }
    private static native void basisGetFileInfoNative(Buffer data, int dataSize, long fileInfoAddr); /*
        basist::basisu_file_info* fileInfo = (basist::basisu_file_info*)fileInfoAddr;
        if (!basisuWrapper::basis::getFileInfo(*fileInfo, (uint8_t*)data, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain Basis file info.");
        }
    */

    /**
     * @return information about the specified image.
     */
    public static BasisuImageInfo basisGetImageInfo(Buffer data, int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        basisGetImageInfoNative(data, data.capacity(), imageIndex, imageInfo.addr);
        return imageInfo;
    }
    private static native void basisGetImageInfoNative(Buffer data, int dataSize, int imageIndex, long imageInfoAddr); /*
        basist::basisu_image_info* imageInfo = (basist::basisu_image_info*)imageInfoAddr;
        if (!basisuWrapper::basis::getImageInfo(*imageInfo, (uint8_t*)data, dataSize, imageIndex)) {
            basisuUtils::throwException(env, "Failed to obtain Basis image info.");
        }
    */

    /** @return information about the KTX2 file. */
    public static Ktx2FileInfo ktx2GetFileInfo(Buffer data) {
        Ktx2FileInfo fileInfo = new Ktx2FileInfo();
        ktx2GetFileInfoNative(data, data.capacity(), fileInfo.addr);
        return fileInfo;
    }
    private static native void ktx2GetFileInfoNative(Buffer data, int dataSize, long fileInfoAddr); /*
        basisuWrapper::ktx2_file_info* fileInfo = (basisuWrapper::ktx2_file_info*)fileInfoAddr;
        if (!basisuWrapper::ktx2::getFileInfo(*fileInfo, (uint8_t*)data, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 file info.");
        }
    */

    /** @return information about the specified image level. */
    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer data, int imageIndex, int imageLevel) {
        Ktx2ImageLevelInfo imageInfo = new Ktx2ImageLevelInfo();
        ktx2GetImageLevelInfoNative(data, data.capacity(), imageIndex, imageLevel, imageInfo.addr);
        return imageInfo;
    }
    private static native void ktx2GetImageLevelInfoNative(Buffer data, int dataSize, int imageIndex, int imageLevel, long imageInfoAddr); /*
        basist::ktx2_image_level_info* imageInfo = (basist::ktx2_image_level_info*)imageInfoAddr;
        if (!basisuWrapper::ktx2::getImageLevelInfo(*imageInfo, (uint8_t*)data, dataSize, imageIndex, imageLevel)) {
            basisuUtils::throwException(env, "Failed to obtain KTX2 image level info.");
        }
    */

    public static ByteBuffer ktx2Transcode(Buffer data, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        byte[] transcodedData = ktx2TranscodeNative(data, data.capacity(), layerIndex, levelIndex, format);

        // Seems like allocating and filling a DirectByteBuffer
        // is faster on Java side rather than on the native one
        // (Even with receiving extra Java primitive array from the native code).
        ByteBuffer buffer = ByteBuffer.allocateDirect(transcodedData.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.put(transcodedData);
        ((Buffer)buffer).position(0);
        ((Buffer)buffer).limit(buffer.capacity());
        return buffer;
    }
    private static native byte[] ktx2TranscodeNative(Buffer dataRaw, int dataSize, int layerIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        basisu::vector<uint8_t> transcodedData;

        if (!basisuWrapper::ktx2::transcode(transcodedData, data, dataSize, layerIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during KTX2 image transcoding.");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    */
}
