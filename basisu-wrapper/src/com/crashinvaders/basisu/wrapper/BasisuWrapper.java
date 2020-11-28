package com.crashinvaders.basisu.wrapper;

public class BasisuWrapper {

    public static boolean validateHeader(byte[] data) {
        return validateHeaderNative(data, data.length);
    }

    public static boolean validateChecksum(byte[] data, boolean fullValidation) {
        return validateChecksumNative(data, data.length, fullValidation);
    }

    public static int getTotalMipMapLevels(byte[] data) {
        return getTotalMipMapLevelsNative(data, data.length);
    }

    public static BasisuFileInfo getFileInfo(byte[] data) {
        BasisuFileInfo fileInfo = new BasisuFileInfo();
        getFileInfoNative(data, data.length, fileInfo.addr);
        return fileInfo;
    }

    public static BasisuImageInfo getImageInfo(byte[] data, int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        getImageInfoNative(data, data.length, imageIndex, imageInfo.addr);
        return imageInfo;
    }

    public static byte[] transcode(byte[] data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        return transcodeNative(data, data.length, imageIndex, levelIndex, format);
    }

    /*JNI

    #include "jni_utils.h"
    #include "basisu_transcoder.h"
    #include "basisu_wrapper.h"
    #include "basisu_utils.h"

    #define LOG_TAG "BasisuWrapper.java"
    #define BASE_PACKAGE com/crashinvaders/basisu/wrapper

    */

    private static native boolean validateHeaderNative(byte[] data, int dataSize); /*
        return basisuWrapper::validateHeader((uint8_t*)data, dataSize);
    */

    private static native boolean validateChecksumNative(byte[] data, int dataSize, boolean fullValidation); /*
        return basisuWrapper::validateChecksum((uint8_t*)data, dataSize, fullValidation);
    */

    private static native int getTotalMipMapLevelsNative(byte[] data, int dataSize); /*
        return basisuWrapper::getTotalMipMapLevels((uint8_t*)data, dataSize);
    */

    private static native void getFileInfoNative(byte[] data, int dataSize, long fileInfoAddr); /*
        basist::basisu_file_info* fileInfo = (basist::basisu_file_info*)fileInfoAddr;
        if (!basisuWrapper::getFileInfo(*fileInfo, (uint8_t*)data, dataSize)) {
            jniUtils::throwException(env, "Failed to obtain file info.");
            return;
        }
    */

    private static native void getImageInfoNative(byte[] data, int dataSize, int imageIndex, long imageInfoAddr); /*
        basist::basisu_image_info* imageInfo = (basist::basisu_image_info*)imageInfoAddr;
        if (!basisuWrapper::getImageInfo(*imageInfo, (uint8_t*)data, dataSize, imageIndex)) {
            jniUtils::throwException(env, "Failed to obtain image info.");
            return;
        }
    */

    private static native byte[] transcodeNative(byte[] obj_data, int dataSize, int imageIndex, int levelIndex, int textureFormatId); /*MANUAL
        std::vector<uint8_t> rgba;
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);

        char* data = (char*)env->GetPrimitiveArrayCritical(obj_data, 0);

        if (!basisuWrapper::transcode(rgba, (uint8_t*)data, dataSize, imageIndex, levelIndex, format)) {
            basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
            jniUtils::throwException(env, "Error during image transcoding!");
            return 0;
        }

        env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

        jbyteArray byteArray = env->NewByteArray(rgba.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)rgba.size(), (jbyte*)rgba.data());
        return byteArray;
    */

}
