package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

public class BasisuFileInfo implements Closeable {

    final JavaScriptObject fileInfoJs;
    private final int[] imageMipmapLevels;

    BasisuFileInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    BasisuFileInfo(Object fileInfoJs, int[] imageMipmapLevels) {
        this.fileInfoJs = (JavaScriptObject)fileInfoJs;
        this.imageMipmapLevels = imageMipmapLevels;
    }

    @Override
    public native void close() /*-{
        // Depends on if the JS object was mapped using "emscripten::class_" or "emscripten::value_object"
        // it might have or haven't the "delete" method.
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        if (data["delete"]) {
            data["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs = null;
    }-*/;

    public BasisuTextureType getTextureType() {
        int textureTypeId = getTextureTypeNative();
        return findOrThrow(BasisuTextureType.values(), textureTypeId);
    }
    native int getTextureTypeNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.textureType.value;
    }-*/;

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getTextureFormatNative();
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }
    native int getTextureFormatNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.textureFormat.value;
    }-*/;

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        return imageMipmapLevels;
    }

    public native int getVersion() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.version;
    }-*/;

    public native int getTotalHeaderSize() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.totalHeaderSize;
    }-*/;

    public native int getTotalSelectors() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.totalSelectors;
    }-*/;

    public native int getSelectorCodebookSize() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.selectorCodebookSize;
    }-*/;

    public native int getTotalEndpoints() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.totalEndpoints;
    }-*/;

    public native int getEndpointCodebookSize() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.endpointCodebookSize;
    }-*/;

    public native int getTablesSize() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.tablesSize;
    }-*/;

    public native int getSlicesSize() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.slicesSize;
    }-*/;

    public native int getUsPerFrame() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.usPerFrame;
    }-*/;

    /** Total number of images. */
    public native int getTotalImages() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.totalImages;
    }-*/;

    public native int getUserdata0() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.userdata0;
    }-*/;

    public native int getUserdata1() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.userdata1;
    }-*/;

    public native boolean isFlippedY() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.yFlipped;
    }-*/;

    public native boolean isEtc1s() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.etc1s;
    }-*/;

    public native boolean hasAlphaSlices() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.hasAlphaSlices;
    }-*/;
}
