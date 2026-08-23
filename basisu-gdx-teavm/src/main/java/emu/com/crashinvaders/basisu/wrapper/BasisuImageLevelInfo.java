package emu.com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

public class BasisuImageLevelInfo implements Closeable {

    final BasisuWrapper.BasisuImageLevelInfoJs levelInfoJs;

    BasisuImageLevelInfo(Object levelInfoJs) {
        this.levelInfoJs = (BasisuWrapper.BasisuImageLevelInfoJs) levelInfoJs;
    }

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(levelInfoJs);
    }

    public int getImageIndex() { return levelInfoJs.getImageIndex(); }

    public int getLevelIndex() { return levelInfoJs.getLevelIndex(); }

    public int getOrigWidth() { return levelInfoJs.getOrigWidth(); }

    public int getOrigHeight() { return levelInfoJs.getOrigHeight(); }

    public int getWidth() { return levelInfoJs.getWidth(); }

    public int getHeight() { return levelInfoJs.getHeight(); }

    public int getNumBlocksX() { return levelInfoJs.getNumBlocksX(); }

    public int getNumBlocksY() { return levelInfoJs.getNumBlocksY(); }

    public int getTotalBlocks() { return levelInfoJs.getTotalBlocks(); }

    public int getFirstSliceIndex() { return levelInfoJs.getFirstSliceIndex(); }

    public boolean hasAlphaFlag() { return levelInfoJs.getAlphaFlag(); }

    public boolean hasIframeFlag() { return levelInfoJs.getIframeFlag(); }
}
