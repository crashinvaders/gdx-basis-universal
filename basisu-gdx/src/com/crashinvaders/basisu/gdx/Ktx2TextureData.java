package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

import java.nio.ByteBuffer;

/**
 * Provides support for KTX2 texture data format for {@link com.badlogic.gdx.graphics.Texture}.
 * The implementation is based of {@link com.badlogic.gdx.graphics.glutils.ETC1TextureData}.
 * <p/>
 * The implementation uses {@link BasisuTextureFormatSelector} to determine
 * which texture format is preferable for the current platform.
 * The {@link BasisuTextureFormatSelector.Default} selector is used for all the instances
 * unless another one is specified through {@link #setTextureFormatSelector(BasisuTextureFormatSelector)}.
 * You can also override the default selector by updating the value of {@link BasisuTextureFormatSelector#defaultSelector}.
 */
public class Ktx2TextureData implements TextureData {
    private static final String TAG = Ktx2TextureData.class.getSimpleName();

    private BasisuTextureFormatSelector formatSelector = BasisuTextureFormatSelector.defaultSelector;

    private final FileHandle file;  // May be null.
    private final int mipmapLevel;

    private Ktx2Data ktx2Data;

    private ByteBuffer transcodedData = null;
    private BasisuTranscoderTextureFormat transcodeFormat = null;

    private int width = 0;
    private int height = 0;
    private boolean isPrepared = false;

    /**
     * @param file the file to load the KTX2 texture data from
     */
    public Ktx2TextureData(FileHandle file) {
        this(file, 0);
    }

    /**
     * @param file the file to load the KTX2 texture data from
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate a KTX2 file).
     */
    public Ktx2TextureData(FileHandle file, int mipmapLevel) {
        this.file = file;
        this.mipmapLevel = mipmapLevel;

        this.ktx2Data = null;
    }

    /**
     * @param ktx2Data the KTX2 texture data to transcode the texture from
     */
    public Ktx2TextureData(Ktx2Data ktx2Data) {
        this(ktx2Data, 0);
    }

    /**
     * @param ktx2Data the KTX2 texture data to transcode the texture from
     * @param mipmapLevel the mipmap level of the image
     *                    (mipmaps should be enabled by the Basis encoder when you generate ae KTX2 file).
     */
    public Ktx2TextureData(Ktx2Data ktx2Data, int mipmapLevel) {
        this.file = null;
        this.mipmapLevel = mipmapLevel;

        this.ktx2Data = ktx2Data;
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
        if (file == null && ktx2Data == null) throw new GdxRuntimeException("Can only load once from ktx2Data");
        if (file != null) {
            ktx2Data = new Ktx2Data(file);
        }

        int mipmapLevels = ktx2Data.getTotalMipmapLevels();
        if (mipmapLevel < 0 || mipmapLevel >= mipmapLevels) {
            throw new BasisuGdxException("mipmapLevel " + mipmapLevel + " exceeds " +
                    "the total number of mipmap levels (" + mipmapLevels + ") in the basis file.");
        }

        //TODO Find a way to get texture type from KTX2 files and add sanity check.
//        BasisuTextureType textureType = fileInfo.getTextureType();
//        if (textureType != BasisuTextureType.REGULAR_2D) {
//            throw new BasisuGdxException("textureType " + textureType + " is not supported at the moment. " +
//                    "Only BasisuTextureType.REGULAR_2D texture type is allowed.");
//        }

        width = ktx2Data.getImageWidth();
        height = ktx2Data.getImageHeight();

        transcodeFormat = formatSelector.resolveTextureFormat(ktx2Data);
        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoding to the " + transcodeFormat + " format");

        int layerIndex = 0; // We do not yet support multi-layer KTX2 formats.
        this.transcodedData = ktx2Data.transcode(layerIndex, mipmapLevel, transcodeFormat);

        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoded texture size: " + MathUtils.round(this.transcodedData.capacity() / 1024.0f) + "kB");

        ktx2Data.dispose();
        ktx2Data = null;
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
