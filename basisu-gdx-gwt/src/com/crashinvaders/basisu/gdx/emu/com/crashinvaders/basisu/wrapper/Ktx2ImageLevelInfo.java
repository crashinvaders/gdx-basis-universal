package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

import java.io.Closeable;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

public class Ktx2ImageLevelInfo implements Closeable {

    final JavaScriptObject imageLevelInfoJs;

    Ktx2ImageLevelInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    Ktx2ImageLevelInfo(Object imageLevelInfoJs) {
        this.imageLevelInfoJs = (JavaScriptObject)imageLevelInfoJs;
    }

    @Override
    public native void close() /*-{
        // Depends on if the JS object was mapped using "emscripten::class_" or "emscripten::value_object"
        // it might have or haven't the "delete" method.
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        if (data["delete"]) {
            data["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs = null;
    }-*/;


    public native int getLevelIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.levelIndex;
    }-*/;

    public native int getLayerIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.layerIndex;
    }-*/;

    public native int getFaceIndex() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.faceIndex;
    }-*/;

    public native int getOrigWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.origWidth;
    }-*/;

    public native int getOrigHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.origHeight;
    }-*/;

    public native int getWidth() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.width;
    }-*/;

    public native int getHeight() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.height;
    }-*/;

    public native int getNumBlocksX() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.numBlocksX;
    }-*/;

    public native int getNumBlocksY() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.numBlocksY;
    }-*/;

    public native int getTotalBlocks() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.totalBlocks;
    }-*/;

    public native boolean getAlphaFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.alphaFlag;
    }-*/;

    public native boolean getIframeFlag() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo::imageLevelInfoJs;
        return data.iframeFlag;
    }-*/;
}
