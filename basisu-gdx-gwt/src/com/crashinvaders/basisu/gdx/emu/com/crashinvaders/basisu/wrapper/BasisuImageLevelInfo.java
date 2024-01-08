package com.crashinvaders.basisu.gdx.emu.com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.Closeable;

public class BasisuImageLevelInfo implements Closeable {

    final JavaScriptObject levelInfoJs;

    BasisuImageLevelInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    BasisuImageLevelInfo(Object levelInfoJs) {
        this.levelInfoJs = (JavaScriptObject)levelInfoJs;
    }

    @Override
    public native void close() /*-{
        // Depends on if the JS object was mapped using "emscripten::class_" or "emscripten::value_object"
        // it might have or haven't the "delete" method.
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        if (data["delete"]) {
            data["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs = null;
    }-*/;

    public native int getImageIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.imageIndex;
    }-*/;

    public native int getLevelIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.levelIndex;
    }-*/;

    public native int getOrigWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.origWidth;
    }-*/;

    public native int getOrigHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.origHeight;
    }-*/;

    public native int getWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.width;
    }-*/;

    public native int getHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.height;
    }-*/;

    public native int getNumBlocksX() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.numBlocksX;
    }-*/;

    public native int getNumBlocksY() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.numBlocksY;
    }-*/;

    public native int getTotalBlocks() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.totalBlocks;
    }-*/;

    public native int getFirstSliceIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.firstSliceIndex;
    }-*/;

    public native boolean hasAlphaFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.alphaFlag;
    }-*/;

    public native boolean hasIframeFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuImageLevelInfo::levelInfoJs;
        return data.iframeFlag;
    }-*/;
}
