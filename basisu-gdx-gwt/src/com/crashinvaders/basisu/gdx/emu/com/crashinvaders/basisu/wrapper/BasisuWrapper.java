package com.crashinvaders.basisu.wrapper;

public class BasisuWrapper {

    public static boolean validateHeader(byte[] data) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        return validateHeaderNative(data, data.length);
    }

    public static boolean validateChecksum(byte[] data, boolean fullValidation) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        return validateChecksumNative(data, data.length, fullValidation);
    }

    public static int getTotalMipMapLevels(byte[] data) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        return getTotalMipMapLevelsNative(data, data.length);
    }

    public static BasisuFileInfo getFileInfo(byte[] data) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        BasisuFileInfo fileInfo = new BasisuFileInfo();
//        getFileInfoNative(data, data.length, fileInfo.addr);
//        return fileInfo;
    }

    public static BasisuImageInfo getImageInfo(byte[] data, int imageIndex) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        BasisuImageInfo imageInfo = new BasisuImageInfo();
//        getImageInfoNative(data, data.length, imageIndex, imageInfo.addr);
//        return imageInfo;
    }

    public static byte[] transcode(byte[] data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        throw new UnsupportedOperationException("Not yet implemented for GWT");

//        int format = textureFormat.getId();
//        return transcodeNative(data, data.length, imageIndex, levelIndex, format);
    }
}
