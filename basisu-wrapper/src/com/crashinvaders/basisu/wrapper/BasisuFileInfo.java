package com.crashinvaders.basisu.wrapper;

/**
 * Direct mapping of <code>basist::transcoder_texture_format</code> struct.
 * <p/>
 * High-level composite texture formats supported by the transcoder.
 * Each of these texture formats directly correspond to OpenGL/D3D/Vulkan etc. texture formats.
 * <p/>
 * Notes:
 * <ul>
 *  <li>If you specify a texture format that supports alpha, but the .basis file doesn't have alpha, the transcoder will automatically output a fully opaque (255) alpha channel.</li>
 *  <li>The PVRTC1 texture formats only support power of 2 dimension .basis files, but this may be relaxed in a future version.</li>
 *  <li>The PVRTC1 transcoders are real-time encoders, so don't expect the highest quality. We may add a slower encoder with improved quality.</li>
 *  <li>These enums must be kept in sync with Javascript code that calls the transcoder.</li>
 * </ul>
 */
public class BasisuFileInfo {
    int version;
    int totalHeaderSize;

    int totalSelectors;
    int selectorCodebookSize;

    int totalEndpoints;
    int endpointCodebookSize;

    int tablesSize;
    int slicesSize;

    BasisuTextureType textureType;
    int usPerFrame;

    int totalImages;
    int[] imageMipmapLevels;

    int userdata0;
    int userdata1;

    BasisuTextureFormat textureFormat;

    boolean flippedY;
    boolean etc1s;
    boolean hasAlphaSlices;

    public int getVersion() {
        return version;
    }

    public int getTotalHeaderSize() {
        return totalHeaderSize;
    }

    public int getTotalSelectors() {
        return totalSelectors;
    }

    public int getSelectorCodebookSize() {
        return selectorCodebookSize;
    }

    public int getTotalEndpoints() {
        return totalEndpoints;
    }

    public int getEndpointCodebookSize() {
        return endpointCodebookSize;
    }

    public int getTablesSize() {
        return tablesSize;
    }

    public int getSlicesSize() {
        return slicesSize;
    }

    public BasisuTextureType getTextureType() {
        return textureType;
    }

    public int getUsPerFrame() {
        return usPerFrame;
    }

    /** Total number of images. */
    public int getTotalImages() {
        return totalImages;
    }

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        return imageMipmapLevels;
    }

    public int getUserdata0() {
        return userdata0;
    }

    public int getUserdata1() {
        return userdata1;
    }

    public BasisuTextureFormat getTextureFormat() {
        return textureFormat;
    }

    /** True if the image was Y flipped. */
    public boolean isFlippedY() {
        return flippedY;
    }

    /** True if the file is ETC1. */
    public boolean isEtc1s() {
        return etc1s;
    }

    /** True if the texture has alpha slices (for ETC1S: even slices RGB, odd slices alpha). */
    public boolean hasAlphaSlices() {
        return hasAlphaSlices;
    }
}
