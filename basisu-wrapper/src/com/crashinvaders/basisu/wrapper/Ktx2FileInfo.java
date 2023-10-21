package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

/**
 * Direct mapping of <code>basisUniversal::ktx2_file_info</code> struct.
 * <p/>
 * CLOSEABLE: Instances of this class internally manage native resources
 * and need to be closed using {@link #close()} when no longer needed.
 */
public class Ktx2FileInfo implements Closeable {
	/*JNI
        #include "basisu_wrapper.h"

        using namespace basisuWrapper;

        static ktx2_file_info* getWrapped(jlong addr) {
            return (ktx2_file_info*)addr;
        }
	 */

    long addr;

    Ktx2FileInfo() {
        this.addr = jniCreate();
    }

    Ktx2FileInfo(Object ignored) {
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

    public int getTotalLayers() {
        return getTotalLayersNative(addr);
    }
    private native int getTotalLayersNative(long addr); /*
        return getWrapped(addr)->layers;
    */

    public int getTotalMipmapLevels() {
        return getTotalMipmapLevelsNative(addr);
    }
    private native int getTotalMipmapLevelsNative(long addr); /*
        return getWrapped(addr)->mipmapLevels;
    */

    public int getImageWidth() {
        return getImageWidthNative(addr);
    }
    private native int getImageWidthNative(long addr); /*
        return getWrapped(addr)->width;
    */

    public int getImageHeight() {
        return getImageHeightNative(addr);
    }
    private native int getImageHeightNative(long addr); /*
        return getWrapped(addr)->height;
    */

    public boolean hasAlpha() {
        return hasAlphaNative(addr);
    }
    private native boolean hasAlphaNative(long addr); /*
        return getWrapped(addr)->hasAlpha;
    */

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getTextureFormatNative(addr);
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }
    private native int getTextureFormatNative(long addr); /*
        return (int)getWrapped(addr)->textureFormat;
    */

    private static native long jniCreate(); /*
        ktx2_file_info* fileInfo = new ktx2_file_info();
        return reinterpret_cast<intptr_t>(fileInfo);
    */

    private static native void jniDispose(long addr); /*
		ktx2_file_info* fileInfo = (ktx2_file_info*)addr;
		delete fileInfo;
	*/
}
