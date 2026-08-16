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
        BasisuFileInfo fileInfo = new BasisuFileInfo();
        if (!jniGetFileInfo(addr, fileInfo.addr)) {
            throw new BasisuWrapperException("Failed to obtain Basis file info.");
        }
        return fileInfo;
    }
    private native boolean jniGetFileInfo(long addr, long fileInfoAddr); /*
        return getWrapped(addr)->getFileInfo(*(basist::basisu_file_info*)fileInfoAddr);
    */

    /** @return information about the specified image. */
    public BasisuImageInfo getImageInfo(int imageIndex) {
        BasisuImageInfo imageInfo = new BasisuImageInfo();
        if (!jniGetImageInfo(addr, imageInfo.addr, imageIndex)) {
            throw new BasisuWrapperException("Failed to obtain Basis image info.");
        }
        return imageInfo;
    }
    private native boolean jniGetImageInfo(long addr, long imageInfoAddr, int imageIndex); /*
        return getWrapped(addr)->getImageInfo(*(basist::basisu_image_info*)imageInfoAddr, imageIndex);
    */

    public BasisuImageLevelInfo getImageLevelInfo(int imageIndex, int imageLevel) {
        BasisuImageLevelInfo levelInfo = new BasisuImageLevelInfo();
        if (!jniGetImageLevelInfo(addr, levelInfo.addr, imageIndex, imageLevel)) {
            throw new BasisuWrapperException("Failed to obtain Basis image level info.");
        }
        return levelInfo;
    }
    private native boolean jniGetImageLevelInfo(long addr, long imageInfoAddr, int imageIndex, int imageLevel); /*
        return getWrapped(addr)->getImageLevelInfo(*(basist::basisu_image_level_info*)imageInfoAddr, imageIndex, imageLevel);
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
