package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
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
}
