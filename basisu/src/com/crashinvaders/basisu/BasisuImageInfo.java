package com.crashinvaders.basisu;

/**
 * Direct mapping of <code>basist::basisu_image_info</code> struct.
 */
public class BasisuImageInfo {
    int imageIndex;
    int totalLevels;

    int origWidth;
    int origHeight;

    int width;
    int height;

    int numBlocksX;
    int numBlocksY;
    int totalBlocks;

    int firstSliceIndex;

    boolean alphaFlag;
    boolean iframeFlag;

    public int getImageIndex() {
        return imageIndex;
    }

    public int getTotalLevels() {
        return totalLevels;
    }

    public int getOrigWidth() {
        return origWidth;
    }

    public int getOrigHeight() {
        return origHeight;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumBlocksX() {
        return numBlocksX;
    }

    public int getNumBlocksY() {
        return numBlocksY;
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public int getFirstSliceIndex() {
        return firstSliceIndex;
    }

    /** True if the image has alpha data. */
    public boolean isAlphaFlag() {
        return alphaFlag;
    }

    /** True if the image is an I-Frame. */
    public boolean isIframeFlag() {
        return iframeFlag;
    }
}
