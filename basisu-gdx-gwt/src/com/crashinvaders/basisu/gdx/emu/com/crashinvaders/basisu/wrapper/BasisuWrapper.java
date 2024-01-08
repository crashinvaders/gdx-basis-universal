package com.crashinvaders.basisu.wrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Uint8Array;

import java.nio.*;

public class BasisuWrapper {

    public static native boolean isTranscoderTexFormatSupported(BasisuTranscoderTextureFormat transcoderTexFormat, BasisuTextureFormat basisTexFormat)/*-{
        var transcoderTexFormatId = transcoderTexFormat.@com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat::getId()();
        var basisTexFormatId = basisTexFormat.@com.crashinvaders.basisu.wrapper.BasisuTextureFormat::getId()();
        return $wnd.basisuModule.isTranscoderTexFormatSupported(transcoderTexFormatId, basisTexFormatId);
    }-*/;

    public static boolean basisValidateHeader(Buffer data) {
        return basisValidateHeaderNative(toTypedArray(data));
    }
    private static native boolean basisValidateHeaderNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.basisValidateHeader(data);
    }-*/;

    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        return basisValidateChecksumNative(toTypedArray(data), fullValidation);
    }
    private static native boolean basisValidateChecksumNative(ArrayBufferView data, boolean fullValidation)/*-{
        return $wnd.basisuModule.basisValidateChecksum(data, fullValidation);
    }-*/;

    public static ByteBuffer basisTranscode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = basisTranscodeNative(toTypedArray(data), imageIndex, levelIndex, textureFormat.getId(), data);
        return fromTypedArray(array);
    }
    static native Uint8Array basisTranscodeNative(ArrayBufferView data, int imageIndex, int levelIndex, int textureFormat, Object dataRaw)/*-{
        return $wnd.basisuModule.basisTranscode(data, imageIndex, levelIndex, textureFormat);
    }-*/;

    public static BasisuFileInfo basisGetFileInfo(Buffer data) {
        JavaScriptObject fileInfoJs = basisGetFileInfoNative(toTypedArray(data));
        return new BasisuFileInfo(fileInfoJs);
    }
    static native JavaScriptObject basisGetFileInfoNative(ArrayBufferView data)/*-{
        return $wnd.basisuModule.basisGetFileInfo(data);
    }-*/;

    public static BasisuImageInfo basisGetImageInfo(Buffer data, int imageIndex) {
        JavaScriptObject imageInfoJs = basisGetImageInfoNative(toTypedArray(data), imageIndex);
        return new BasisuImageInfo(imageInfoJs);
    }
    static native JavaScriptObject basisGetImageInfoNative(ArrayBufferView data, int imageIndex)/*-{
        return $wnd.basisuModule.basisGetImageInfo(data, imageIndex);
    }-*/;

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer data, int imageIndex, int imageLevel) {
        JavaScriptObject levelInfoJs = basisGetImageLevelInfoNative(toTypedArray(data), imageIndex, imageLevel);
        return new BasisuImageLevelInfo(levelInfoJs);
    }
    static native JavaScriptObject basisGetImageLevelInfoNative(ArrayBufferView data, int imageIndex, int imageLevel)/*-{
        return $wnd.basisuModule.basisGetImageLevelInfo(data, imageIndex, imageLevel);
    }-*/;

    public static Ktx2FileInfo ktx2GetFileInfo(Buffer data) {
        JavaScriptObject fileInfoJs = ktx2GetFileInfoNative(toTypedArray(data));
        return new Ktx2FileInfo(fileInfoJs);
    }
    static native JavaScriptObject ktx2GetFileInfoNative(ArrayBufferView data) /*-{
        return $wnd.basisuModule.ktx2GetFileInfo(data);
    }-*/;

    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer data, int layerIndex, int imageLevel) {
        JavaScriptObject imageLayerInfoJs = ktx2GetImageLevelInfoNative(toTypedArray(data), layerIndex, imageLevel);
        return new Ktx2ImageLevelInfo(imageLayerInfoJs);
    }
    static native JavaScriptObject ktx2GetImageLevelInfoNative(ArrayBufferView data, int layerIndex, int imageLevel) /*-{
        return $wnd.basisuModule.ktx2GetImageLevelInfo(data, layerIndex, imageLevel);
    }-*/;

    public static ByteBuffer ktx2Transcode(Buffer data, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = ktx2TranscodeNative(toTypedArray(data), layerIndex, levelIndex, textureFormat.getId(), data);
        return fromTypedArray(array);
    }
    static native Uint8Array ktx2TranscodeNative(ArrayBufferView data, int layerIndex, int levelIndex, int textureFormat, Object dataRaw) /*-{
        return $wnd.basisuModule.ktx2Transcode(data, layerIndex, levelIndex, textureFormat);
    }-*/;

    public static void disposeNativeBuffer(ByteBuffer dataBuffer) {
        // JS array buffers are managed. We don't need to free them manually.
    }

    //region Emscripten exception handler.
    static {
        setupNativeExceptionHandler();
    }
    private static native void setupNativeExceptionHandler()/*-{
        $wnd.basisuThrowException = function(message) {
            @com.crashinvaders.basisu.wrapper.BasisuWrapper::throwBasisuException(Ljava/lang/String;)(message);
        }
        console.log("BasisuWrapper: Native code exception handler has been set.");
    }-*/;
    private static void throwBasisuException(String message) {
        throw new BasisuWrapperException("Native code exception: " + message);
    }
    //endregion


    //region JSNI utils.
    private static ArrayBufferView toTypedArray(Buffer data) {
        return ((HasArrayBufferView)data).getTypedArray();
    }

    private static ByteBuffer fromTypedArray(Uint8Array array) {
        return BasisuGwtBufferUtil.createDirectByteBuffer(array.buffer());
    }
    //endregion
}
