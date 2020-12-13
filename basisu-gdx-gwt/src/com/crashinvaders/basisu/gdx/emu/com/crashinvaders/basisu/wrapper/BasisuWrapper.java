package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Uint8Array;

import java.nio.Buffer;
import java.nio.HasArrayBufferView;

public class BasisuWrapper {

    public static native boolean validateHeader(byte[] data)/*-{
        var typedArray = new Uint8Array(data);
        return $wnd.basisuModule.validateHeader(typedArray);
    }-*/;

    public static native boolean validateChecksum(byte[] data, boolean fullValidation)/*-{
        var typedArray = new Uint8Array(data);
        return $wnd.basisuModule.validateChecksum(typedArray, fullValidation);
    }-*/;

    public static native int getTotalMipMapLevels(byte[] data)/*-{
        var typedArray = new Uint8Array(data);
        return $wnd.basisuModule.getTotalMipMapLevels(typedArray);
    }-*/;

    public static BasisuFileInfo getFileInfo(byte[] data) {
        JavaScriptObject fileInfoJs = getFileInfoNative(data);
        return new BasisuFileInfo(fileInfoJs);
    }
    static native JavaScriptObject getFileInfoNative(byte[] data)/*-{
        var typedArray = new Uint8Array(data);
        return $wnd.basisuModule.getFileInfo(typedArray);
    }-*/;

    public static BasisuImageInfo getImageInfo(byte[] data, int imageIndex) {
        JavaScriptObject imageInfoJs = getImageInfoNative(data, imageIndex);
        return new BasisuImageInfo(imageInfoJs);
    }
    static native JavaScriptObject getImageInfoNative(byte[] data, int imageIndex)/*-{
        var typedArray = new Uint8Array(data);
        return $wnd.basisuModule.getImageInfo(typedArray, imageIndex);
    }-*/;

    public static byte[] transcode(byte[] data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array typedArray = transcodeNative(data, imageIndex, levelIndex, textureFormat);
        byte[] bytes = new byte[typedArray.length()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) typedArray.get(i);
        }
        return bytes;
    }
    static native Uint8Array transcodeNative(byte[] data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat)/*-{
        var typedArray = new Uint8Array(data);
        var format = textureFormat.@com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat::getId()();
        return $wnd.basisuModule.transcode(typedArray, imageIndex, levelIndex, format);
    }-*/;

    public static boolean validateHeaderBuf(Buffer data, int dataSize) {
        return validateHeaderBufNative(toTypedArray(data));
    }
    private static native boolean validateHeaderBufNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.validateHeader(data);
    }-*/;

    private static ArrayBufferView toTypedArray(Buffer data) {
        return ((HasArrayBufferView)data).getTypedArray();
    }
}
