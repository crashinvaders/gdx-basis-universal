package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntSet;
import com.crashinvaders.basisu.wrapper.BasisuPlatform;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

/**
 * Various utility methods required for Basis Universal LibGDX port.
 * <p/>
 * <b>References for OpenGL extension names and texture format codes</b>
 * <ul>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/OES/OES_compressed_ETC1_RGB8_texture.txt">ETC1</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_ES3_compatibility.txt">ETC2</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/EXT/EXT_texture_compression_s3tc.txt">BC1, BC3 (S3TC, DXTn)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_texture_compression_rgtc.txt">BC4, BC5 (RGTC)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_texture_compression_bptc.txt">BC7 (BPTC)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/KHR/KHR_texture_compression_astc_hdr.txt">ASTC</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/AMD/AMD_compressed_ATC_texture.txt">ATC</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/3DFX/3DFX_texture_compression_FXT1.txt">FXT1</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/IMG/IMG_texture_compression_pvrtc.txt">PVRTC1</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/IMG/IMG_texture_compression_pvrtc2.txt">PVRTC2</a>
 * </li>
 * </ul>
 */
public class BasisuGdxUtils {

    public static final int GL_TEX_ETC1_RGB8 = 0x8d64;
    public static final int GL_TEX_ETC2_RGB8 = 0x9274;
    public static final int GL_TEX_ETC2_RGBA8 = 0x9278;
    public static final int GL_TEX_ETC2_R11 = 0x9270;
    public static final int GL_TEX_ETC2_RG11 = 0x9272;
    public static final int GL_TEX_BC1_DXT1_RGB = 0x83f0;
    public static final int GL_TEX_BC3_DXT5_RGBA = 0x83f3;
    public static final int GL_TEX_BC4_RGTC1_RED = 0x8dbb;
    public static final int GL_TEX_BC5_RGTC2_RG = 0x8dbd;
    public static final int GL_TEX_BC7_BPTC_RGBA = 0x8e8c;
    public static final int GL_TEX_ASTC_4X4_RGBA = 0x93b0;
    public static final int GL_TEX_ATC_RGB = 0x8c92;
    public static final int GL_TEX_ATC_RGBA_INTERPOLATED = 0x87ee;
    public static final int GL_TEX_FXT1_RGB = 0x86b0;
    public static final int GL_TEX_PVRTC1_4BPP_RGB = 0x8c00;
    public static final int GL_TEX_PVRTC1_4BPP_RGBA = 0x8c02;
    public static final int GL_TEX_PVRTC2_4BPP_RGBA = 0x9138;

    private static final IntSet supportedGlTextureFormats = new IntSet();
    private static boolean supportedGlTextureFormatsInitialized = false;

    private static BasisuPlatform platform = null;

    public static BasisuPlatform getPlatform() {
        if (platform == null) {
            switch (Gdx.app.getType()) {
                case Android:
                    platform = BasisuPlatform.ANDROID;
                    break;
                case iOS:
                    platform = BasisuPlatform.IOS;
                case WebGL:
                    platform = BasisuPlatform.WEB;
                    break;
                case Desktop:
                    //FIXME As all the desktop targets have the same texture compatibility we simply use any of those as fallback for now.
                default:
                    // Fallback to Windows as it has the most diverse texture format support.
                    platform = BasisuPlatform.WINDOWS;
            }
        }
        return platform;
    }

    /**
     * Checks if the transcoder texture format is compatible with the current platform.
     * This is a not native texture support check, but rather test if Basis Universal
     * is able to transcode to the requested format.
     */
    public static boolean isPlatformCompatible(BasisuTranscoderTextureFormat basisuFormat) {
        BasisuPlatform platform = getPlatform();
        return platform.isCompatible(basisuFormat);
    }

    /**
     * Mapping is done according to the official Basis Universal
     * <a href="https://github.com/BinomialLLC/basis_universal/wiki/OpenGL-texture-format-enums-table">texture format table</a>.
     */
    public static int toGlTextureFormat(BasisuTranscoderTextureFormat basisuFormat) {
        switch (basisuFormat) {
            case ETC1_RGB:
                if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                    // Since OpenGL doesn't return ETC1 code upon GL_COMPRESSED_TEXTURE_FORMATS request,
                    // we replace it with the compatible ETC2_RGB8 format.
                    return GL_TEX_ETC2_RGB8;
                } else {
                    return GL_TEX_ETC1_RGB8;
                }
            case ETC2_RGBA:
                return GL_TEX_ETC2_RGBA8;
            case ETC2_EAC_R11:
                return GL_TEX_ETC2_R11;
            case ETC2_EAC_RG11:
                return GL_TEX_ETC2_RG11;
            case BC1_RGB:
                return GL_TEX_BC1_DXT1_RGB;
            case BC3_RGBA:
                return GL_TEX_BC3_DXT5_RGBA;
            case BC4_R:
                return GL_TEX_BC4_RGTC1_RED;
            case BC5_RG:
                return GL_TEX_BC5_RGTC2_RG;
            case BC7_RGBA:
                return GL_TEX_BC7_BPTC_RGBA;
            case ASTC_4x4_RGBA:
                return GL_TEX_ASTC_4X4_RGBA;
            case ATC_RGB:
                return GL_TEX_ATC_RGB;
            case ATC_RGBA:
                return GL_TEX_ATC_RGBA_INTERPOLATED;
//            case FXT1_RGB:
//                return GL_TEX_FXT1_RGB;
            case PVRTC1_4_RGB:
                return GL_TEX_PVRTC1_4BPP_RGB;
            case PVRTC1_4_RGBA:
                return GL_TEX_PVRTC1_4BPP_RGBA;
            case PVRTC2_4_RGB:
            case PVRTC2_4_RGBA:
                return GL_TEX_PVRTC2_4BPP_RGBA;
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

    public static IntSet getSupportedGlTextureFormats() {
        return supportedGlTextureFormats;
    }

    /**
     * Checks if the texture format can be transcoded to and if there's a native graphics API support for it as well.
     */
    public static boolean isBasisuFormatSupported(BasisuTranscoderTextureFormat textureFormat) {
        int glTextureFormat = toGlTextureFormat(textureFormat);
        return isGlTextureFormatSupported(glTextureFormat) && isPlatformCompatible(textureFormat);
    }

    public static boolean isGlTextureFormatSupported(int glTextureFormat) {
        initSupportedGlTextureFormats();
        return supportedGlTextureFormats.contains(glTextureFormat);
    }

    private static synchronized void initSupportedGlTextureFormats() {
        if (supportedGlTextureFormatsInitialized) return;
        supportedGlTextureFormatsInitialized = true;

        int[] formats = BasisuGdxGl.getSupportedTextureFormats();
        supportedGlTextureFormats.addAll(formats);
    }

    /**
     * Checks if the dimensions are equal and are power of two.
     */
    public static boolean isSquareAndPowerOfTwo(int width, int height) {
        return width == height && MathUtils.isPowerOfTwo(width);
    }

    /**
     * Checks if the dimensions are multiple of 4.
     */
    public static boolean isMultipleOfFour(int width, int height) {
        return width % 4 == 0 && height % 4 == 0;
    }
}
