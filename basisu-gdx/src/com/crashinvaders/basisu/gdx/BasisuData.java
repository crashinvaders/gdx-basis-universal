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

        //FIXME Replace this the commented code once BasisuFileInfo is fully nativally mapped.
//        this.fileInfo = BasisuWrapper.getFileInfo(encodedData);
        this.fileInfo = new BasisuFileInfoExtension();
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

    private static class BasisuFileInfoExtension extends BasisuFileInfo {
        @Override
        public int getTotalImages() {
            return 1;
        }
        @Override
        public int[] getImageMipmapLevels() {
            return new int[]{1};
        }
    }
}
