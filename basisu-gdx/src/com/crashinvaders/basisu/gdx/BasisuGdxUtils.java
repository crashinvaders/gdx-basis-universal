package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.StreamUtils;
import com.crashinvaders.basisu.wrapper.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Various utility methods required for Basis Universal libGDX library.
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

    /** Default texture format selector to be used by all the BasisuTextureData/KTX2TextureData instances. */
    public static BasisuTextureFormatSelector defaultFormatSelector = new BasisuTextureFormatSelector.Default();

    private static final IntSet supportedGlTextureFormats = new IntSet();
    private static boolean supportedGlTextureFormatsInitialized = false;

    /**
     * Checks if the transcoder texture format is compatible with the current platform.
     * This is a not native GPU texture support check, but rather test if Basis Universal
     * is able to transcode to the requested format.
     */
    public static boolean isTranscoderTextureFormatSupported(BasisuTranscoderTextureFormat transcoderTexFormat, BasisuTextureFormat basisTexFormat) {
        return BasisuTranscoderTextureFormatSupportIndex.isTextureFormatSupported(transcoderTexFormat, basisTexFormat);
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

    /**
     * Checks if the texture format can be transcoded to
     * and if there's a native graphics API support for it as well.
     */
    public static boolean isBasisuFormatSupported(BasisuTranscoderTextureFormat textureFormat,
                                                  BasisuTextureFormat basisTexFormat) {
        // The uncompressed formats are supported unconditionally.
        switch (textureFormat) {
            case RGBA32:
            case RGB565:
            case RGBA4444:
                return true;
        }

        int glTextureFormat = toGlTextureFormat(textureFormat);
        return isGlTextureFormatSupported(glTextureFormat) &&
                isTranscoderTextureFormatSupported(textureFormat, basisTexFormat);
    }

    /**
     * @return the list of GL texture formats supported by the GPU on the runtime.
     */
    public static IntSet getSupportedGlTextureFormats() {
        initSupportedGlTextureFormats();
        return supportedGlTextureFormats;
    }

    /**
     * Fetches and prepares the supported GL texture format list.
     * Must be called from the main LibGDX thread.
     */
    public static synchronized void initSupportedGlTextureFormats() {
        if (supportedGlTextureFormatsInitialized) return;
        supportedGlTextureFormatsInitialized = true;

        int[] formats = BasisuGdxGl.getSupportedTextureFormats();
        supportedGlTextureFormats.addAll(formats);
    }

    /**
     * Checks if the GL texture format is supported by the GPU on the runtime.
     */
    public static boolean isGlTextureFormatSupported(int glTextureFormat) {
        initSupportedGlTextureFormats();
        return supportedGlTextureFormats.contains(glTextureFormat);
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
                //TODO Replace with BufferUtils.newUnsafeByteBuffer(fileSize) once it's compatible with GWT compiler.
                byteBuffer = BasisuBufferUtils.newUnsafeByteBuffer(fileSize);
            }

            int readBytes = 0;
            while ((readBytes = in.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, readBytes);
            }
            ((Buffer)byteBuffer).position(0);
            ((Buffer)byteBuffer).limit(byteBuffer.capacity());
            return byteBuffer;
        } catch (Exception e) {
            throw new BasisuGdxException("Couldn't load file '" + file + "'", e);
        } finally {
            StreamUtils.closeQuietly(in);
        }
    }

    public static String reportAvailableTranscoderFormats(BasisuTextureFormat basisTexFormat) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== AVAILABLE TRANSCODER FORMATS | ").append(basisTexFormat.name()).append(" | (\"+\" if supported by the platform)").append(" =====");
        ArrayList<BasisuTranscoderTextureFormat> formats = new ArrayList<>(
                BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(basisTexFormat));
        Collections.sort(formats, (v0, v1) -> v0.ordinal() - v1.ordinal());
        for (BasisuTranscoderTextureFormat format : formats) {
            boolean glSupported = BasisuGdxUtils.isBasisuFormatSupported(format, basisTexFormat);
            sb.append("\n").append(glSupported ? "+ " : "  ").append(format);
        }
        return sb.toString();
    }
}
