package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.crashinvaders.basisu.wrapper.BasisuTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;
import com.crashinvaders.basisu.wrapper.Ktx2ImageLevelInfo;

import java.nio.ByteBuffer;

/**
 * A simple wrapper to load and work with the KTX2 texture file data.
 * Must be disposed when no longer needed.
 */
public class Ktx2Data implements Disposable {

    private final ByteBuffer encodedData;

    /**
     * Keeps track of all the image info instances created by this object
     * and calls "#close()" for them when disposed itself.
     * Maps imageIndex > mipmapLevel > imageLevelInfo
     */
    private final IntMap<IntMap<Ktx2ImageLevelInfo>> imageInfoIndex = new IntMap<>();

    /**
     * @param file the file to load the KTX2 texture data from.
     */
    public Ktx2Data(FileHandle file) {
        this(BasisuGdxUtils.readFileIntoBuffer(file));
    }

    /**
     * @param encodedData the raw KTX2 texture data (as it's loaded from the file)
     */
    public Ktx2Data(ByteBuffer encodedData) {
        BasisuNativeLibLoader.loadIfNeeded();

        this.encodedData = encodedData;

        // KTX2 codec doesn't provide a simple validation method.
        // We assume we're good if we can parse the header and read "anything" out of it.
        if (BasisuWrapper.ktx2GetTotalLayers(encodedData) < 0) {
            throw new BasisuGdxException("Cannot validate header of KTX2 data.");
        }
    }

    @Override
    public void dispose() {
        for (IntMap<Ktx2ImageLevelInfo> mipmapLevelMap : imageInfoIndex.values()) {
            for (Ktx2ImageLevelInfo value : mipmapLevelMap.values()) {
                value.close();
            }
        }
        imageInfoIndex.clear();

        //TODO Replace with BufferUtils.newUnsafeByteBuffer(fileSize) once it's compatible with GWT compiler.
        if (BasisuBufferUtils.isUnsafeByteBuffer(encodedData)) {
            BasisuBufferUtils.disposeUnsafeByteBuffer(encodedData);
        }
    }

    public int getTotalLayers() {
        return BasisuWrapper.ktx2GetTotalLayers(encodedData);
    }

    public int getTotalMipmapLevels() {
        return BasisuWrapper.ktx2GetTotalMipmapLevels(encodedData);
    }

    public int getImageWidth() {
        return BasisuWrapper.ktx2GetImageWidth(encodedData);
    }

    public int getImageHeight() {
        return BasisuWrapper.ktx2GetImageHeight(encodedData);
    }

    public boolean hasAlpha() {
        return BasisuWrapper.ktx2HasAlpha(encodedData);
    }

    public BasisuTextureFormat getTextureFormat() {
        return BasisuWrapper.ktx2GetTextureFormat(encodedData);
    }

    /**
     * @return the raw KTX2 texture data (as it was loaded from the file)
     */
    public ByteBuffer getEncodedData() {
        return encodedData;
    }

    /**
     * Retrieves the image info data for the specified image number.
     * <br/>
     * NOTE: You don't have to call {@link Ktx2ImageLevelInfo#close()} as the returned instance is managed by the Ktx2Data.
     * @see Ktx2ImageLevelInfo
     * @param imageIndex the image index in the KTX2 file
     * @return the image info for the specified image index
     */
    public Ktx2ImageLevelInfo getImageLevelInfo(int imageIndex, int mipmapLevel) {
        IntMap<Ktx2ImageLevelInfo> mipmapLevelMap = imageInfoIndex.get(imageIndex);
        if (mipmapLevelMap == null) {
            mipmapLevelMap= new IntMap<>();
            imageInfoIndex.put(imageIndex, mipmapLevelMap);
        }
        Ktx2ImageLevelInfo imageInfo = mipmapLevelMap.get(imageIndex);
        if (imageInfo == null) {
            imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(encodedData, imageIndex, mipmapLevel);
            mipmapLevelMap.put(imageIndex, imageInfo);
        }
        return imageInfo;
    }

    /**
     * Transcodes the KTX2 image to the target texture format.
     * @param imageIndex the image index in the KTX2 file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate a KTX2 file).
     * @param textureFormat the target format to transcode to
     * @return the transcoded texture bytes.
     * Can be used for further processing or supplied directly to the OpenGL as compressed texture.
     */
    public ByteBuffer transcode(int imageIndex, int mipmapLevel, BasisuTranscoderTextureFormat textureFormat) {
        return BasisuWrapper.ktx2Transcode(encodedData, imageIndex, mipmapLevel, textureFormat);
    }
}
