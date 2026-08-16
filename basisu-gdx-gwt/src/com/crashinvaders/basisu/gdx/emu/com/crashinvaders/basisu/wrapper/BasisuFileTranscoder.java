package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Uint32Array;
import com.google.gwt.typedarrays.shared.Uint8Array;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BasisuFileTranscoder implements Closeable {

    private JavaScriptObject handleJs;

    public BasisuFileTranscoder(Buffer dataBuffer) {
        this.handleJs = createNative(BasisuWrapper.toTypedArray(dataBuffer));
    }
    private static native JavaScriptObject createNative(ArrayBufferView data) /*-{
        return new $wnd.basisuModule.BasisFileTranscoder(data);
    }-*/;

    BasisuFileTranscoder(Object ignored) {
        throw new UnsupportedOperationException("This constructor exists solely for GWT compilation compatibility.");
    }

    @Override
    public native void close() /*-{
        var handle = this.@com.crashinvaders.basisu.wrapper.BasisuFileTranscoder::handleJs;
        if (handle) {
            handle["delete"]();
        }
        this.@com.crashinvaders.basisu.wrapper.BasisuFileTranscoder::handleJs = null;
    }-*/;

    public boolean validateHeader() { return validateHeaderNative(handleJs); }
    private static native boolean validateHeaderNative(JavaScriptObject handle) /*-{
        return handle.validateHeader();
    }-*/;

    public boolean validateChecksum(boolean fullValidation) { return validateChecksumNative(handleJs, fullValidation); }
    private static native boolean validateChecksumNative(JavaScriptObject handle, boolean fullValidation) /*-{
        return handle.validateChecksum(fullValidation);
    }-*/;

    public BasisuFileInfo getFileInfo() {
        JavaScriptObject fileInfoJs = getFileInfoNative(handleJs);
        Uint8Array mipmapLevelsArray = getImageMipmapLevelsNative(handleJs);
        int[] imageMipmapLevels = new int[mipmapLevelsArray.length()];
        for (int i = 0; i < imageMipmapLevels.length; i++) {
            imageMipmapLevels[i] = mipmapLevelsArray.get(i);
        }
        return new BasisuFileInfo(fileInfoJs, imageMipmapLevels);
    }
    private static native JavaScriptObject getFileInfoNative(JavaScriptObject handle) /*-{
        return handle.getFileInfo();
    }-*/;
    private static native Uint8Array getImageMipmapLevelsNative(JavaScriptObject handle) /*-{
        return handle.getImageMipmapLevels();
    }-*/;

    public BasisuImageInfo getImageInfo(int imageIndex) {
        return new BasisuImageInfo(getImageInfoNative(handleJs, imageIndex));
    }
    private static native JavaScriptObject getImageInfoNative(JavaScriptObject handle, int imageIndex) /*-{
        return handle.getImageInfo(imageIndex);
    }-*/;

    public BasisuImageLevelInfo getImageLevelInfo(int imageIndex, int imageLevel) {
        return new BasisuImageLevelInfo(getImageLevelInfoNative(handleJs, imageIndex, imageLevel));
    }
    private static native JavaScriptObject getImageLevelInfoNative(JavaScriptObject handle, int imageIndex, int imageLevel) /*-{
        return handle.getImageLevelInfo(imageIndex, imageLevel);
    }-*/;

    public ByteBuffer transcode(int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = transcodeNative(handleJs, imageIndex, levelIndex, textureFormat.getId());
        return BasisuWrapper.fromTypedArray(array);
    }
    private static native Uint8Array transcodeNative(JavaScriptObject handle, int imageIndex, int levelIndex, int textureFormat) /*-{
        return handle.transcode(imageIndex, levelIndex, textureFormat);
    }-*/;

    /**
     * Decodes every mipmap level of the image, packed into a single buffer (see {@link TranscodedMipChain}).
     */
    public TranscodedMipChain transcodeAllLevels(int imageIndex, BasisuTranscoderTextureFormat textureFormat) {
        JavaScriptObject result = transcodeAllLevelsNative(handleJs, imageIndex, textureFormat.getId());
        Uint8Array dataArray = getResultDataNative(result);
        Uint32Array offsetsArray = getResultLevelOffsetsNative(result);

        int[] levelOffsets = new int[offsetsArray.length()];
        for (int i = 0; i < levelOffsets.length; i++) {
            levelOffsets[i] = (int) offsetsArray.get(i);
        }

        return new TranscodedMipChain(BasisuWrapper.fromTypedArray(dataArray), levelOffsets);
    }
    private static native JavaScriptObject transcodeAllLevelsNative(JavaScriptObject handle, int imageIndex, int textureFormat) /*-{
        return handle.transcodeAllLevels(imageIndex, textureFormat);
    }-*/;
    private static native Uint8Array getResultDataNative(JavaScriptObject result) /*-{
        return result.data;
    }-*/;
    private static native Uint32Array getResultLevelOffsetsNative(JavaScriptObject result) /*-{
        return result.levelOffsets;
    }-*/;
}
