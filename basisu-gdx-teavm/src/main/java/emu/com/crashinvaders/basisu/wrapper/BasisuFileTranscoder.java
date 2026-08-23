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

public class BasisuFileTranscoder implements Closeable {

    private BasisuWrapper.BasisFileHandleJs handle;

    public BasisuFileTranscoder(Buffer dataBuffer) {
        this.handle = createNative(BasisuWrapper.toTypedArray(dataBuffer));
    }
    @JSBody(params = "data", script = "return new basisuModule.BasisFileTranscoder(data);")
    private static native BasisuWrapper.BasisFileHandleJs createNative(ArrayBufferView data);

    @Override
    public void close() {
        BasisuWrapper.deleteIfPresent(handle);
        handle = null;
    }

    public boolean validateHeader() { return handle.validateHeader(); }

    public boolean validateChecksum(boolean fullValidation) { return handle.validateChecksum(fullValidation); }

    public BasisuFileInfo getFileInfo() {
        BasisuWrapper.BasisuFileInfoJs fileInfoJs = handle.getFileInfo();
        Uint8Array mipmapLevelsArray = handle.getImageMipmapLevels();
        int[] imageMipmapLevels = new int[mipmapLevelsArray.getLength()];
        for (int i = 0; i < imageMipmapLevels.length; i++) {
            imageMipmapLevels[i] = mipmapLevelsArray.get(i);
        }
        return new BasisuFileInfo(fileInfoJs, imageMipmapLevels);
    }

    public BasisuImageInfo getImageInfo(int imageIndex) {
        return new BasisuImageInfo(handle.getImageInfo(imageIndex));
    }

    public BasisuImageLevelInfo getImageLevelInfo(int imageIndex, int imageLevel) {
        return new BasisuImageLevelInfo(handle.getImageLevelInfo(imageIndex, imageLevel));
    }

    public ByteBuffer transcode(int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        Uint8Array array = handle.transcode(imageIndex, levelIndex, textureFormat.getId());
        return BasisuWrapper.fromTypedArray(array);
    }

    /**
     * Decodes every mipmap level of the image, packed into a single buffer (see {@link TranscodedMipChain}).
     */
    public TranscodedMipChain transcodeAllLevels(int imageIndex, BasisuTranscoderTextureFormat textureFormat) {
        int totalLevels = getImageInfo(imageIndex).getTotalLevels();
        return transcodeAllLevels(imageIndex, totalLevels, textureFormat);
    }

    /**
     * Same as {@link #transcodeAllLevels(int, BasisuTranscoderTextureFormat)}, but only transcodes
     * the first "levelCount" mipmap levels, so a caller that doesn't need the full chain (e.g.
     * mipmaps disabled) doesn't pay for transcoding the rest of it.
     */
    public TranscodedMipChain transcodeAllLevels(int imageIndex, int levelCount, BasisuTranscoderTextureFormat textureFormat) {
        BasisuWrapper.TranscodeAllLevelsResultJs result = handle.transcodeAllLevels(imageIndex, levelCount, textureFormat.getId());
        Uint8Array dataArray = result.getData();
        Uint32Array offsetsArray = result.getLevelOffsets();

        int[] levelOffsets = new int[offsetsArray.getLength()];
        for (int i = 0; i < levelOffsets.length; i++) {
            levelOffsets[i] = (int) offsetsArray.get(i);
        }

        return new TranscodedMipChain(BasisuWrapper.fromTypedArray(dataArray), levelOffsets);
    }
}
