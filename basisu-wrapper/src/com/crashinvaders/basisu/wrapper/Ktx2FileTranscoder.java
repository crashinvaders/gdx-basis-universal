package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Holds an open KTX2 transcoding session for a single encoded file buffer.
 * <p/>
 * Same idea as {@link BasisuFileTranscoder}: decoding the ETC1S global data is done at most once
 * per file, lazily, on the first {@link #transcode} call, instead of once per call.
 * <p/>
 * CLOSEABLE: Instances of this class internally manage native resources
 * and need to be closed using {@link #close()} when no longer needed.
 */
public class Ktx2FileTranscoder implements Closeable {
	/*JNI
        #include "basisu_wrapper.h"
        #include "basisu_native_utils.h"

        static basisuWrapper::ktx2::TranscoderSession* getWrapped(jlong addr) {
            return (basisuWrapper::ktx2::TranscoderSession*)addr;
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
     * @param dataBuffer the raw KTX2 texture data (as it's loaded from the file).
     *                   Must stay alive (not be freed/disposed) for as long as this session is open.
     */
    public Ktx2FileTranscoder(Buffer dataBuffer) {
        this.addr = jniOpen(dataBuffer, dataBuffer.capacity());
    }

    Ktx2FileTranscoder(Object ignored) {
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

    /** @return information about the KTX2 file. */
    public Ktx2FileInfo getFileInfo() {
        int[] values = new int[Ktx2FileInfo.FIELD_COUNT];
        if (!jniGetFileInfo(addr, values)) {
            throw new BasisuWrapperException("Failed to obtain KTX2 file info.");
        }
        return new Ktx2FileInfo(values);
    }
    private native boolean jniGetFileInfo(long addr, int[] outValues); /*
        basisuWrapper::ktx2_file_info fileInfo;
        if (!getWrapped(addr)->getFileInfo(fileInfo)) {
            return false;
        }

        outValues[0] = (jint)fileInfo.layers;
        outValues[1] = (jint)fileInfo.mipmapLevels;
        outValues[2] = (jint)fileInfo.width;
        outValues[3] = (jint)fileInfo.height;
        outValues[4] = fileInfo.hasAlpha ? 1 : 0;
        outValues[5] = (jint)fileInfo.textureFormat;
        return true;
    */

    /** @return information about the specified image level. */
    public Ktx2ImageLevelInfo getImageLevelInfo(int layerIndex, int imageLevel) {
        int[] values = new int[Ktx2ImageLevelInfo.FIELD_COUNT];
        if (!jniGetImageLevelInfo(addr, layerIndex, imageLevel, values)) {
            throw new BasisuWrapperException("Failed to obtain KTX2 image level info.");
        }
        return new Ktx2ImageLevelInfo(values);
    }
    private native boolean jniGetImageLevelInfo(long addr, int layerIndex, int imageLevel, int[] outValues); /*
        basist::ktx2_image_level_info levelInfo;
        if (!getWrapped(addr)->getImageLevelInfo(levelInfo, layerIndex, imageLevel)) {
            return false;
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
        return true;
    */

    public ByteBuffer transcode(int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        return jniTranscode(addr, layerIndex, levelIndex, textureFormat.getId());
    }
    private native ByteBuffer jniTranscode(long addr, int layerIndex, int levelIndex, int textureFormatId); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        basisu::vector<uint8_t> transcodedData;

        if (!getWrapped(addr)->transcode(transcodedData, layerIndex, levelIndex, format)) {
            basisuUtils::throwException(env, "Error during KTX2 image transcoding.");
            return 0;
        }

        return wrapIntoBuffer(env, transcodedData);
    */

    /**
     * Decodes every mipmap level of the image to any of the supported output texture formats,
     * packed into a single buffer (one native allocation for the whole chain instead of one per
     * level - see {@link TranscodedMipChain}).
     */
    public TranscodedMipChain transcodeAllLevels(int layerIndex, BasisuTranscoderTextureFormat textureFormat) {
        int totalLevels = getFileInfo().getTotalMipmapLevels();
        return transcodeAllLevels(layerIndex, totalLevels, textureFormat);
    }

    /**
     * Same as {@link #transcodeAllLevels(int, BasisuTranscoderTextureFormat)}, but only transcodes
     * the first "levelCount" mipmap levels, so a caller that doesn't need the full chain (e.g.
     * mipmaps disabled) doesn't pay for transcoding the rest of it.
     */
    public TranscodedMipChain transcodeAllLevels(int layerIndex, int levelCount, BasisuTranscoderTextureFormat textureFormat) {
        int[] levelOffsets = new int[levelCount + 1];
        ByteBuffer data = jniTranscodeAllLevels(addr, layerIndex, levelCount, textureFormat.getId(), levelOffsets);
        if (data == null) {
            throw new BasisuWrapperException("Error during KTX2 image transcoding.");
        }
        return new TranscodedMipChain(data, levelOffsets);
    }
    private native ByteBuffer jniTranscodeAllLevels(long addr, int layerIndex, int levelCount, int textureFormatId, int[] outLevelOffsets); /*MANUAL
        basist::transcoder_texture_format format = static_cast<basist::transcoder_texture_format>(textureFormatId);
        basisu::vector<uint8_t> transcodedData;
        basisu::vector<uint32_t> levelOffsets;

        if (!getWrapped(addr)->transcodeAllLevels(transcodedData, levelOffsets, layerIndex, (uint32_t)levelCount, format)) {
            basisuUtils::throwException(env, "Error during KTX2 image transcoding.");
            return 0;
        }

        // "MANUAL" methods don't get jnigen's automatic array unwrapping, unlike the non-MANUAL
        // methods elsewhere in this class - Get/ReleaseIntArrayElements has to be done by hand here.
        jint* buf = env->GetIntArrayElements(outLevelOffsets, NULL);
        for (size_t i = 0; i < levelOffsets.size(); i++) {
            buf[i] = (jint)levelOffsets[i];
        }
        env->ReleaseIntArrayElements(outLevelOffsets, buf, 0);

        return wrapIntoBuffer(env, transcodedData);
    */

    private static native long jniOpen(Buffer dataBuffer, int dataSize); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        basisuWrapper::ktx2::TranscoderSession* session = new basisuWrapper::ktx2::TranscoderSession(data, (uint32_t)dataSize);
        return reinterpret_cast<intptr_t>(session);
    */

    private static native void jniClose(long addr); /*
        delete getWrapped(addr);
    */
}
