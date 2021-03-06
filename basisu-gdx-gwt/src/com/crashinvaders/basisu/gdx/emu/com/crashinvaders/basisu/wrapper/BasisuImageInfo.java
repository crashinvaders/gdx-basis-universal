package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.Closeable;

public class BasisuImageInfo implements Closeable {

    final JavaScriptObject imageInfoJs;

    BasisuImageInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    BasisuImageInfo(Object imageInfoJs) {
        this.imageInfoJs = (JavaScriptObject)imageInfoJs;
    }

    @Override
    public native void close() /*-{
        // Depends on if the JS object was mapped using "emscripten::class_" or "emscripten::value_object"
        // it might have or haven't the "delete" method.
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        if (data["delete"]) {
            data["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs = null;
    }-*/;

    public native int getImageIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.imageIndex;
    }-*/;

    public native int getTotalLevels() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.totalLevels;
    }-*/;

    public native int getOrigWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.origWidth;
    }-*/;

    public native int getOrigHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.origHeight;
    }-*/;

    public native int getWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.width;
    }-*/;

    public native int getHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.height;
    }-*/;

    public native int getNumBlocksX() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.numBlocksX;
    }-*/;

    public native int getNumBlocksY() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.numBlocksY;
    }-*/;

    public native int getTotalBlocks() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.totalBlocks;
    }-*/;

    public native int getFirstSliceIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.firstSliceIndex;
    }-*/;

    public native boolean hasAlphaFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.alphaFlag;
    }-*/;

    public native boolean hasIframeFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageInfo::imageInfoJs;
        return data.iframeFlag;
    }-*/;
}
