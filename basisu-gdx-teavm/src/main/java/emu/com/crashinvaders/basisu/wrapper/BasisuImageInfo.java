package emu.com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

public class BasisuImageInfo implements Closeable {

    final BasisuWrapper.BasisuImageInfoJs imageInfoJs;

    BasisuImageInfo(Object imageInfoJs) {
        this.imageInfoJs = (BasisuWrapper.BasisuImageInfoJs) imageInfoJs;
    }

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(imageInfoJs);
    }

    public int getImageIndex() { return imageInfoJs.getImageIndex(); }

    public int getTotalLevels() { return imageInfoJs.getTotalLevels(); }

    public int getOrigWidth() { return imageInfoJs.getOrigWidth(); }

    public int getOrigHeight() { return imageInfoJs.getOrigHeight(); }

    public int getWidth() { return imageInfoJs.getWidth(); }

    public int getHeight() { return imageInfoJs.getHeight(); }

    public int getNumBlocksX() { return imageInfoJs.getNumBlocksX(); }

    public int getNumBlocksY() { return imageInfoJs.getNumBlocksY(); }

    public int getTotalBlocks() { return imageInfoJs.getTotalBlocks(); }

    public int getFirstSliceIndex() { return imageInfoJs.getFirstSliceIndex(); }

    public boolean hasAlphaFlag() { return imageInfoJs.getAlphaFlag(); }

    public boolean hasIframeFlag() { return imageInfoJs.getIframeFlag(); }
}
