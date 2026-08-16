package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Holds an open Basis Universal transcoding session for a single encoded file buffer.
 * <p/>
 * Decoding the ETC1S global codebooks (done lazily, once, on the first {@link #transcode} call)
 * is the expensive part of transcoding. Keeping this session open across multiple
 * {@link #getImageInfo}/{@link #getImageLevelInfo}/{@link #transcode} calls for the same file
 * (e.g. once per mipmap level) avoids repeating that work on every single call.
 * <p/>
 * CLOSEABLE: Instances of this class internally manage native resources
 * and need to be closed using {@link #close()} when no longer needed.
 */
public class BasisuFileTranscoder implements Closeable {
	/*JNI
        #include "basisu_wrapper.h"
        #include "basisu_native_utils.h"

        static basisuWrapper::basis::TranscoderSession* getWrapped(jlong addr) {
            return (basisuWrapper::basis::TranscoderSession*)addr;
        }

        // Marked "static" (internal linkage) since "com_crashinvaders_basisu_wrapper_BasisuWrapper.cpp"
        // already defines a function with the same name and signature.
        static jobject wrapIntoBuffer(JNIEnv* env, basisu::vector<uint8_t>& imageData) {
            uint32_t imageDataSize = imageData.size_in_bytes();
            uint8_t* nativeBuffer = (uint8_t*)malloc(imageDataSize);
            memcpy(nativeBuffer, imageData.data(), imageDataSize);
            return env->NewDirectByteBuffer(nativeBuffer, imageDataSize);
        }
	 */

    long addr;

    /**
     * @param dataBuffer the raw Basis texture data (as it's loaded from the file).
     *                   Must stay alive (not be freed/disposed) for as long as this session is open.
     */
    public BasisuFileTranscoder(Buffer dataBuffer) {
        this.addr = jniOpen(dataBuffer, dataBuffer.capacity());
    }

    BasisuFileTranscoder(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (addr == 0) {
            throw new IllegalStateException("Object was already closed!");
        }
        jniClose(addr);
        addr = 0;
    }

    /** Quick header validation - no crc16 checks. */
    public boolean validateHeader() { return jniValidateHeader(addr); }
    private native boolean jniValidateHeader(long addr); /*
        return getWrapped(addr)->validateHeader();
    */

    /** Validates the .basis file. This computes a crc16 over the entire file, so it's slow. */
    public boolean validateChecksum(boolean fullValidation) { return jniValidateChecksum(addr, fullValidation); }
    private native boolean jniValidateChecksum(long addr, boolean fullValidation); /*
        return getWrapped(addr)->validateChecksum(fullValidation);
    */

    /** @return a description of the basis file and low-level information about each slice. */
    public BasisuFileInfo getFileInfo() {
        int[] values = new int[BasisuFileInfo.FIELD_COUNT];
        int[] imageMipmapLevels = jniGetFileInfo(addr, values);
        if (imageMipmapLevels == null) {
            throw new BasisuWrapperException("Failed to obtain Basis file info.");
        }
        return new BasisuFileInfo(values, imageMipmapLevels);
    }
    // Returns the variable-length mipmap level count array as the return value, while the fixed
    // set of scalar fields is packed into "outValues" - one native call populates the whole object.
    private native int[] jniGetFileInfo(long addr, int[] outValues); /*
        basist::basisu_file_info fileInfo;
        if (!getWrapped(addr)->getFileInfo(fileInfo)) {
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

    /** @return information about the specified image. */
    public BasisuImageInfo getImageInfo(int imageIndex) {
        int[] values = new int[BasisuImageInfo.FIELD_COUNT];
        if (!jniGetImageInfo(addr, imageIndex, values)) {
            throw new BasisuWrapperException("Failed to obtain Basis image info.");
        }
        return new BasisuImageInfo(values);
    }
    private native boolean jniGetImageInfo(long addr, int imageIndex, int[] outValues); /*
        basist::basisu_image_info imageInfo;
        if (!getWrapped(addr)->getImageInfo(imageInfo, imageIndex)) {
            return false;
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
        return true;
    */

    public BasisuImageLevelInfo getImageLevelInfo(int imageIndex, int imageLevel) {
        int[] values = new int[BasisuImageLevelInfo.FIELD_COUNT];
        if (!jniGetImageLevelInfo(addr, imageIndex, imageLevel, values)) {
            throw new BasisuWrapperException("Failed to obtain Basis image level info.");
        }
        return new BasisuImageLevelInfo(values);
    }
    private native boolean jniGetImageLevelInfo(long addr, int imageIndex, int imageLevel, int[] outValues); /*
        basist::basisu_image_level_info levelInfo;
        if (!getWrapped(addr)->getImageLevelInfo(levelInfo, imageIndex, imageLevel)) {
            return false;
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
        return true;
    */

    /**
     * Decodes a single mipmap level from the .basis file to any of the supported output texture formats.
     * If the .basis file doesn't have alpha slices, the output alpha blocks will be set to fully opaque (all 255's).
     * Currently, to decode to PVRTC1 the basis texture's dimensions in pixels must be a power of 2, due to PVRTC1 format requirements.
     * @return the transcoded texture bytes
     */
    public ByteBuffer transcode(int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        return jniTranscode(addr, imageIndex, levelIndex, textureFormat.getId());
    }
    private native ByteBuffer jniTranscode(long addr, int imageIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        basisu::vector<uint8_t> transcodedData;

        if (!getWrapped(addr)->transcode(transcodedData, imageIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during Basis image transcoding.");
            return 0;
        }

        return wrapIntoBuffer(env, transcodedData);
    */

    private static native long jniOpen(Buffer dataBuffer, int dataSize); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        basisuWrapper::basis::TranscoderSession* session = new basisuWrapper::basis::TranscoderSession(data, (uint32_t)dataSize);
        return reinterpret_cast<intptr_t>(session);
    */

    private static native void jniClose(long addr); /*
        delete getWrapped(addr);
    */
}
