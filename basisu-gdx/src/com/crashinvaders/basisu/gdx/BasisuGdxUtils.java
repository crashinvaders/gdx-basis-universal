package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

import java.nio.IntBuffer;

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
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_texture_compression_rgtc.txt">BC4, BC5 (RGTC)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/ARB/ARB_texture_compression_bptc.txt">BC7 (BPTC)</a>
 * </li>
 * <li>
 *     <a href="https://www.khronos.org/registry/OpenGL/extensions/KHR/KHR_texture_compression_astc_hdr.txt">ASTC</a>
 * </li>
 * </ul>
 */
public class BasisuGdxUtils {
//    public static final int GL_TEX_FORMAT_ETC1_RGB8 = 0x8d64;
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
                // Since OpenGL doesn't return ETC1 code upon GL_COMPRESSED_TEXTURE_FORMATS request,
                // we replace it with compatible ETC2_RGB8 format.
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
//            case PVRTC1_4_RGB:
//                return ;
//            case PVRTC1_4_RGBA:
//                return ;
            case ASTC_4x4_RGBA:
                return GL_TEX_ASTC_4X4_RGBA;
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

    public static void printCompressedTextureFormats() {
        IntBuffer buffer = BufferUtils.newIntBuffer(64);
        Gdx.gl.glGetIntegerv(GL20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, buffer);
        int formatAmount = buffer.get(0);
        if (buffer.capacity() < formatAmount) {
            buffer = BufferUtils.newIntBuffer(formatAmount);
        }
        Gdx.gl.glGetIntegerv(GL20.GL_COMPRESSED_TEXTURE_FORMATS, buffer);
        for (int i = 0; i < formatAmount; i++) {
            System.out.println("Compressed format: " + Integer.toHexString(buffer.get(i)));
        }
    }
}
