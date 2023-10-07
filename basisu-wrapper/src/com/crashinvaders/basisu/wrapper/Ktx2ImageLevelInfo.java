package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

/**
 * Direct mapping of <code>basist::ktx2_image_level_info</code> struct.
 * <p/>
 * CLOSEABLE: Instances of this class internally manage native resources
 * and need to be closed using {@link #close()} when no longer needed.
 */
public class Ktx2ImageLevelInfo implements Closeable {
	/*JNI
        #include "basisu_transcoder.h"

        static basist::ktx2_image_level_info* getWrapped(jlong addr) {
            return (basist::ktx2_image_level_info*)addr;
        }
	 */

    long addr;

    Ktx2ImageLevelInfo() {
        this.addr = jniCreate();
    }

    Ktx2ImageLevelInfo(Object ignored) {
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

    /** The mipmap level index (0=largest) of the image. */
    public int getLevelIndex() { return jniGetLevelIndex(addr); }
    private native int jniGetLevelIndex(long addr); /*
        return getWrapped(addr)->m_level_index;
    */

    /** The texture array layer index of the image. */
    public int getLayerIndex() { return jniGetLayerIndex(addr); }
    private native int jniGetLayerIndex(long addr); /*
        return getWrapped(addr)->m_layer_index;
    */

    /** The cubemap face index of the image.*/
    public int getFaceIndex() { return jniGetFaceIndex(addr); }
    private native int jniGetFaceIndex(long addr); /*
        return getWrapped(addr)->m_face_index;
    */

    /** The image's actual (or the original source image's) width in pixels, which may not be divisible by 4 pixels. */
    public int getOrigWidth() { return jniGetOrigWidth(addr); }
    private native int jniGetOrigWidth(long addr); /*
        return getWrapped(addr)->m_orig_width;
    */

    /** The image's actual (or the original source image's) height in pixels, which may not be divisible by 4 pixels. */
    public int getOrigHeight() { return jniGetOrigHeight(addr); }
    private native int jniGetOrigHeight(long addr); /*
        return getWrapped(addr)->m_orig_height;
    */

    /** The image's physical width, which will always be divisible by 4 pixels. */
    public int getWidth() { return jniGetWidth(addr); }
    private native int jniGetWidth(long addr); /*
        return getWrapped(addr)->m_width;
    */

    /** The image's physical height, which will always be divisible by 4 pixels. */
    public int getHeight() { return jniGetHeight(addr); }
    private native int jniGetHeight(long addr); /*
        return getWrapped(addr)->m_height;
    */

    /** The texture's width in 4x4 texel blocks. */
    public int getNumBlocksX() { return jniGetNumBlocksX(addr); }
    private native int jniGetNumBlocksX(long addr); /*
        return getWrapped(addr)->m_num_blocks_x;
    */

    /** The texture's height in 4x4 texel blocks. */
    public int getNumBlocksY() { return jniGetNumBlocksY(addr); }
    private native int jniGetNumBlocksY(long addr); /*
        return getWrapped(addr)->m_num_blocks_y;
    */

    /** The total number of blocks */
    public int getTotalBlocks() { return jniGetTotalBlocks(addr); }
    private native int jniGetTotalBlocks(long addr); /*
        return getWrapped(addr)->m_total_blocks;
    */

    /** True if the image has alpha data */
    public boolean getAlphaFlag() { return jniGetAlphaFlag(addr); }
    private native boolean jniGetAlphaFlag(long addr); /*
        return getWrapped(addr)->m_alpha_flag;
    */

    /** True if the image is an I-Frame.
     * Currently, for ETC1S textures, the first frame will always be an I-Frame,
     * and subsequent frames will always be P-Frames. */
    public boolean getIframeFlag() { return jniGetIframeFlag(addr); }
    private native boolean jniGetIframeFlag(long addr); /*
        return getWrapped(addr)->m_iframe_flag;
    */

    private static native long jniCreate(); /*
        basist::ktx2_image_level_info* imageInfo = new basist::ktx2_image_level_info();
        return reinterpret_cast<intptr_t>(imageInfo);
    */

    private static native void jniDispose(long addr); /*
		basist::ktx2_image_level_info* imageInfo = (basist::ktx2_image_level_info*)addr;
		delete imageInfo;
	*/
}
