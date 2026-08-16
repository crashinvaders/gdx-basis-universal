package com.crashinvaders.basisu.wrapper;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

/**
 * Direct mapping of <code>basisUniversal::ktx2_file_info</code> struct.
 * <p/>
 * Populated in a single native call (see {@link #FIELD_COUNT}) instead of one native call per
 * field, so all getters below are plain Java field reads with no further native-code round trips.
 */
public class Ktx2FileInfo implements Closeable {

    static final int FIELD_COUNT = 6;

    private boolean closed;

    private final int totalLayers;
    private final int totalMipmapLevels;
    private final int imageWidth;
    private final int imageHeight;
    private final boolean hasAlpha;
    private final BasisuTextureFormat textureFormat;

    Ktx2FileInfo(int[] values) {
        this.totalLayers = values[0];
        this.totalMipmapLevels = values[1];
        this.imageWidth = values[2];
        this.imageHeight = values[3];
        this.hasAlpha = values[4] != 0;
        this.textureFormat = findOrThrow(BasisuTextureFormat.values(), values[5]);
    }

    Ktx2FileInfo(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public void close() {
        if (closed) {
            throw new IllegalStateException("Object was already closed!");
        }
        closed = true;
    }

    public int getTotalLayers() { return totalLayers; }

    public int getTotalMipmapLevels() { return totalMipmapLevels; }

    public int getImageWidth() { return imageWidth; }

    public int getImageHeight() { return imageHeight; }

    public boolean hasAlpha() { return hasAlpha; }

    public BasisuTextureFormat getTextureFormat() { return textureFormat; }
}
