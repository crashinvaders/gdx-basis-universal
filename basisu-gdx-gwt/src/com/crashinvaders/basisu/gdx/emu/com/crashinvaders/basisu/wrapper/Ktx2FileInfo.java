package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

public class Ktx2FileInfo implements Closeable {

    final JavaScriptObject fileInfoJs;

    Ktx2FileInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    Ktx2FileInfo(Object fileInfoJs) {
        this.fileInfoJs = (JavaScriptObject)fileInfoJs;
    }

    @Override
    public native void close() /*-{
        // Depends on if the JS object was mapped using "emscripten::class_" or "emscripten::value_object"
        // it might have or haven't the "delete" method.
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        if (data["delete"]) {
            data["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs = null;
    }-*/;

    public native int getTotalLayers() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.layers;
    }-*/;

    public native int getTotalMipmapLevels() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.mipmapLevels;
    }-*/;

    public native int getImageWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.width;
    }-*/;

    public native int getImageHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.height;
    }-*/;

    public native boolean hasAlpha() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.hasAlpha;
    }-*/;

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getTextureFormatNative();
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }
    native int getTextureFormatNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2FileInfo::fileInfoJs;
        return data.textureFormat.value;
    }-*/;
}
