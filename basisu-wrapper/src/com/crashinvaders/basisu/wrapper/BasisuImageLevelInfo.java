package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

/**
 * Direct mapping of <code>basist::basisu_image_level_info</code> struct.
 * <p/>
 * Populated in a single native call (see {@link #FIELD_COUNT}) instead of one native call per
 * field, so all getters below are plain Java field reads with no further native-code round trips.
 */
public class BasisuImageLevelInfo implements Closeable {

    static final int FIELD_COUNT = 12;

    private boolean closed;

    private final int imageIndex;
    private final int levelIndex;
    private final int origWidth;
    private final int origHeight;
    private final int width;
    private final int height;
    private final int numBlocksX;
    private final int numBlocksY;
    private final int totalBlocks;
    private final int firstSliceIndex;
    private final boolean alphaFlag;
    private final boolean iframeFlag;

    BasisuImageLevelInfo(int[] values) {
        this.imageIndex = values[0];
        this.levelIndex = values[1];
        this.origWidth = values[2];
        this.origHeight = values[3];
        this.width = values[4];
        this.height = values[5];
        this.numBlocksX = values[6];
        this.numBlocksY = values[7];
        this.totalBlocks = values[8];
        this.firstSliceIndex = values[9];
        this.alphaFlag = values[10] != 0;
        this.iframeFlag = values[11] != 0;
    }

    BasisuImageLevelInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Object was already closed!");
        }
        closed = true;
    }

    public int getImageIndex() { return imageIndex; }

    public int getLevelIndex() { return levelIndex; }

    public int getOrigWidth() { return origWidth; }

    public int getOrigHeight() { return origHeight; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getNumBlocksX() { return numBlocksX; }

    public int getNumBlocksY() { return numBlocksY; }

    public int getTotalBlocks() { return totalBlocks; }

    public int getFirstSliceIndex() { return firstSliceIndex; }

    /** True if the image has alpha data. */
    public boolean hasAlphaFlag() { return alphaFlag; }

    /** True if the image is an I-Frame. */
    public boolean hasIframeFlag() { return iframeFlag; }
}
