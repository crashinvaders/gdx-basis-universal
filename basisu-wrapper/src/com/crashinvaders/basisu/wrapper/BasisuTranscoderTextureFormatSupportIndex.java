package com.crashinvaders.basisu.wrapper;

import java.util.*;

/**
 * Keeps track of {@link BasisuTranscoderTextureFormat} support for the current platform.
 * <p/>
 * The sole purpose of this class is to cache the index of supported formats and avoid frequent calls to the native based
 * {@link BasisuWrapper#isTranscoderTexFormatSupported(BasisuTranscoderTextureFormat, BasisuTextureFormat)} method
 * everytime we need to check if the transcoder texture is supported.
 */
public class BasisuTranscoderTextureFormatSupportIndex {

    static final HashMap<BasisuTextureFormat, Set<BasisuTranscoderTextureFormat>> supportMap = new HashMap<>();

    /**
     * Checks weather the transcoder can transcode to the specified texture format.
     */
    public synchronized static boolean isTextureFormatSupported(BasisuTranscoderTextureFormat textureFormat, BasisuTextureFormat basisTexFormat) {
        return getSupportedTextureFormats(basisTexFormat).contains(textureFormat);
    }

    /**
     * Returns a list of the texture formats that the transcoder can transcode to.
     * Basis Universal library is compiled with some transcode tables excluded per platform to save up space.
     */
    public synchronized static Set<BasisuTranscoderTextureFormat> getSupportedTextureFormats(BasisuTextureFormat basisTexFormat) {
        Set<BasisuTranscoderTextureFormat> supportIndex = supportMap.get(basisTexFormat);
        if (supportIndex == null) {
            supportIndex = new HashSet<>();
            collectSupportedTextureFormats(basisTexFormat, supportIndex);
            supportMap.put(basisTexFormat, supportIndex);
        }
        return supportIndex;
    }

    private static void collectSupportedTextureFormats(BasisuTextureFormat basisTexFormat, Set<BasisuTranscoderTextureFormat> result) {
        for (BasisuTranscoderTextureFormat format : BasisuTranscoderTextureFormat.values()) {
            if (BasisuWrapper.isTranscoderTexFormatSupported(format, basisTexFormat)) {
                result.add(format);
            }
        }
    }
}
