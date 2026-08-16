package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Uint32Array;
import com.google.gwt.typedarrays.shared.Uint8Array;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Ktx2FileTranscoder implements Closeable {

    private JavaScriptObject handleJs;

    public Ktx2FileTranscoder(Buffer dataBuffer) {
        this.handleJs = createNative(BasisuWrapper.toTypedArray(dataBuffer));
    }
    private static native JavaScriptObject createNative(ArrayBufferView data) /*-{
        return new $wnd.basisuModule.Ktx2FileTranscoder(data);
    }-*/;

    Ktx2FileTranscoder(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public native void close() /*-{
        var handle = this.@com.crashinvaders.basisu.wrapper.Ktx2FileTranscoder::handleJs;
        if (handle) {
            handle["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.Ktx2FileTranscoder::handleJs = null;
    }-*/;

    public Ktx2FileInfo getFileInfo() {
        return new Ktx2FileInfo(getFileInfoNative(handleJs));
    }
    private static native JavaScriptObject getFileInfoNative(JavaScriptObject handle) /*-{
        return handle.getFileInfo();
    }-*/;

    public Ktx2ImageLevelInfo getImageLevelInfo(int layerIndex, int imageLevel) {
        return new Ktx2ImageLevelInfo(getImageLevelInfoNative(handleJs, layerIndex, imageLevel));
    }
    private static native JavaScriptObject getImageLevelInfoNative(JavaScriptObject handle, int layerIndex, int imageLevel) /*-{
        return handle.getImageLevelInfo(layerIndex, imageLevel);
    }-*/;

    public ByteBuffer transcode(int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = transcodeNative(handleJs, layerIndex, levelIndex, textureFormat.getId());
        return BasisuWrapper.fromTypedArray(array);
    }
    private static native Uint8Array transcodeNative(JavaScriptObject handle, int layerIndex, int levelIndex, int textureFormat) /*-{
        return handle.transcode(layerIndex, levelIndex, textureFormat);
    }-*/;

    /**
     * Decodes every mipmap level of the image, packed into a single buffer (see {@link TranscodedMipChain}).
     */
    public TranscodedMipChain transcodeAllLevels(int layerIndex, BasisuTranscoderTextureFormat textureFormat) {
        int totalLevels = getFileInfo().getTotalMipmapLevels();
        return transcodeAllLevels(layerIndex, totalLevels, textureFormat);
    }

    /**
     * Same as {@link #transcodeAllLevels(int, BasisuTranscoderTextureFormat)}, but only transcodes
     * the first "levelCount" mipmap levels, so a caller that doesn't need the full chain (e.g.
     * mipmaps disabled) doesn't pay for transcoding the rest of it.
     */
    public TranscodedMipChain transcodeAllLevels(int layerIndex, int levelCount, BasisuTranscoderTextureFormat textureFormat) {
        JavaScriptObject result = transcodeAllLevelsNative(handleJs, layerIndex, levelCount, textureFormat.getId());
        Uint8Array dataArray = getResultDataNative(result);
        Uint32Array offsetsArray = getResultLevelOffsetsNative(result);

        int[] levelOffsets = new int[offsetsArray.length()];
        for (int i = 0; i < levelOffsets.length; i++) {
            levelOffsets[i] = (int) offsetsArray.get(i);
        }

        return new TranscodedMipChain(BasisuWrapper.fromTypedArray(dataArray), levelOffsets);
    }
    private static native JavaScriptObject transcodeAllLevelsNative(JavaScriptObject handle, int layerIndex, int levelCount, int textureFormat) /*-{
        return handle.transcodeAllLevels(layerIndex, levelCount, textureFormat);
    }-*/;
    private static native Uint8Array getResultDataNative(JavaScriptObject result) /*-{
        return result.data;
    }-*/;
    private static native Uint32Array getResultLevelOffsetsNative(JavaScriptObject result) /*-{
        return result.levelOffsets;
    }-*/;
}
