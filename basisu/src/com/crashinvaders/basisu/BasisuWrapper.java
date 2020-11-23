package com.crashinvaders.basisu;

public class BasisuWrapper {

    public static boolean validateHeader(byte[] data, int dataSize) {
        return validateHeaderNative(data, dataSize);
    }

    public static boolean validateChecksum(byte[] data, int dataSize, boolean fullValidation) {
        return validateChecksumNative(data, dataSize, fullValidation);
    }

    public static int getTotalMipMapLevels(byte[] data, int dataSize) {
        return getTotalMipMapLevelsNative(data, dataSize);
    }

//    public static BasisuFileInfo getFileInfo(byte[] data, int dataSize) {
//        BasisuFileInfo fileInfo = new BasisuFileInfo();
//        getFileInfoNative(data, dataSize, fileInfo);
//        return fileInfo;
//    }

    public static BasisuImageInfo getImageInfo(byte[] data, int dataSize, int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        getImageInfoNative(data, dataSize, imageIndex, imageInfo);
        return imageInfo;
    }

    public static byte[] transcode(byte[] data, int dataSize, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        int format = textureFormat.getId();
        return transcodeNative(data, dataSize, levelIndex, format);
    }

    /*JNI

    #include <iostream>
    #include <jni.h>

    #include "jni_utils.h"
    #include "basisu_transcoder.h"
    #include "basisu_wrapper.h"

    #define BASE_PACKAGE com/crashinvaders/basisu

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

//    private static native void getFileInfoNative(byte[] data, int dataSize, BasisuFileInfo fileInfo); /*
//
//    */

    //TODO Finish BasisuImageInfo data mapping.
    private static native void getImageInfoNative(byte[] data, int dataSize, int imageIndex, BasisuImageInfo target); /*
        basist:basisu_image_info imageInfo;
        if (!basisuWrapper::getImageInfo(imageInfo, (uint8_t*)data, dataSize, imageIndex)) {
            jniUtils::throwException(env, "Failed to obtain image info.");
            return;
        }

        jclass targetClazz = env->GetObjectClass(target);
        env->SetIntField(target, env->GetFieldID(targetClazz, "width", "I"), imageInfo.m_width);
        env->SetIntField(target, env->GetFieldID(targetClazz, "height", "I"), imageInfo.m_height);
    */

    private static native byte[] transcodeNative(byte[] data, int dataSize, int levelIndex, int textureFormatId); /*
        std::cout << "Transcoding basis image into plain RGBA..." << std::endl;

        std::vector<uint8_t> rgba;
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);

        if (!basisuWrapper::transcode(rgba, (uint8_t*)data, dataSize, levelIndex, format)) {
            std::cout << "Error during image transcoding!" << std::endl;
            jniUtils::throwException(env, "Error during image transcoding!");
            return 0;
        }

        jbyteArray byteArray = env->NewByteArray(rgba.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)rgba.size(), (jbyte*)rgba.data());
        return byteArray;
    */

}
