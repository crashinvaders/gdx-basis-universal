package emu.com.crashinvaders.basisu.wrapper;

import com.crashinvaders.basisu.wrapper.BasisuTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuTextureType;

import java.io.Closeable;

public class BasisuFileInfo implements Closeable {

    final BasisuWrapper.BasisuFileInfoJs fileInfoJs;
    private final int[] imageMipmapLevels;

    BasisuFileInfo(Object fileInfoJs, int[] imageMipmapLevels) {
        this.fileInfoJs = (BasisuWrapper.BasisuFileInfoJs) fileInfoJs;
        this.imageMipmapLevels = imageMipmapLevels;
    }

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(fileInfoJs);
    }

    public BasisuTextureType getTextureType() {
        return BasisuWrapper.findOrThrow(BasisuTextureType.values(), fileInfoJs.getTextureType().getValue());
    }

    public BasisuTextureFormat getTextureFormat() {
        return BasisuWrapper.findOrThrow(BasisuTextureFormat.values(), fileInfoJs.getTextureFormat().getValue());
    }

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        return imageMipmapLevels;
    }

    public int getVersion() { return fileInfoJs.getVersion(); }

    public int getTotalHeaderSize() { return fileInfoJs.getTotalHeaderSize(); }

    public int getTotalSelectors() { return fileInfoJs.getTotalSelectors(); }

    public int getSelectorCodebookSize() { return fileInfoJs.getSelectorCodebookSize(); }

    public int getTotalEndpoints() { return fileInfoJs.getTotalEndpoints(); }

    public int getEndpointCodebookSize() { return fileInfoJs.getEndpointCodebookSize(); }

    public int getTablesSize() { return fileInfoJs.getTablesSize(); }

    public int getSlicesSize() { return fileInfoJs.getSlicesSize(); }

    public int getUsPerFrame() { return fileInfoJs.getUsPerFrame(); }

    /** Total number of images. */
    public int getTotalImages() { return fileInfoJs.getTotalImages(); }

    public int getUserdata0() { return fileInfoJs.getUserdata0(); }

    public int getUserdata1() { return fileInfoJs.getUserdata1(); }

    /** True if the image was Y flipped. */
    public boolean isFlippedY() { return fileInfoJs.getYFlipped(); }

    /** True if the file is ETC1. */
    public boolean isEtc1s() { return fileInfoJs.getEtc1s(); }

    /** True if the texture has alpha slices (for ETC1S: even slices RGB, odd slices alpha). */
    public boolean hasAlphaSlices() { return fileInfoJs.getHasAlphaSlices(); }
}
