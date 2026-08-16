package com.crashinvaders.basisu.wrapper;

import java.nio.ByteBuffer;

/**
 * Holds the transcoded bytes for every mipmap level of one image, packed into a single buffer
 * (one native allocation for the whole chain instead of one per level).
 * <p/>
 * Level {@code i}'s bytes occupy {@code [getLevelOffset(i), getLevelOffset(i) + getLevelSize(i))}
 * within {@link #data}.
 * <p/>
 * Use {@link BasisuWrapper#disposeNativeBuffer(ByteBuffer)} to free {@link #data} once uploaded.
 */
public class TranscodedMipChain {

    public final ByteBuffer data;

    /** Length is {@link #getLevelCount()} + 1; the last entry equals {@code data.capacity()}. */
    private final int[] levelOffsets;

    public TranscodedMipChain(ByteBuffer data, int[] levelOffsets) {
        this.data = data;
        this.levelOffsets = levelOffsets;
    }

    public int getLevelCount() {
        return levelOffsets.length - 1;
    }

    public int getLevelOffset(int level) {
        return levelOffsets[level];
    }

    public int getLevelSize(int level) {
        return levelOffsets[level + 1] - levelOffsets[level];
    }
}
