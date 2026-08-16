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
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.validateHeader();
    }-*/;

    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        return basisValidateChecksumNative(toTypedArray(data), fullValidation);
    }
    private static native boolean basisValidateChecksumNative(ArrayBufferView data, boolean fullValidation)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.validateChecksum(fullValidation);
    }-*/;

    public static ByteBuffer basisTranscode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = basisTranscodeNative(toTypedArray(data), imageIndex, levelIndex, textureFormat.getId());
        return fromTypedArray(array);
    }
    static native Uint8Array basisTranscodeNative(ArrayBufferView data, int imageIndex, int levelIndex, int textureFormat)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.transcode(imageIndex, levelIndex, textureFormat);
    }-*/;

    public static BasisuFileInfo basisGetFileInfo(Buffer data) {
        ArrayBufferView typedArray = toTypedArray(data);
        JavaScriptObject fileInfoJs = basisGetFileInfoNative(typedArray);
        Uint8Array mipmapLevelsArray = basisGetImageMipmapLevelsNative(typedArray);
        int[] imageMipmapLevels = new int[mipmapLevelsArray.length()];
        for (int i = 0; i < imageMipmapLevels.length; i++) {
            imageMipmapLevels[i] = mipmapLevelsArray.get(i);
        }
        return new BasisuFileInfo(fileInfoJs, imageMipmapLevels);
    }
    static native JavaScriptObject basisGetFileInfoNative(ArrayBufferView data)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getFileInfo();
    }-*/;
    static native Uint8Array basisGetImageMipmapLevelsNative(ArrayBufferView data)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getImageMipmapLevels();
    }-*/;

    public static BasisuImageInfo basisGetImageInfo(Buffer data, int imageIndex) {
        JavaScriptObject imageInfoJs = basisGetImageInfoNative(toTypedArray(data), imageIndex);
        return new BasisuImageInfo(imageInfoJs);
    }
    static native JavaScriptObject basisGetImageInfoNative(ArrayBufferView data, int imageIndex)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getImageInfo(imageIndex);
    }-*/;

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer data, int imageIndex, int imageLevel) {
        JavaScriptObject levelInfoJs = basisGetImageLevelInfoNative(toTypedArray(data), imageIndex, imageLevel);
        return new BasisuImageLevelInfo(levelInfoJs);
    }
    static native JavaScriptObject basisGetImageLevelInfoNative(ArrayBufferView data, int imageIndex, int imageLevel)/*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::basisFileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getImageLevelInfo(imageIndex, imageLevel);
    }-*/;

    public static Ktx2FileInfo ktx2GetFileInfo(Buffer data) {
        JavaScriptObject fileInfoJs = ktx2GetFileInfoNative(toTypedArray(data));
        return new Ktx2FileInfo(fileInfoJs);
    }
    static native JavaScriptObject ktx2GetFileInfoNative(ArrayBufferView data) /*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::ktx2FileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getFileInfo();
    }-*/;

    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer data, int layerIndex, int imageLevel) {
        JavaScriptObject imageLayerInfoJs = ktx2GetImageLevelInfoNative(toTypedArray(data), layerIndex, imageLevel);
        return new Ktx2ImageLevelInfo(imageLayerInfoJs);
    }
    static native JavaScriptObject ktx2GetImageLevelInfoNative(ArrayBufferView data, int layerIndex, int imageLevel) /*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::ktx2FileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.getImageLevelInfo(layerIndex, imageLevel);
    }-*/;

    public static ByteBuffer ktx2Transcode(Buffer data, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = ktx2TranscodeNative(toTypedArray(data), layerIndex, levelIndex, textureFormat.getId());
        return fromTypedArray(array);
    }
    static native Uint8Array ktx2TranscodeNative(ArrayBufferView data, int layerIndex, int levelIndex, int textureFormat) /*-{
        var file = @com.crashinvaders.basisu.wrapper.BasisuWrapper::ktx2FileOf(Lcom/google/gwt/typedarrays/shared/ArrayBufferView;)(data);
        return file.transcode(layerIndex, levelIndex, textureFormat);
    }-*/;

    public static void disposeNativeBuffer(ByteBuffer dataBuffer) {
        // JS array buffers are managed. We don't need to free them manually.
    }

    /**
     * Releases the native-side Wasm handle (and the copy of the data it holds) that was lazily
     * created for this buffer by {@link #basisFileOf} / {@link #ktx2FileOf}.
     * Must be called once the given buffer is no longer needed, otherwise the Wasm-side copy leaks.
     */
    public static void releaseEncodedData(Buffer data) {
        releaseEncodedDataNative(toTypedArray(data));
    }
    private static native void releaseEncodedDataNative(ArrayBufferView data) /*-{
        var basisFile = data.__basisuBasisFile;
        if (basisFile) {
            basisFile["delete"]();
            data.__basisuBasisFile = null;
        }
        var ktx2File = data.__basisuKtx2File;
        if (ktx2File) {
            ktx2File["delete"]();
            data.__basisuKtx2File = null;
        }
    }-*/;

    //region Emscripten module handle caching.
    // The wasm module has to copy the buffer contents into its own heap once (Wasm memory
    // is a separate address space from JS), so we cache that copy on the JS typed array itself
    // and reuse it for every subsequent call instead of re-uploading the whole buffer each time.
    private static native JavaScriptObject basisFileOf(ArrayBufferView data) /*-{
        var handle = data.__basisuBasisFile;
        if (!handle) {
            handle = new $wnd.basisuModule.BasisFileTranscoder(data);
            data.__basisuBasisFile = handle;
        }
        return handle;
    }-*/;

    private static native JavaScriptObject ktx2FileOf(ArrayBufferView data) /*-{
        var handle = data.__basisuKtx2File;
        if (!handle) {
            handle = new $wnd.basisuModule.Ktx2FileTranscoder(data);
            data.__basisuKtx2File = handle;
        }
        return handle;
    }-*/;
    //endregion

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
    static ArrayBufferView toTypedArray(Buffer data) {
        return ((HasArrayBufferView)data).getTypedArray();
    }

    static ByteBuffer fromTypedArray(Uint8Array array) {
        return BasisuGwtBufferUtil.createDirectByteBuffer(array.buffer());
    }
    //endregion
}
