package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
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

    private boolean useMipMaps = true;

    private BasisuData basisuData;

    /** Holds the width/height of each mipmap level.
     * Index of the array corresponds to the index of mipmap level. */
    private TranscodedLevelData[] transcodedLevels = null;
    /** Holds the transcoded bytes for every mipmap level, packed into a single buffer. */
    private TranscodedMipChain mipChain = null;
    private BasisuTranscoderTextureFormat transcodeFormat = null;

    private int width = 0;
    private int height = 0;
    private boolean isPrepared = false;

    /**
     * @param file the file to load the Basis texture data from
     */
    public BasisuTextureData(FileHandle file) {
        this(file, 0);
    }

    /**
     * @param file the file to load the Basis texture data from
     * @param imageIndex the image index in the Basis file
     */
    public BasisuTextureData(FileHandle file, int imageIndex) {
        this.file = file;
        this.imageIndex = imageIndex;

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
        this.file = null;
        this.imageIndex = imageIndex;

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

        BasisuTextureType textureType = fileInfo.getTextureType();
        if (textureType != BasisuTextureType.REGULAR_2D) {
            throw new BasisuGdxException("textureType " + textureType + " is not supported at the moment. " +
                    "Only BasisuTextureType.REGULAR_2D texture type is allowed.");
        }

        transcodeFormat = formatSelector.resolveTextureFormat(basisuData, imageIndex);
        Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoding to the " + transcodeFormat + " format");

        int transcodeLevels = 1;
        if (useMipMaps) {
            transcodeLevels = fileInfo.getImageMipmapLevels()[imageIndex];
        }
        // WebGL1 rejects any mip level beyond 0 on a non-power-of-two texture (desktop GL doesn't care).
        if (transcodeLevels > 1 && Gdx.app.getType() == Application.ApplicationType.WebGL) {
            BasisuImageLevelInfo level0Info = basisuData.getImageLevelInfo(imageIndex, 0);
            if (!MathUtils.isPowerOfTwo(level0Info.getOrigWidth()) || !MathUtils.isPowerOfTwo(level0Info.getOrigHeight())) {
                Gdx.app.error(TAG, (file != null ? "["+file.path()+"] " : "") +
                        "Non-power-of-two texture, WebGL1 can't use its mip chain - loading level 0 only.");
                transcodeLevels = 1;
                useMipMaps = false;
            }
        }
        transcodedLevels = new TranscodedLevelData[transcodeLevels];
        for (int level = 0; level < transcodeLevels; level++) {
            BasisuImageLevelInfo levelInfo = basisuData.getImageLevelInfo(imageIndex, level);
            transcodedLevels[level] = new TranscodedLevelData(level, levelInfo.getOrigWidth(), levelInfo.getOrigHeight());
        }

        mipChain = basisuData.transcodeAllLevels(imageIndex, transcodeLevels, transcodeFormat);
        for (int level = 0; level < transcodeLevels; level++) {
            TranscodedLevelData entry = transcodedLevels[level];
            Gdx.app.debug(TAG, (file != null ? "["+file.path()+"] " : "") + "Transcoded [mipmap:" + level + "] [size:" + entry.width + "x" + entry.height + "] [memory:" + MathUtils.round(mipChain.getLevelSize(level) / 1024.0f) + "kB]");
        }

        this.width = transcodedLevels[0].width;
        this.height = transcodedLevels[0].height;

        basisuData.dispose();
        basisuData = null;
        isPrepared = true;
    }

    @Override
    public void consumeCustomData(int target) {
        if (!isPrepared) throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");

        final int glFormatCode = BasisuGdxUtils.toGlTextureFormat(transcodeFormat);
        boolean isCompressedFormat = transcodeFormat.isCompressedFormat();

        for (int level = 0; level < transcodedLevels.length; level++) {
            TranscodedLevelData entry = transcodedLevels[level];

            ByteBuffer data = mipChain.data.duplicate();
            data.position(mipChain.getLevelOffset(level));
            data.limit(mipChain.getLevelOffset(level) + mipChain.getLevelSize(level));

            if (isCompressedFormat) {
                BasisuGdxGl.glCompressedTexImage2D(target, level, glFormatCode,
                        entry.width, entry.height, 0,
                        data.remaining(), data);
            } else {
                int textureType = BasisuGdxUtils.toUncompressedGlTextureType(transcodeFormat);
                Gdx.gl.glTexImage2D(target, level, glFormatCode,
                        entry.width, entry.height, 0,
                        glFormatCode, textureType, data);
            }

            int glError = Gdx.gl.glGetError();
            if (glError != 0) {
                Gdx.app.error(TAG, (file != null ? "["+file.path()+"] " : "") +
                        "Failed to upload texture (mimpap: " + level + ") to GPU. GL error: " + glError);
            }
        }

        // Cleanup.
        BasisuWrapper.disposeNativeBuffer(mipChain.data);
        mipChain = null;
        transcodedLevels = null;
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

    public void setUseMipMaps(boolean useMipMaps) {
        this.useMipMaps = useMipMaps;
    }

    @Override
    public boolean useMipMaps() {
        return useMipMaps;
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    private static class TranscodedLevelData {
        public final int levelIndex;
        public final int width;
        public final int height;

        public TranscodedLevelData(int levelIndex, int width, int height) {
            this.levelIndex = levelIndex;
            this.width = width;
            this.height = height;
        }
    }
}

