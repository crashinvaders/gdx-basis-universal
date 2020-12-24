package com.crashinvaders.basisu.wrapper;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BasisuWrapper {

    /*JNI

    #include<cstring>

    #include "basisu_transcoder.h"
    #include "basisu_wrapper.h"
    #include "basisu_native_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu/wrapper

    */

    public static boolean validateHeader(Buffer data) {
        return validateHeaderNative(data, data.capacity());
    }
    private static native boolean validateHeaderNative(Buffer data, int dataSize); /*
        return basisuWrapper::validateHeader((uint8_t*)data, dataSize);
    */

    public static boolean validateChecksum(Buffer data, boolean fullValidation) {
        return validateChecksumNative(data, data.capacity(), fullValidation);
    }
    private static native boolean validateChecksumNative(Buffer data, int dataSize, boolean fullValidation); /*
        return basisuWrapper::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    */

    public static int getTotalMipMapLevels(Buffer data) {
        return getTotalMipMapLevelsNative(data, data.capacity());
    }
    private static native int getTotalMipMapLevelsNative(Buffer data, int dataSize); /*
        return basisuWrapper::getTotalMipMapLevels((uint8_t*)data, dataSize);
    */

    public static ByteBuffer transcode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        byte[] transcodedData = transcodeNative(data, data.capacity(), imageIndex, levelIndex, format);

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
    private static native byte[] transcodeNative(Buffer dataRaw, int dataSize, int imageIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataRaw);
        std::vector<uint8_t> transcodedData;

        if (!basisuWrapper::transcode(transcodedData, data, dataSize, imageIndex, levelIndex, format)) {
            basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
            basisuUtils::throwException(env, "Error during image transcoding!");
            return 0;
        };

        jbyteArray byteArray = env->NewByteArray(transcodedData.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)transcodedData.size(), (jbyte*)transcodedData.data());
        return byteArray;
    */

    public static BasisuFileInfo getFileInfo(Buffer data) {
        BasisuFileInfo fileInfo = new BasisuFileInfo();
        getFileInfoNative(data, data.capacity(), fileInfo.addr);
        return fileInfo;
    }
    private static native void getFileInfoNative(Buffer data, int dataSize, long fileInfoAddr); /*
        basist::basisu_file_info* fileInfo = (basist::basisu_file_info*)fileInfoAddr;
        if (!basisuWrapper::getFileInfo(*fileInfo, (uint8_t*)data, dataSize)) {
            basisuUtils::throwException(env, "Failed to obtain file info.");
        }
    */

    public static BasisuImageInfo getImageInfo(Buffer data, int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        getImageInfoNative(data, data.capacity(), imageIndex, imageInfo.addr);
        return imageInfo;
    }
    private static native void getImageInfoNative(Buffer data, int dataSize, int imageIndex, long imageInfoAddr); /*
        basist::basisu_image_info* imageInfo = (basist::basisu_image_info*)imageInfoAddr;
        if (!basisuWrapper::getImageInfo(*imageInfo, (uint8_t*)data, dataSize, imageIndex)) {
            basisuUtils::throwException(env, "Failed to obtain image info.");
        }
    */
}
