package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

/**
 * Direct mapping of <code>basist::transcoder_texture_format</code> struct.
 *
 * <p/>
 * High-level composite texture formats supported by the transcoder.
 * Each of these texture formats directly correspond to OpenGL/D3D/Vulkan etc. texture formats.
 *
 * <p/>
 * Notes:
 * <ul>
 *  <li>If you specify a texture format that supports alpha, but the .basis file doesn't have alpha, the transcoder will automatically output a fully opaque (255) alpha channel.</li>
 *  <li>The PVRTC1 texture formats only support power of 2 dimension .basis files, but this may be relaxed in a future version.</li>
 *  <li>The PVRTC1 transcoders are real-time encoders, so don't expect the highest quality. We may add a slower encoder with improved quality.</li>
 *  <li>These enums must be kept in sync with Javascript code that calls the transcoder.</li>
 * </ul>
 *
 * <p/>
 * CLOSEABLE: Instances of this class internally manage native resources
 * and need to be closed using {@link #close()} when no longer needed.
 */
public class BasisuFileInfo implements Closeable {
	/*JNI
        #include "basisu_transcoder.h"

        static basist::basisu_file_info* getWrapped(jlong addr) {
            return (basist::basisu_file_info*)addr;
        }
	 */

    long addr = 0;

    BasisuFileInfo() {
        this.addr = jniCreate();
    }

    BasisuFileInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (addr == 0) {
            throw new IllegalStateException("Object was already closed!");
        }
        jniDispose(addr);
        addr = 0;
    }

//    @Override
//    protected void finalize() throws Throwable {
//        if (addr != 0) {
//            System.err.println(this + " object was GC'ed but never closed!");
//            close();
//        }
//        super.finalize();
//    }

    public BasisuTextureType getTextureType() {
        int textureTypeId = jniGetTextureType(addr);
        return findOrThrow(BasisuTextureType.values(), textureTypeId);
    }
    private native int jniGetTextureType(long addr); /*
        return (int)getWrapped(addr)->m_tex_type;
    */

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = jniGetTextureFormat(addr);
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }
    private native int jniGetTextureFormat(long addr); /*
        return (int)getWrapped(addr)->m_tex_format;
    */

    public int getVersion() { return jniGetVersion(addr); }
    private native int jniGetVersion(long addr); /*
        return getWrapped(addr)->m_version;
    */

    public int getTotalHeaderSize() { return jniGetTotalHeaderSize(addr); }
    private native int jniGetTotalHeaderSize(long addr); /*
        return getWrapped(addr)->m_total_header_size;
    */

    public int getTotalSelectors() { return jniGetTotalSelectors(addr); }
    private native int jniGetTotalSelectors(long addr); /*
        return getWrapped(addr)->m_total_selectors;
    */

    public int getSelectorCodebookSize() { return jniGetSelectorCodebookSize(addr); }
    private native int jniGetSelectorCodebookSize(long addr); /*
        return getWrapped(addr)->m_selector_codebook_size;
    */

    public int getTotalEndpoints() { return jniGetTotalEndpoints(addr); }
    private native int jniGetTotalEndpoints(long addr); /*
        return getWrapped(addr)->m_total_endpoints;
    */

    public int getEndpointCodebookSize() { return jniGetEndpointCodebookSize(addr); }
    private native int jniGetEndpointCodebookSize(long addr); /*
        return getWrapped(addr)->m_endpoint_codebook_size;
    */

    public int getTablesSize() { return jniGetTablesSize(addr); }
    private native int jniGetTablesSize(long addr); /*
        return getWrapped(addr)->m_tables_size;
    */

    public int getSlicesSize() { return jniGetSlicesSize(addr); }
    private native int jniGetSlicesSize(long addr); /*
        return getWrapped(addr)->m_slices_size;
    */

    public int getUsPerFrame() { return jniGetUsPerFrame(addr); }
    private native int jniGetUsPerFrame(long addr); /*
        return getWrapped(addr)->m_us_per_frame;
    */

    /** Total number of images. */
    public int getTotalImages() { return jniGetTotalImages(addr); }
    private native int jniGetTotalImages(long addr); /*
        return getWrapped(addr)->m_total_images;
    */

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() { return jniGetImageMipmapLevels(addr); }
    private native int[] jniGetImageMipmapLevels(long addr); /*
        std::vector<uint32_t> imageLevels = getWrapped(addr)->m_image_mipmap_levels;

        jintArray intArray = env->NewIntArray(imageLevels.size());
        env->SetIntArrayRegion(intArray, (jsize)0, (jsize)imageLevels.size(), (jint*)imageLevels.data());
        return intArray;
    */

    public int getUserdata0() { return jniGetUserdata0(addr); }
    private native int jniGetUserdata0(long addr); /*
        return getWrapped(addr)->m_userdata0;
    */

    public int getUserdata1() { return jniGetUserdata1(addr); }
    private native int jniGetUserdata1(long addr); /*
        return getWrapped(addr)->m_userdata1;
    */

    /** True if the image was Y flipped. */
    public boolean isFlippedY() { return jniIsFlippedY(addr); }
    private native boolean jniIsFlippedY(long addr); /*
        return getWrapped(addr)->m_y_flipped;
    */

    /** True if the file is ETC1. */
    public boolean isEtc1s() { return jniIsEtc1s(addr); }
    private native boolean jniIsEtc1s(long addr); /*
        return getWrapped(addr)->m_etc1s;
    */

    /** True if the texture has alpha slices (for ETC1S: even slices RGB, odd slices alpha). */
    public boolean hasAlphaSlices() { return jniHasAlphaSlices(addr); }
    private native boolean jniHasAlphaSlices(long addr); /*
        return getWrapped(addr)->m_has_alpha_slices;
    */

    private static native long jniCreate(); /*
       basist::basisu_file_info* fileInfo = new basist::basisu_file_info();
       return reinterpret_cast<intptr_t>(fileInfo);
    */

    private static native void jniDispose(long addr); /*
		basist::basisu_file_info* fileInfo = (basist::basisu_file_info*)addr;
		delete fileInfo;
	*/
}
