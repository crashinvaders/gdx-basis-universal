package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

/**
 * <b>References for OpenGL extension names and texture format codes</b>
 * <ul>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_ES3_compatibility.txt">ETC2</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/EXT/EXT_texture_compression_s3tc.txt">BC1, BC3 (S3TC, DXTn)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_texture_compression_bptc.txt">BC7 (BPTC)</a>
 * </li>
 * </ul>
 */
public class BasisuGdxUtils {

    private static final ObjectMap<String, Boolean> glExtensionCheckCache = new ObjectMap<>();

    public static boolean isGlExtensionSupported(String extension) {
        if (glExtensionCheckCache.containsKey(extension)) {
            return glExtensionCheckCache.get(extension);
        }
        boolean result = Gdx.graphics.supportsExtension(extension);
        glExtensionCheckCache.put(extension, result);
        return result;
    }

    //TODO Re-enable BasisuGdxUtilsTest#testToGlTextureFormat test once all the formats are mapped.
    public static int toGlTextureFormat(BasisuTranscoderTextureFormat basisuFormat) {
        switch (basisuFormat) {
            case ETC1_RGB:
                return 0x8d64;
            case ETC2_RGBA:
                return 0x9278;
            case BC1_RGB:
                return 0x83F0;
            case BC3_RGBA:
                return 0x83F3;
//            case BC4_R:
//                return ;
//            case BC5_RG:
//                return ;
            case BC7_RGBA:
                return 0x8e8c;
//            case PVRTC1_4_RGB:
//                return ;
//            case PVRTC1_4_RGBA:
//                return ;
//            case ASTC_4x4_RGBA:
//                return ;
//            case ATC_RGB:
//                return ;
//            case ATC_RGBA:
//                return ;
//            case FXT1_RGB:
//                return ;
//            case PVRTC2_4_RGB:
//                return ;
//            case PVRTC2_4_RGBA:
//                return ;
//            case ETC2_EAC_R11:
//                return 0x9270;
//            case ETC2_EAC_RG11:
//                return 0x9272;
            case RGB565:
                return GL20.GL_RGB;
            case RGBA32:
            case RGBA4444:
                return GL20.GL_RGBA;
            default:
                throw new BasisuGdxException("Unsupported basis texture format: " + basisuFormat);
        }
    }

    public static int toUncompressedGlTextureType(BasisuTranscoderTextureFormat basisuFormat) {
        if (basisuFormat.isCompressedFormat())
            throw new BasisuGdxException("The \"basisuFormat\" parameter is not an uncompressed texture format: " + basisuFormat);

        switch (basisuFormat) {
            case RGB565:
                return GL20.GL_UNSIGNED_SHORT_5_6_5;
            case RGBA32:
                return GL20.GL_UNSIGNED_BYTE;
            case RGBA4444:
                return GL20.GL_UNSIGNED_SHORT_4_4_4_4;
            default:
                throw new BasisuGdxException("Unexpected basis texture format: " + basisuFormat);

        }
    }
}
