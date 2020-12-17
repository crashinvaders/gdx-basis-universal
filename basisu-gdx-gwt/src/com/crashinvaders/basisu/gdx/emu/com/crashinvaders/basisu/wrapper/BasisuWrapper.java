package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Uint8Array;

import java.nio.*;

public class BasisuWrapper {

    public static boolean validateHeader(Buffer data) {
        return validateHeaderNative(toTypedArray(data));
    }
    private static native boolean validateHeaderNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.validateHeader(data);
    }-*/;

    public static boolean validateChecksum(Buffer data, boolean fullValidation) {
        return validateChecksumNative(toTypedArray(data), fullValidation);
    }
    private static native boolean validateChecksumNative(ArrayBufferView data, boolean fullValidation)/*-{
        return $wnd.basisuModule.validateChecksum(data, fullValidation);
    }-*/;

    public static int getTotalMipMapLevels(Buffer data) {
        return getTotalMipMapLevelsNative(toTypedArray(data));
    }
    private static native int getTotalMipMapLevelsNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.getTotalMipMapLevels(data);
    }-*/;

    public static ByteBuffer transcode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = transcodeNative(toTypedArray(data), imageIndex, levelIndex, textureFormat.getId(), data);
        return fromTypedArray(array);
    }
    static native Uint8Array transcodeNative(ArrayBufferView data, int imageIndex, int levelIndex, int textureFormat, Object dataRaw)/*-{
        return $wnd.basisuModule.transcode(data, imageIndex, levelIndex, textureFormat);
    }-*/;

    public static BasisuFileInfo getFileInfo(Buffer data) {
        JavaScriptObject fileInfoJs = getFileInfoNative(toTypedArray(data));
        return new BasisuFileInfo(fileInfoJs);
    }
    static native JavaScriptObject getFileInfoNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.getFileInfo(data);
    }-*/;

    public static BasisuImageInfo getImageInfo(Buffer data, int imageIndex) {
        JavaScriptObject imageInfoJs = getImageInfoNative(toTypedArray(data), imageIndex);
        return new BasisuImageInfo(imageInfoJs);
    }
    static native JavaScriptObject getImageInfoNative(ArrayBufferView data, int imageIndex)/*-{
        return $wnd.basisuModule.getImageInfo(data, imageIndex);
    }-*/;


    //region JSNI utils.
    private static ArrayBufferView toTypedArray(Buffer data) {
        return ((HasArrayBufferView)data).getTypedArray();
    }

    private static ByteBuffer fromTypedArray(Uint8Array array) {
        return BasisuGwtBufferUtil.createDirectByteBuffer(array.buffer());
    }
    //endregion
}
