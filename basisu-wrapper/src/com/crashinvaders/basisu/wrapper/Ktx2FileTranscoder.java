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
        Ktx2FileInfo fileInfo = new Ktx2FileInfo();
        if (!jniGetFileInfo(addr, fileInfo.addr)) {
            throw new BasisuWrapperException("Failed to obtain KTX2 file info.");
        }
        return fileInfo;
    }
    private native boolean jniGetFileInfo(long addr, long fileInfoAddr); /*
        return getWrapped(addr)->getFileInfo(*(basisuWrapper::ktx2_file_info*)fileInfoAddr);
    */

    /** @return information about the specified image level. */
    public Ktx2ImageLevelInfo getImageLevelInfo(int layerIndex, int imageLevel) {
        Ktx2ImageLevelInfo imageInfo = new Ktx2ImageLevelInfo();
        if (!jniGetImageLevelInfo(addr, imageInfo.addr, layerIndex, imageLevel)) {
            throw new BasisuWrapperException("Failed to obtain KTX2 image level info.");
        }
        return imageInfo;
    }
    private native boolean jniGetImageLevelInfo(long addr, long imageInfoAddr, int layerIndex, int imageLevel); /*
        return getWrapped(addr)->getImageLevelInfo(*(basist::ktx2_image_level_info*)imageInfoAddr, layerIndex, imageLevel);
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

    private static native long jniOpen(Buffer dataBuffer, int dataSize); /*MANUAL
        uint8_t* data = (uint8_t*)env->GetDirectBufferAddress(dataBuffer);
        basisuWrapper::ktx2::TranscoderSession* session = new basisuWrapper::ktx2::TranscoderSession(data, (uint32_t)dataSize);
        return reinterpret_cast<intptr_t>(session);
    */

    private static native void jniClose(long addr); /*
        delete getWrapped(addr);
    */
}
