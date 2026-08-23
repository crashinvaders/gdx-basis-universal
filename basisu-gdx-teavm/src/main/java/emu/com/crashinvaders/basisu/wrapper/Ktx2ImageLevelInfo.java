package emu.com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

public class Ktx2ImageLevelInfo implements Closeable {

    final BasisuWrapper.Ktx2ImageLevelInfoJs imageLevelInfoJs;

    Ktx2ImageLevelInfo(Object imageLevelInfoJs) {
        this.imageLevelInfoJs = (BasisuWrapper.Ktx2ImageLevelInfoJs) imageLevelInfoJs;
    }

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(imageLevelInfoJs);
    }

    public int getLevelIndex() { return imageLevelInfoJs.getLevelIndex(); }

    public int getLayerIndex() { return imageLevelInfoJs.getLayerIndex(); }

    public int getFaceIndex() { return imageLevelInfoJs.getFaceIndex(); }

    public int getOrigWidth() { return imageLevelInfoJs.getOrigWidth(); }

    public int getOrigHeight() { return imageLevelInfoJs.getOrigHeight(); }

    public int getWidth() { return imageLevelInfoJs.getWidth(); }

    public int getHeight() { return imageLevelInfoJs.getHeight(); }

    public int getNumBlocksX() { return imageLevelInfoJs.getNumBlocksX(); }

    public int getNumBlocksY() { return imageLevelInfoJs.getNumBlocksY(); }

    public int getTotalBlocks() { return imageLevelInfoJs.getTotalBlocks(); }

    public boolean getAlphaFlag() { return imageLevelInfoJs.getAlphaFlag(); }

    public boolean getIframeFlag() { return imageLevelInfoJs.getIframeFlag(); }
}
