package com.crashinvaders.basisu;

public class BasisImageInfo {
    private int imageIndex;
    private int totalLevels;
    private int origWidth;
    private int origHeight;
    private int width;
    private int height;
    private int numBlocksX;
    private int numBlocksY;
    private int totalBlocks;
    private boolean hasAlpha;

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

    public boolean isHasAlpha() {
        return hasAlpha;
    }
}
