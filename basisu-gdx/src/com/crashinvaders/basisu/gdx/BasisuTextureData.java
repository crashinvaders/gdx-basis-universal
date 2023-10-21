package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.basisu.wrapper.*;

import java.nio.ByteBuffer;

/**
 * Provides support for Basis texture data format for {@link com.badlogic.gdx.graphics.Texture}.
 * The implementation is based of {@link com.badlogic.gdx.graphics.glutils.ETC1TextureData}.
 * <p/>
 * The implementation uses {@link BasisuTextureFormatSelector} to determine
 * which texture format is preferable for the current platform.
 * The {@link com.crashinvaders.basisu.gdx.BasisuTextureFormatSelector.Default} selector is used for all the instances
 * unless another one is specified through {@link #setTextureFormatSelector(BasisuTextureFormatSelector)}.
 * You can also override the default selector by updating the value of {@link BasisuGdxUtils#defaultFormatSelector}.
 */
public class BasisuTextureData implements TextureData {
    private static final String TAG = BasisuTextureData.class.getSimpleName();

    private BasisuTextureFormatSelector formatSelector = BasisuGdxUtils.defaultFormatSelector;

    private final FileHandle file;  // May be null.
    private final int imageIndex;
    private final int mipmapLevel;

    private BasisuData basisuData;

    private ByteBuffer transcodedData = null;
    private BasisuTranscoderTextureFormat transcodeFormat = null;

    private int width = 0;
    private int height = 0;
    private boolean isPrepared = false;

    /**
     * @param file the file to load the Basis texture data from
     */
    public BasisuTextureData(FileHandle file) {
        this(file, 0, 0);
    }

    /**
     * @param file the file to load the Basis texture data from
     * @param imageIndex the image index in the Basis file
     */
    public BasisuTextureData(FileHandle file, int imageIndex) {
        this(file, imageIndex, 0);
    }

    /**
     * @param file the file to load the Basis texture data from
     * @param imageIndex the image index in the Basis file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate the Basis file).
     */
    public BasisuTextureData(FileHandle file, int imageIndex, int mipmapLevel) {
        this.file = file;
        this.imageIndex = imageIndex;
        this.mipmapLevel = mipmapLevel;

        this.basisuData = null;
    }

    /**
     * @param basisuData the Basis texture data to transcode the texture from
     */
    public BasisuTextureData(BasisuData basisuData) {
        this(basisuData, 0);
    }

    /**
     * @param basisuData the Basis texture data to transcode the texture from
     * @param imageIndex the image index in the Basis file
     */
    public BasisuTextureData(BasisuData basisuData, int imageIndex) {
        this(basisuData, imageIndex, 0);
    }

    /**
     * @param basisuData the Basis texture data to transcode the texture from
     * @param imageIndex the image index in the Basis file
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate the Basis file).
     */
    public BasisuTextureData(BasisuData basisuData, int imageIndex, int mipmapLevel) {
        this.file = null;
        this.imageIndex = imageIndex;
        this.mipmapLevel = mipmapLevel;

        this.basisuData = basisuData;
    }

    /**
     * @return the GPU compressed texture format selector to be used to select the format to transcode to
     */
    public BasisuTextureFormatSelector getTextureFormatSelector() {
        return formatSelector;
    }

    /**
     * @param formatSelector  the GPU compressed texture format selector to be used to select the format to transcode to
     */
    public void setTextureFormatSelector(BasisuTextureFormatSelector formatSelector) {
        this.formatSelector = formatSelector;
    }

    /**
     * Shortcut method to lock the transcoder to the specific texture format.
     * Use it carefully as there's no single texture format to be supported by all the platforms
     * (that's kinda the whole purpose of Basis Universal dynamic format selector...)
     */
    public void setTextureFormatSelector(BasisuTranscoderTextureFormat format) {
        this.formatSelector = new BasisuTextureFormatSelector.Fixed(format);
    }

    @Override
    public TextureDataType getType() {
        return TextureDataType.Custom;
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void prepare() {
        if (isPrepared) throw new GdxRuntimeException("Already prepared");
        if (file == null && basisuData == null) throw new GdxRuntimeException("Can only load once from BasisuData");
        if (file != null) {
            basisuData = new BasisuData(file);
        }

        BasisuFileInfo fileInfo = basisuData.getFileInfo();

        int totalImages = fileInfo.getTotalImages();
        if (imageIndex < 0 || imageIndex >= totalImages) {
            throw new BasisuGdxException("imageIndex " + imageIndex + " exceeds " +
                    "the total number of images (" + totalImages + ") in the basis file.");
        }

        int mipmapLevels = fileInfo.getImageMipmapLevels()[imageIndex];
        if (mipmapLevel < 0 || mipmapLevel >= mipmapLevels) {
            throw new BasisuGdxException("mipmapLevel " + mipmapLevel + " exceeds " +
                    "the total number of mipmap levels (" + mipmapLevels + ") in the basis file.");
        }

        BasisuTextureType textureType = fileInfo.getTextureType();
        if (textureType != BasisuTextureType.REGULAR_2D) {
            throw new BasisuGdxException("textureType " + textureType + " is not supported at the moment. " +
                    "Only BasisuTextureType.REGULAR_2D texture type is allowed.");
        }

        BasisuImageInfo imageInfo = basisuData.getImageInfo(imageIndex);
        width = imageInfo.getWidth();
        height = imageInfo.getHeight();

        transcodeFormat = formatSelector.resolveTextureFormat(basisuData, imageIndex);
        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoding to the " + transcodeFormat + " format");

        this.transcodedData = basisuData.transcode(imageIndex, mipmapLevel, transcodeFormat);

        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoded texture size: " + MathUtils.round(this.transcodedData.capacity() / 1024.0f) + "kB");

        basisuData.dispose();
        basisuData = null;
        isPrepared = true;
    }

    @Override
    public void consumeCustomData(int target) {
        if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");

        final int glInternalFormatCode = BasisuGdxUtils.toGlTextureFormat(transcodeFormat);

        if (transcodeFormat.isCompressedFormat()) {
            BasisuGdxGl.glCompressedTexImage2D(target, 0, glInternalFormatCode,
                    width, height, 0,
                    transcodedData.capacity(), transcodedData);
        } else {
            int textureType = BasisuGdxUtils.toUncompressedGlTextureType(transcodeFormat);
            Gdx.gl.glTexImage2D(target, 0, glInternalFormatCode,
                    width, height, 0,
                    glInternalFormatCode, textureType, transcodedData);
        }

        // Cleanup.
        BasisuWrapper.disposeNativeBuffer(transcodedData);
        transcodedData = null;
        transcodeFormat = null;

        isPrepared = false;
    }

    @Override
    public Pixmap consumePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap.");
    }

    @Override
    public boolean disposePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap.");
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Pixmap.Format getFormat() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    @Override
    public boolean useMipMaps() {
        return false;
    }

    @Override
    public boolean isManaged() {
        return true;
    }
}
