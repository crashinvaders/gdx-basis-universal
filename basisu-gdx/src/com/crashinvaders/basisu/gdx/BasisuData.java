package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BasisuData implements Disposable {
    private final ByteBuffer encodedData;
    private final BasisuFileInfo fileInfo;

    public BasisuData(FileHandle fileHandle) {
        this(readFileIntoBuffer(fileHandle));
    }

    public BasisuData(ByteBuffer encodedData) {
        BasisuNativeLibLoader.loadIfNeeded();

        this.encodedData = encodedData;

        if (!BasisuWrapper.validateHeader(encodedData)) {
            throw new BasisuGdxException("Cannot validate header of the basis universal data.");
        }

        this.fileInfo = BasisuWrapper.getFileInfo(encodedData);
    }

    @Override
    public void dispose() {
        fileInfo.close();
        if (BufferUtils.isUnsafeByteBuffer(encodedData)) {
            BufferUtils.disposeUnsafeByteBuffer(encodedData);
        }
    }

    public ByteBuffer getEncodedData() {
        return encodedData;
    }

    public BasisuFileInfo getFileInfo() {
        return fileInfo;
    }

    public BasisuImageInfo getImageInfo(int imageIndex) {
        return BasisuWrapper.getImageInfo(encodedData, imageIndex);
    }

    public ByteBuffer transcode(int imageIndex, int mipmapLevel, BasisuTranscoderTextureFormat textureFormat) {
        return BasisuWrapper.transcode(encodedData, imageIndex, mipmapLevel, textureFormat);
    }

    /**
     * Reads the file content into the {@link ByteBuffer}.
     * It uses unsafe (direct) byte buffer for all the platforms except for GWT,
     * so don't forget to free it using {@link BufferUtils#disposeUnsafeByteBuffer(ByteBuffer)}.
     */
    public static ByteBuffer readFileIntoBuffer(FileHandle file) {
        byte[] buffer = new byte[1024 * 10];
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(file.read()));
            int fileSize = (int)file.length();

            // We use unsafe (direct) byte buffer everywhere but not on GWT as it doesn't support it.
            final ByteBuffer byteBuffer;
            if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
                byteBuffer = BufferUtils.newByteBuffer(fileSize);
            } else {
                byteBuffer = BufferUtils.newUnsafeByteBuffer(fileSize);
            }

            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, readBytes);
            }
            ((Buffer)byteBuffer).position(0);
            ((Buffer)byteBuffer).limit(byteBuffer.capacity());
            return byteBuffer;
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't load file '" + file + "'", e);
        } finally {
            StreamUtils.closeQuietly(in);
        }
    }
}
