package emu.com.crashinvaders.basisu.wrapper;

import com.crashinvaders.basisu.wrapper.BasisuTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapperException;
import com.github.xpenatan.gdx.teavm.backends.web.dom.typedarray.TypedArrays;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint32Array;
import org.teavm.jso.typedarrays.Uint8Array;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * TeaVM counterpart of {@code com.crashinvaders.basisu.wrapper.BasisuWrapper}, talking to the same
 * Emscripten/Wasm module (exposed as the global {@code basisuModule}) that the GWT backend uses.
 */
public class BasisuWrapper {

    public static boolean isTranscoderTexFormatSupported(BasisuTranscoderTextureFormat transcoderTexFormat, BasisuTextureFormat basisTexFormat) {
        return isTranscoderTexFormatSupportedNative(transcoderTexFormat.getId(), basisTexFormat.getId());
    }
    @JSBody(params = {"transcoderTexFormatId", "basisTexFormatId"}, script =
            "return basisuModule.isTranscoderTexFormatSupported(transcoderTexFormatId, basisTexFormatId);")
    private static native boolean isTranscoderTexFormatSupportedNative(int transcoderTexFormatId, int basisTexFormatId);

    public static boolean basisValidateHeader(Buffer data) {
        return basisFileOf(toTypedArray(data)).validateHeader();
    }

    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        return basisFileOf(toTypedArray(data)).validateChecksum(fullValidation);
    }

    public static ByteBuffer basisTranscode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = basisFileOf(toTypedArray(data)).transcode(imageIndex, levelIndex, textureFormat.getId());
        return fromTypedArray(array);
    }

    public static BasisuFileInfo basisGetFileInfo(Buffer data) {
        BasisFileHandleJs handle = basisFileOf(toTypedArray(data));
        BasisuFileInfoJs fileInfoJs = handle.getFileInfo();
        Uint8Array mipmapLevelsArray = handle.getImageMipmapLevels();
        int[] imageMipmapLevels = new int[mipmapLevelsArray.getLength()];
        for (int i = 0; i < imageMipmapLevels.length; i++) {
            imageMipmapLevels[i] = mipmapLevelsArray.get(i);
        }
        return new BasisuFileInfo(fileInfoJs, imageMipmapLevels);
    }

    public static BasisuImageInfo basisGetImageInfo(Buffer data, int imageIndex) {
        return new BasisuImageInfo(basisFileOf(toTypedArray(data)).getImageInfo(imageIndex));
    }

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer data, int imageIndex, int imageLevel) {
        return new BasisuImageLevelInfo(basisFileOf(toTypedArray(data)).getImageLevelInfo(imageIndex, imageLevel));
    }

    public static Ktx2FileInfo ktx2GetFileInfo(Buffer data) {
        return new Ktx2FileInfo(ktx2FileOf(toTypedArray(data)).getFileInfo());
    }

    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer data, int layerIndex, int imageLevel) {
        return new Ktx2ImageLevelInfo(ktx2FileOf(toTypedArray(data)).getImageLevelInfo(layerIndex, imageLevel));
    }

    public static ByteBuffer ktx2Transcode(Buffer data, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = ktx2FileOf(toTypedArray(data)).transcode(layerIndex, levelIndex, textureFormat.getId());
        return fromTypedArray(array);
    }

    public static void disposeNativeBuffer(ByteBuffer dataBuffer) {
        // JS array buffers are managed. We don't need to free them manually.
    }

    /**
     * Releases the native-side Wasm handle (and the copy of the data it holds) that was lazily
     * created for this buffer by {@link #basisFileOf} / {@link #ktx2FileOf}.
     * Must be called once the given buffer is no longer needed, otherwise the Wasm-side copy leaks.
     */
    public static void releaseEncodedData(Buffer data) {
        CacheHolder holder = (CacheHolder) toTypedArray(data);
        BasisFileHandleJs basisFile = holder.getCachedBasisFile();
        if (basisFile != null) {
            deleteIfPresent(basisFile);
            holder.setCachedBasisFile(null);
        }
        Ktx2FileHandleJs ktx2File = holder.getCachedKtx2File();
        if (ktx2File != null) {
            deleteIfPresent(ktx2File);
            holder.setCachedKtx2File(null);
        }
    }

    //region Emscripten module handle caching.
    // The wasm module has to copy the buffer contents into its own heap once (Wasm memory
    // is a separate address space from JS), so we cache that copy on the JS typed array itself
    // and reuse it for every subsequent call instead of re-uploading the whole buffer each time.
    static BasisFileHandleJs basisFileOf(ArrayBufferView data) {
        CacheHolder holder = (CacheHolder) data;
        BasisFileHandleJs handle = holder.getCachedBasisFile();
        if (isNullOrUndefined(handle)) {
            handle = newBasisFileHandle(data);
            holder.setCachedBasisFile(handle);
        }
        return handle;
    }
    @JSBody(params = "data", script = "return new basisuModule.BasisFileTranscoder(data);")
    private static native BasisFileHandleJs newBasisFileHandle(ArrayBufferView data);

    static Ktx2FileHandleJs ktx2FileOf(ArrayBufferView data) {
        CacheHolder holder = (CacheHolder) data;
        Ktx2FileHandleJs handle = holder.getCachedKtx2File();
        if (isNullOrUndefined(handle)) {
            handle = newKtx2FileHandle(data);
            holder.setCachedKtx2File(handle);
        }
        return handle;
    }
    @JSBody(params = "data", script = "return new basisuModule.Ktx2FileTranscoder(data);")
    private static native Ktx2FileHandleJs newKtx2FileHandle(ArrayBufferView data);

    /**
     * An unset {@code @JSProperty} on a plain JS object reads back as {@code undefined}, not
     * {@code null} - a Java {@code == null} check on the JSO-bridged value misses that case, so the
     * cache lookups above go through this instead of a plain null check.
     */
    @JSBody(params = "value", script = "return value === null || value === undefined;")
    private static native boolean isNullOrUndefined(JSObject value);

    interface CacheHolder extends JSObject {
        @JSProperty("__basisuBasisFile") BasisFileHandleJs getCachedBasisFile();
        @JSProperty("__basisuBasisFile") void setCachedBasisFile(BasisFileHandleJs handle);
        @JSProperty("__basisuKtx2File") Ktx2FileHandleJs getCachedKtx2File();
        @JSProperty("__basisuKtx2File") void setCachedKtx2File(Ktx2FileHandleJs handle);
    }
    //endregion

    //region Emscripten exception handler.
    static {
        setupNativeExceptionHandler(BasisuWrapper::throwBasisuException);
    }
    @JSFunctor
    interface ExceptionCallback extends JSObject {
        void onException(String message);
    }
    @JSBody(params = "handler", script = "window.basisuThrowException = handler;")
    private static native void setupNativeExceptionHandler(ExceptionCallback handler);
    private static void throwBasisuException(String message) {
        throw new BasisuWrapperException("Native code exception: " + message);
    }
    //endregion

    //region Shared utils.
    // UniqueIdUtils#findOrThrow is package-private to com.crashinvaders.basisu.wrapper and thus not
    // reachable from this (differently-named, pre-TeaVM-remap) "emu.com..." package - reimplemented here.
    static <T extends com.crashinvaders.basisu.wrapper.UniqueIdUtils.UniqueIdValue> T findOrThrow(T[] values, int id) {
        for (T value : values) {
            if (value.getId() == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("Cannot find an enum value with ID: " + id);
    }
    //endregion

    //region JS interop utils.
    static ArrayBufferView toTypedArray(Buffer data) {
        return TypedArrays.getUint8Array(data);
    }

    static ByteBuffer fromTypedArray(Uint8Array array) {
        return ByteBuffer.wrap(TypedArrays.toByteArray(array));
    }

    static void deleteIfPresent(JSObject handle) {
        deleteIfPresentNative(handle);
    }
    @JSBody(params = "handle", script = "if (handle && handle['delete']) { handle['delete'](); }")
    private static native void deleteIfPresentNative(JSObject handle);
    //endregion

    //region JS handle/value types shared with BasisuFileTranscoder/Ktx2FileTranscoder.
    interface EnumValueJs extends JSObject {
        @JSProperty int getValue();
    }

    interface BasisFileHandleJs extends JSObject {
        boolean validateHeader();
        boolean validateChecksum(boolean fullValidation);
        BasisuFileInfoJs getFileInfo();
        Uint8Array getImageMipmapLevels();
        BasisuImageInfoJs getImageInfo(int imageIndex);
        BasisuImageLevelInfoJs getImageLevelInfo(int imageIndex, int imageLevel);
        Uint8Array transcode(int imageIndex, int levelIndex, int textureFormat);
        TranscodeAllLevelsResultJs transcodeAllLevels(int imageIndex, int levelCount, int textureFormat);
    }

    interface Ktx2FileHandleJs extends JSObject {
        Ktx2FileInfoJs getFileInfo();
        Ktx2ImageLevelInfoJs getImageLevelInfo(int layerIndex, int imageLevel);
        Uint8Array transcode(int layerIndex, int levelIndex, int textureFormat);
        TranscodeAllLevelsResultJs transcodeAllLevels(int layerIndex, int levelCount, int textureFormat);
    }

    interface TranscodeAllLevelsResultJs extends JSObject {
        @JSProperty("data") Uint8Array getData();
        @JSProperty("levelOffsets") Uint32Array getLevelOffsets();
    }

    interface BasisuFileInfoJs extends JSObject {
        @JSProperty("version") int getVersion();
        @JSProperty("totalHeaderSize") int getTotalHeaderSize();
        @JSProperty("totalSelectors") int getTotalSelectors();
        @JSProperty("selectorCodebookSize") int getSelectorCodebookSize();
        @JSProperty("totalEndpoints") int getTotalEndpoints();
        @JSProperty("endpointCodebookSize") int getEndpointCodebookSize();
        @JSProperty("tablesSize") int getTablesSize();
        @JSProperty("slicesSize") int getSlicesSize();
        @JSProperty("usPerFrame") int getUsPerFrame();
        @JSProperty("totalImages") int getTotalImages();
        @JSProperty("userdata0") int getUserdata0();
        @JSProperty("userdata1") int getUserdata1();
        @JSProperty("yFlipped") boolean getYFlipped();
        @JSProperty("etc1s") boolean getEtc1s();
        @JSProperty("hasAlphaSlices") boolean getHasAlphaSlices();
        @JSProperty("textureType") EnumValueJs getTextureType();
        @JSProperty("textureFormat") EnumValueJs getTextureFormat();
    }

    interface BasisuImageInfoJs extends JSObject {
        @JSProperty("imageIndex") int getImageIndex();
        @JSProperty("totalLevels") int getTotalLevels();
        @JSProperty("origWidth") int getOrigWidth();
        @JSProperty("origHeight") int getOrigHeight();
        @JSProperty("width") int getWidth();
        @JSProperty("height") int getHeight();
        @JSProperty("numBlocksX") int getNumBlocksX();
        @JSProperty("numBlocksY") int getNumBlocksY();
        @JSProperty("totalBlocks") int getTotalBlocks();
        @JSProperty("firstSliceIndex") int getFirstSliceIndex();
        @JSProperty("alphaFlag") boolean getAlphaFlag();
        @JSProperty("iframeFlag") boolean getIframeFlag();
    }

    interface BasisuImageLevelInfoJs extends JSObject {
        @JSProperty("imageIndex") int getImageIndex();
        @JSProperty("levelIndex") int getLevelIndex();
        @JSProperty("origWidth") int getOrigWidth();
        @JSProperty("origHeight") int getOrigHeight();
        @JSProperty("width") int getWidth();
        @JSProperty("height") int getHeight();
        @JSProperty("numBlocksX") int getNumBlocksX();
        @JSProperty("numBlocksY") int getNumBlocksY();
        @JSProperty("totalBlocks") int getTotalBlocks();
        @JSProperty("firstSliceIndex") int getFirstSliceIndex();
        @JSProperty("alphaFlag") boolean getAlphaFlag();
        @JSProperty("iframeFlag") boolean getIframeFlag();
    }

    interface Ktx2FileInfoJs extends JSObject {
        @JSProperty("layers") int getLayers();
        @JSProperty("mipmapLevels") int getMipmapLevels();
        @JSProperty("width") int getWidth();
        @JSProperty("height") int getHeight();
        @JSProperty("hasAlpha") boolean getHasAlpha();
        @JSProperty("textureFormat") EnumValueJs getTextureFormat();
    }

    interface Ktx2ImageLevelInfoJs extends JSObject {
        @JSProperty("levelIndex") int getLevelIndex();
        @JSProperty("layerIndex") int getLayerIndex();
        @JSProperty("faceIndex") int getFaceIndex();
        @JSProperty("origWidth") int getOrigWidth();
        @JSProperty("origHeight") int getOrigHeight();
        @JSProperty("width") int getWidth();
        @JSProperty("height") int getHeight();
        @JSProperty("numBlocksX") int getNumBlocksX();
        @JSProperty("numBlocksY") int getNumBlocksY();
        @JSProperty("totalBlocks") int getTotalBlocks();
        @JSProperty("alphaFlag") boolean getAlphaFlag();
        @JSProperty("iframeFlag") boolean getIframeFlag();
    }
    //endregion
}
