package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.files.FileHandle;
import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuWrapper;

public class BasisuData {
    private final byte[] encodedData;
    private final BasisuFileInfo fileInfo;

    public BasisuData(FileHandle fileHandle) {
        this(fileHandle.readBytes());
    }

    public BasisuData(byte[] encodedData) {
        BasisuNativeLibLoader.loadIfNeeded();

        this.encodedData = encodedData;

        if (!BasisuWrapper.validateHeader(encodedData)) {
            throw new BasisuGdxException("Cannot validate header of the basis universal data.");
        }

        this.fileInfo = BasisuWrapper.getFileInfo(encodedData);
    }

    public byte[] getEncodedData() {
        return encodedData;
    }

    public BasisuFileInfo getFileInfo() {
        return fileInfo;
    }

    public BasisuImageInfo getImageInfo(int imageIndex) {
        return BasisuWrapper.getImageInfo(encodedData, imageIndex);
    }

    public byte[] transcode(int imageIndex, int mipmapLevel, BasisuTranscoderTextureFormat textureFormat) {
        return BasisuWrapper.transcode(encodedData, imageIndex, mipmapLevel, textureFormat);
    }
}
