package emu.com.crashinvaders.basisu.wrapper;

import com.crashinvaders.basisu.wrapper.BasisuTextureFormat;

import java.io.Closeable;

public class Ktx2FileInfo implements Closeable {

    final BasisuWrapper.Ktx2FileInfoJs fileInfoJs;

    Ktx2FileInfo(Object fileInfoJs) {
        this.fileInfoJs = (BasisuWrapper.Ktx2FileInfoJs) fileInfoJs;
    }

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(fileInfoJs);
    }

    public int getTotalLayers() { return fileInfoJs.getLayers(); }

    public int getTotalMipmapLevels() { return fileInfoJs.getMipmapLevels(); }

    public int getImageWidth() { return fileInfoJs.getWidth(); }

    public int getImageHeight() { return fileInfoJs.getHeight(); }

    public boolean hasAlpha() { return fileInfoJs.getHasAlpha(); }

    public BasisuTextureFormat getTextureFormat() {
        return BasisuWrapper.findOrThrow(BasisuTextureFormat.values(), fileInfoJs.getTextureFormat().getValue());
    }
}
