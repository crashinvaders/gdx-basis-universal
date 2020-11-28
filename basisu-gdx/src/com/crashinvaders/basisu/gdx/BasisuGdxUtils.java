package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntSet;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

import java.nio.IntBuffer;

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
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/AMD/AMD_compressed_ATC_texture.txt">ARC</a>
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
    public static final int GL_TEX_BC1_DXT1_RGB = 0x83F0;
    public static final int GL_TEX_BC3_DXT5_RGBA = 0x83F3;
    public static final int GL_TEX_BC4_RGTC1_RED = 0x8DBB;
    public static final int GL_TEX_BC5_RGTC2_RG = 0x8DBD;
    public static final int GL_TEX_BC7_BPTC_RGBA = 0x8E8C;
    public static final int GL_TEX_ASTC_4X4_RGBA = 0x93B0;
    public static final int GL_TEX_ARC_RGB = 0x8C92;
    public static final int GL_TEX_ARC_RGBA_INTERPOLATED = 0x87EE;
    public static final int GL_TEX_FXT1_RGB = 0x86B0;
    public static final int GL_TEX_PVRTC1_4BPP_RGB = 0x8C00;
    public static final int GL_TEX_PVRTC1_4BPP_RGBA = 0x8C02;
    public static final int GL_TEX_PVRTC2_4BPP_RGBA = 0x9138;

//    private static final ObjectMap<String, Boolean> glExtensionCheckCache = new ObjectMap<>();
    private static final IntSet supportedGlTextureFormats = new IntSet();
    private static boolean supportedGlTextureFormatsInitialized = false;

//    public static boolean isGlExtensionSupported(String extension) {
//        if (glExtensionCheckCache.containsKey(extension)) {
//            return glExtensionCheckCache.get(extension);
//        }
//        boolean result = Gdx.graphics.supportsExtension(extension);
//        glExtensionCheckCache.put(extension, result);
//        return result;
//    }

    /**
     * Mapping is done according to the official Basis Universal
     * <a href="https://github.com/BinomialLLC/basis_universal/wiki/OpenGL-texture-format-enums-table">texture format table</a>.
     */
    public static int toGlTextureFormat(BasisuTranscoderTextureFormat basisuFormat) {
        switch (basisuFormat) {
            case ETC1_RGB:
                // Since OpenGL doesn't return ETC1 code upon GL_COMPRESSED_TEXTURE_FORMATS request,
                // we replace it with the compatible ETC2_RGB8 format.
                return GL_TEX_ETC2_RGB8;
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
                return GL_TEX_ARC_RGB;
            case ATC_RGBA:
                return GL_TEX_ARC_RGBA_INTERPOLATED;
//            case FXT1_RGB:
//                return GL_TEX_FXT1_RGB;
//            case PVRTC1_4_RGB:
//                return GL_TEX_PVRTC1_4BPP_RGB;
//            case PVRTC1_4_RGBA:
//                return GL_TEX_PVRTC1_4BPP_RGBA;
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

    public static boolean isBasisuFormatSupported(BasisuTranscoderTextureFormat textureFormat) {
        int glTextureFormat = toGlTextureFormat(textureFormat);
        return isGlTextureFormatSupported(glTextureFormat);
    }

    public static boolean isGlTextureFormatSupported(int glTextureFormat) {
        initSupportedGlTextureFormats();
        return supportedGlTextureFormats.contains(glTextureFormat);
    }

    private static synchronized void initSupportedGlTextureFormats() {
        if (supportedGlTextureFormatsInitialized) return;

        supportedGlTextureFormatsInitialized = true;

        IntBuffer buffer = BufferUtils.newIntBuffer(64);
        Gdx.gl.glGetIntegerv(GL20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, buffer);
        int formatAmount = buffer.get(0);
        if (buffer.capacity() < formatAmount) {
            buffer = BufferUtils.newIntBuffer(formatAmount);
        }
        Gdx.gl.glGetIntegerv(GL20.GL_COMPRESSED_TEXTURE_FORMATS, buffer);
        for (int i = 0; i < formatAmount; i++) {
            int code = buffer.get(i);
            supportedGlTextureFormats.add(code);
//            Gdx.app.log("GL_TEX_FORMAT", Integer.toHexString(code));
        }
    }

    /**
     * Checks if the dimensions are equal and are power of two.
     */
    public static boolean isSquareAndPowerOfTwo(int width, int height) {
        return width == height && MathUtils.isPowerOfTwo(width);
    }
}
