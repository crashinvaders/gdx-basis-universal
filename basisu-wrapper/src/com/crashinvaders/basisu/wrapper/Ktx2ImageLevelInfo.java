package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

/**
 * Direct mapping of <code>basist::ktx2_image_level_info</code> struct.
 * <p/>
 * Populated in a single native call (see {@link #FIELD_COUNT}) instead of one native call per
 * field, so all getters below are plain Java field reads with no further native-code round trips.
 */
public class Ktx2ImageLevelInfo implements Closeable {

    static final int FIELD_COUNT = 12;

    private boolean closed;

    private final int levelIndex;
    private final int layerIndex;
    private final int faceIndex;
    private final int origWidth;
    private final int origHeight;
    private final int width;
    private final int height;
    private final int numBlocksX;
    private final int numBlocksY;
    private final int totalBlocks;
    private final boolean alphaFlag;
    private final boolean iframeFlag;

    Ktx2ImageLevelInfo(int[] values) {
        this.levelIndex = values[0];
        this.layerIndex = values[1];
        this.faceIndex = values[2];
        this.origWidth = values[3];
        this.origHeight = values[4];
        this.width = values[5];
        this.height = values[6];
        this.numBlocksX = values[7];
        this.numBlocksY = values[8];
        this.totalBlocks = values[9];
        this.alphaFlag = values[10] != 0;
        this.iframeFlag = values[11] != 0;
    }

    Ktx2ImageLevelInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Object was already closed!");
        }
        closed = true;
    }

    /** The mipmap level index (0=largest) of the image. */
    public int getLevelIndex() { return levelIndex; }

    /** The texture array layer index of the image. */
    public int getLayerIndex() { return layerIndex; }

    /** The cubemap face index of the image.*/
    public int getFaceIndex() { return faceIndex; }

    /** The image's actual (or the original source image's) width in pixels, which may not be divisible by 4 pixels. */
    public int getOrigWidth() { return origWidth; }

    /** The image's actual (or the original source image's) height in pixels, which may not be divisible by 4 pixels. */
    public int getOrigHeight() { return origHeight; }

    /** The image's physical width, which will always be divisible by 4 pixels. */
    public int getWidth() { return width; }

    /** The image's physical height, which will always be divisible by 4 pixels. */
    public int getHeight() { return height; }

    /** The texture's width in 4x4 texel blocks. */
    public int getNumBlocksX() { return numBlocksX; }

    /** The texture's height in 4x4 texel blocks. */
    public int getNumBlocksY() { return numBlocksY; }

    /** The total number of blocks */
    public int getTotalBlocks() { return totalBlocks; }

    /** True if the image has alpha data */
    public boolean getAlphaFlag() { return alphaFlag; }

    /** True if the image is an I-Frame.
     * Currently, for ETC1S textures, the first frame will always be an I-Frame,
     * and subsequent frames will always be P-Frames. */
    public boolean getIframeFlag() { return iframeFlag; }
}
