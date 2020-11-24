package com.crashinvaders.basisu.wrapper;

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

    //TODO Finish BasisuFileInfo data mapping.
    private static native void getFileInfoNative(byte[] data, int dataSize, BasisuFileInfo target); /*
        basist::basisu_file_info fileInfo;
        if (!basisuWrapper::getFileInfo(fileInfo, (uint8_t*)data, dataSize)) {
            jniUtils::throwException(env, "Failed to obtain file info.");
            return;
        }

        jclass targetClazz = env->GetObjectClass(target);
        env->SetIntField(target, env->GetFieldID(targetClazz, "version", "I"), fileInfo.m_version);
//		uint32_t m_total_header_size;
//		uint32_t m_total_selectors;
//		uint32_t m_selector_codebook_size;
//		uint32_t m_total_endpoints;
//		uint32_t m_endpoint_codebook_size;
//		uint32_t m_tables_size;
//		uint32_t m_slices_size;
//		basis_texture_type m_tex_type;
//		uint32_t m_us_per_frame;
//		basisu_slice_info_vec m_slice_info;
        env->SetIntField(target, env->GetFieldID(targetClazz, "totalImages", "I"), fileInfo.m_total_images);
//		std::vector<uint32_t> m_image_mipmap_levels;
//		uint32_t m_userdata0;
//		uint32_t m_userdata1;
//		basis_tex_format m_tex_format;
//		bool m_y_flipped;
//		bool m_etc1s;
//		bool m_has_alpha_slices;
    */

    private static native void getImageInfoNative(byte[] data, int dataSize, int imageIndex, BasisuImageInfo target); /*
        basist::basisu_image_info imageInfo;
        if (!basisuWrapper::getImageInfo(imageInfo, (uint8_t*)data, dataSize, imageIndex)) {
            jniUtils::throwException(env, "Failed to obtain image info.");
            return;
        }

        jclass targetClazz = env->GetObjectClass(target);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "width",           "I"), imageInfo.m_width);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "height",          "I"), imageInfo.m_height);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "imageIndex",      "I"), imageInfo.m_image_index);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "totalLevels",     "I"), imageInfo.m_total_levels);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "origWidth",       "I"), imageInfo.m_orig_width);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "origHeight",      "I"), imageInfo.m_orig_height);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "numBlocksX",      "I"), imageInfo.m_num_blocks_x);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "numBlocksY",      "I"), imageInfo.m_num_blocks_y);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "totalBlocks",     "I"), imageInfo.m_total_blocks);
        env->SetIntField    (target, env->GetFieldID(targetClazz, "firstSliceIndex", "I"), imageInfo.m_first_slice_index);
        env->SetBooleanField(target, env->GetFieldID(targetClazz, "alphaFlag",       "Z"), imageInfo.m_alpha_flag);
        env->SetBooleanField(target, env->GetFieldID(targetClazz, "iframeFlag",      "Z"), imageInfo.m_iframe_flag);
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
