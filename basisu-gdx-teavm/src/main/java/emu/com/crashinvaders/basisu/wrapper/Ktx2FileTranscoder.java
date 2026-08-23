package emu.com.crashinvaders.basisu.wrapper;

import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.TranscodedMipChain;
import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint32Array;
import org.teavm.jso.typedarrays.Uint8Array;

import java.io.Closeable;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class Ktx2FileTranscoder implements Closeable {

    private BasisuWrapper.Ktx2FileHandleJs handle;

    public Ktx2FileTranscoder(Buffer dataBuffer) {
        this.handle = createNative(BasisuWrapper.toTypedArray(dataBuffer));
    }
    @JSBody(params = "data", script = "return new basisuModule.Ktx2FileTranscoder(data);")
    private static native BasisuWrapper.Ktx2FileHandleJs createNative(ArrayBufferView data);

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(handle);
        handle = null;
    }

    public Ktx2FileInfo getFileInfo() {
        return new Ktx2FileInfo(handle.getFileInfo());
    }

    public Ktx2ImageLevelInfo getImageLevelInfo(int layerIndex, int imageLevel) {
        return new Ktx2ImageLevelInfo(handle.getImageLevelInfo(layerIndex, imageLevel));
    }

    public ByteBuffer transcode(int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = handle.transcode(layerIndex, levelIndex, textureFormat.getId());
        return BasisuWrapper.fromTypedArray(array);
    }

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
        BasisuWrapper.TranscodeAllLevelsResultJs result = handle.transcodeAllLevels(layerIndex, levelCount, textureFormat.getId());
        Uint8Array dataArray = result.getData();
        Uint32Array offsetsArray = result.getLevelOffsets();

        int[] levelOffsets = new int[offsetsArray.getLength()];
        for (int i = 0; i < levelOffsets.length; i++) {
            levelOffsets[i] = (int) offsetsArray.get(i);
        }

        return new TranscodedMipChain(BasisuWrapper.fromTypedArray(dataArray), levelOffsets);
    }
}
