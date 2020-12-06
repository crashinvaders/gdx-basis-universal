package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;

public class BasisuImageInfo {

    final JavaScriptObject imageInfoJs;

    public BasisuImageInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    public BasisuImageInfo(JavaScriptObject imageInfoJs) {
        this.imageInfoJs = imageInfoJs;
    }

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
