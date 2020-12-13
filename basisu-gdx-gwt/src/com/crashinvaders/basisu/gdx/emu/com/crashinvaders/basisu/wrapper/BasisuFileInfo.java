package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.Uint8Array;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

public class BasisuFileInfo {

    final JavaScriptObject fileInfoJs;

    BasisuFileInfo() {
        throw new UnsupportedOperationException("GWT doesn't support this constructor.");
    }

    BasisuFileInfo(Object fileInfoJs) {
        this.fileInfoJs = (JavaScriptObject)fileInfoJs;
    }

    public BasisuTextureType getTextureType() {
        int textureTypeId = getTextureTypeNative();
        return findOrThrow(BasisuTextureType.values(), textureTypeId);
    }
    native int getTextureTypeNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.getTexType();
    }-*/;

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getTextureFormatNative();
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }
    native int getTextureFormatNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.getTexFormat();
    }-*/;

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        Uint8Array typedArray = getImageMipmapLevelsNative();
        int[] result = new int[typedArray.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = typedArray.get(i);
        }
        return result;
    }
    native Uint8Array getImageMipmapLevelsNative() /*-{
        var data = this.@com.crashinvaders.basisu.wrapper.BasisuFileInfo::fileInfoJs;
        return data.getImageMipmapLevels();
//        var vec = data.imageMipmapLevels; // BasisuModule.Uint32Vector
//        var arr = new Uint8Array(vec.size());
//        for (var i = 0; i < arr.length; i++) {
//            arr[i] = vec.get(i);
//        }
//        $wnd.mipmapsArr = arr;
//        $wnd.mipmapsVec = vec;
//        return arr;
    }-*/;

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
