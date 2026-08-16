package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
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
        return new BasisuFileInfo(getFileInfoNative(handleJs));
    }
    private static native JavaScriptObject getFileInfoNative(JavaScriptObject handle) /*-{
        return handle.getFileInfo();
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
}
