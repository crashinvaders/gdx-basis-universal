package com.crashinvaders.basisu.wrapper;

/**
 * Direct mapping of <code>basist::basisu_image_info</code> struct.
 */
public class BasisuImageInfo {
	/*JNI
        #include "basisu_transcoder.h"

        static basist::basisu_image_info* getWrapped(jlong addr) {
            return (basist::basisu_image_info*)addr;
        }
	 */

    final long addr;

    BasisuImageInfo() {
        this.addr = jniCreate();
    }

    BasisuImageInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    //TODO Finalizers are deprecated since Java 9. Figure how to handle native resource release properly.
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        jniDispose(addr);
    }


    public int getImageIndex() { return jniGetImageIndex(addr); }
    private native int jniGetImageIndex(long addr); /*
        return getWrapped(addr)->m_image_index;
    */

    public int getTotalLevels() { return jniGetTotalLevels(addr); }
    private native int jniGetTotalLevels(long addr); /*
        return getWrapped(addr)->m_total_levels;
    */

    public int getOrigWidth() { return jniGetOrigWidth(addr); }
    private native int jniGetOrigWidth(long addr); /*
        return getWrapped(addr)->m_orig_width;
    */

    public int getOrigHeight() { return jniGetOrigHeight(addr); }
    private native int jniGetOrigHeight(long addr); /*
        return getWrapped(addr)->m_orig_height;
    */

    public int getWidth() { return jniGetWidth(addr); }
    private native int jniGetWidth(long addr); /*
        return getWrapped(addr)->m_width;
    */

    public int getHeight() { return jniGetHeight(addr); }
    private native int jniGetHeight(long addr); /*
        return getWrapped(addr)->m_height;
    */

    public int getNumBlocksX() { return jniGetNumBlocksX(addr); }
    private native int jniGetNumBlocksX(long addr); /*
        return getWrapped(addr)->m_num_blocks_x;
    */

    public int getNumBlocksY() { return jniGetNumBlocksY(addr); }
    private native int jniGetNumBlocksY(long addr); /*
        return getWrapped(addr)->m_num_blocks_y;
    */

    public int getTotalBlocks() { return jniGetTotalBlocks(addr); }
    private native int jniGetTotalBlocks(long addr); /*
        return getWrapped(addr)->m_total_blocks;
    */

    public int getFirstSliceIndex() { return jniGetFirstSliceIndex(addr); }
    private native int jniGetFirstSliceIndex(long addr); /*
        return getWrapped(addr)->m_first_slice_index;
    */

    /** True if the image has alpha data. */
    public boolean hasAlphaFlag() { return jniHasAlphaFlag(addr); }
    private native boolean jniHasAlphaFlag(long addr); /*
        return getWrapped(addr)->m_alpha_flag;
    */

    /** True if the image is an I-Frame. */
    public boolean hasIframeFlag() { return jniHasIframeFlag(addr); }
    private native boolean jniHasIframeFlag(long addr); /*
        return getWrapped(addr)->m_iframe_flag;
    */

    private static native long jniCreate(); /*
       basist::basisu_image_info* imageInfo = new basist::basisu_image_info();
       return reinterpret_cast<intptr_t>(imageInfo);
    */

    private static native void jniDispose (long addr); /*
		basist::basisu_image_info* imageInfo = (basist::basisu_image_info*)addr;
		delete imageInfo;
	*/
}
