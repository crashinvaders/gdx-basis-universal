package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BasisuTextureData implements TextureData {
    private static final String TAG = BasisuTextureData.class.getSimpleName();

    public static BasisuTextureFormatSelector defaultFormatSelector = new BasisuTextureFormatSelector.Default();

    private BasisuTextureFormatSelector formatSelector = BasisuTextureData.defaultFormatSelector;

    private final FileHandle file;  // May be null.
    private final int imageIndex;
    private final int mipmapLevel;

    private BasisuData basisuData;

    private ByteBuffer transcodedData = null;
    private BasisuTranscoderTextureFormat transcodeFormat = null;

    private int width = 0;
    private int height = 0;
    private boolean isPrepared = false;

    public BasisuTextureData(FileHandle file) {
        this(file, 0, 0);
    }

    public BasisuTextureData(FileHandle file, int imageIndex) {
        this(file, imageIndex, 0);
    }

    public BasisuTextureData(FileHandle file, int imageIndex, int mipmapLevel) {
        this.file = file;
        this.imageIndex = imageIndex;
        this.mipmapLevel = mipmapLevel;

        this.basisuData = null;
    }

    public BasisuTextureData(BasisuData basisuData) {
        this(basisuData, 0);
    }

    public BasisuTextureData(BasisuData basisuData, int imageIndex) {
        this(basisuData, imageIndex, 0);
    }

    public BasisuTextureData(BasisuData basisuData, int imageIndex, int mipmapLevel) {
        this.file = null;
        this.imageIndex = imageIndex;
        this.mipmapLevel = mipmapLevel;

        this.basisuData = basisuData;
    }

    public BasisuTextureFormatSelector getFormatSelector() {
        return formatSelector;
    }

    public void setFormatSelector(BasisuTextureFormatSelector formatSelector) {
        this.formatSelector = formatSelector;
    }

    public void setFormatSelector(BasisuTranscoderTextureFormat format) {
        this.formatSelector = (data, imageInfo) -> format;
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

        int totalImages = basisuData.getFileInfo().getTotalImages();
        if (imageIndex < 0 || imageIndex >= totalImages) {
            throw new IllegalStateException("imageIndex " + imageIndex + " exceeds " +
                    "the total number of images (" + totalImages + ") in the basis file.");
        }

        int mipmapLevels = basisuData.getFileInfo().getImageMipmapLevels()[imageIndex];
        if (mipmapLevel < 0 || mipmapLevel >= mipmapLevels) {
            throw new IllegalStateException("mipmapLevel " + mipmapLevel + " exceeds " +
                    "the total number of mipmap levels (" + mipmapLevels + ") in the basis file.");
        }

        BasisuImageInfo imageInfo = basisuData.getImageInfo(imageIndex);
        width = imageInfo.getWidth();
        height = imageInfo.getHeight();

        transcodeFormat = formatSelector.resolveTextureFormat(basisuData, imageInfo);
        Gdx.app.debug(TAG, "Transcoding " + (file != null ? file.path() : "a texture") + " to the " + transcodeFormat + " format.");

        byte[] transcodedData = basisuData.transcode(imageIndex, mipmapLevel, transcodeFormat);
        ByteBuffer buffer = BufferUtils.newByteBuffer(transcodedData.length);
        buffer.put(transcodedData);
        ((Buffer)buffer).position(0);
        this.transcodedData = buffer;

        isPrepared = true;
    }

    @Override
    public void consumeCustomData(int target) {
        if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");

        final int glInternalFormatCode = BasisuGdxUtils.toGlTextureFormat(transcodeFormat);

        if (transcodeFormat.isCompressedFormat()) {
            Gdx.gl.glCompressedTexImage2D(target, 0, glInternalFormatCode,
                    width, height, 0,
                    transcodedData.capacity(), transcodedData);
        } else {
            int textureType = BasisuGdxUtils.toUncompressedGlTextureType(transcodeFormat);

            Gdx.gl.glTexImage2D(target, 0, glInternalFormatCode,
                    width, height, 0,
                    glInternalFormatCode, textureType, transcodedData);
        }

        // Cleanup.
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
