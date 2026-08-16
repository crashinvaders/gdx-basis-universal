package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

/**
 * Direct mapping of <code>basist::basisu_file_info</code> struct.
 * <p/>
 * Populated in a single native call (see {@link #FIELD_COUNT}) instead of one native call per
 * field, so all getters below are plain Java field reads with no further native-code round trips.
 */
public class BasisuFileInfo implements Closeable {

    static final int FIELD_COUNT = 17;

    private boolean closed;

    private final int version;
    private final int totalHeaderSize;
    private final int totalSelectors;
    private final int selectorCodebookSize;
    private final int totalEndpoints;
    private final int endpointCodebookSize;
    private final int tablesSize;
    private final int slicesSize;
    private final int usPerFrame;
    private final int totalImages;
    private final int userdata0;
    private final int userdata1;
    private final boolean yFlipped;
    private final boolean etc1s;
    private final boolean hasAlphaSlices;
    private final BasisuTextureType textureType;
    private final BasisuTextureFormat textureFormat;
    private final int[] imageMipmapLevels;

    BasisuFileInfo(int[] values, int[] imageMipmapLevels) {
        this.version = values[0];
        this.totalHeaderSize = values[1];
        this.totalSelectors = values[2];
        this.selectorCodebookSize = values[3];
        this.totalEndpoints = values[4];
        this.endpointCodebookSize = values[5];
        this.tablesSize = values[6];
        this.slicesSize = values[7];
        this.usPerFrame = values[8];
        this.totalImages = values[9];
        this.userdata0 = values[10];
        this.userdata1 = values[11];
        this.yFlipped = values[12] != 0;
        this.etc1s = values[13] != 0;
        this.hasAlphaSlices = values[14] != 0;
        this.textureType = findOrThrow(BasisuTextureType.values(), values[15]);
        this.textureFormat = findOrThrow(BasisuTextureFormat.values(), values[16]);
        this.imageMipmapLevels = imageMipmapLevels;
    }

    BasisuFileInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Object was already closed!");
        }
        closed = true;
    }

    public BasisuTextureType getTextureType() {
        return textureType;
    }

    public BasisuTextureFormat getTextureFormat() {
        return textureFormat;
    }

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        return imageMipmapLevels;
    }

    public int getVersion() { return version; }

    public int getTotalHeaderSize() { return totalHeaderSize; }

    public int getTotalSelectors() { return totalSelectors; }

    public int getSelectorCodebookSize() { return selectorCodebookSize; }

    public int getTotalEndpoints() { return totalEndpoints; }

    public int getEndpointCodebookSize() { return endpointCodebookSize; }

    public int getTablesSize() { return tablesSize; }

    public int getSlicesSize() { return slicesSize; }

    public int getUsPerFrame() { return usPerFrame; }

    /** Total number of images. */
    public int getTotalImages() { return totalImages; }

    public int getUserdata0() { return userdata0; }

    public int getUserdata1() { return userdata1; }

    /** True if the image was Y flipped. */
    public boolean isFlippedY() { return yFlipped; }

    /** True if the file is ETC1. */
    public boolean isEtc1s() { return etc1s; }

    /** True if the texture has alpha slices (for ETC1S: even slices RGB, odd slices alpha). */
    public boolean hasAlphaSlices() { return hasAlphaSlices; }
}
