package com.crashinvaders.basisu.wrapper;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class BasisuWrapperTest {

    private static final String IMAGE_BASIS_NAME = "kodim3.etc1s.basis";
    private static final String IMAGE_KTX2_NAME = "screen_stuff.uastc.ktx2";

    private static ByteBuffer imageBasisBuffer;
    private static ByteBuffer imageKtx2Buffer;

    @BeforeClass
    public static void init() throws IOException {
        new SharedLibraryLoader().load("gdx-basis-universal");

        System.out.println("Loading " + IMAGE_BASIS_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_BASIS_NAME)) {
            assert is != null;
            byte[] bytes = TestUtils.readToByteArray(is);
            imageBasisBuffer = TestUtils.asByteBuffer(bytes);
        }

        System.out.println("Loading " + IMAGE_KTX2_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_KTX2_NAME)) {
            assert is != null;
            byte[] bytes = TestUtils.readToByteArray(is);
            imageKtx2Buffer = TestUtils.asByteBuffer(bytes);
        }
    }

    /** Test if transcoder texture format check method works for all the Java declared formats. */
    @Test
    public void testSupportedTranscoderTextureFormats() {
        for (BasisuTextureFormat basisTextureFormat : BasisuTextureFormat.values()) {
            BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(basisTextureFormat);
        }
    }

    @Test
    public void testBasisValidateHeader() {
        assertTrue(BasisuWrapper.basisValidateHeader(imageBasisBuffer));
    }

    @Test
    public void testBasisGetFileInfo() {
        BasisuFileInfo fileInfo = BasisuWrapper.basisGetFileInfo(imageBasisBuffer);

        assertEquals(BasisuTextureType.REGULAR_2D, fileInfo.getTextureType());
        assertEquals(BasisuTextureFormat.ETC1S, fileInfo.getTextureFormat());
        assertEquals(19, fileInfo.getVersion());
        assertEquals(100, fileInfo.getTotalHeaderSize());
        assertEquals(14720, fileInfo.getTotalSelectors());
        assertEquals(27491, fileInfo.getSelectorCodebookSize());
        assertEquals(2584, fileInfo.getTotalEndpoints());
        assertEquals(4272, fileInfo.getEndpointCodebookSize());
        assertEquals(2051, fileInfo.getTablesSize());
        assertEquals(51390, fileInfo.getSlicesSize());
        assertEquals(0, fileInfo.getUsPerFrame());
        assertEquals(1, fileInfo.getTotalImages());
        assertEquals(1, fileInfo.getImageMipmapLevels().length);
        assertEquals(1, fileInfo.getImageMipmapLevels()[0]);
        assertEquals(0, fileInfo.getUserdata0());
        assertEquals(0, fileInfo.getUserdata1());
        assertFalse(fileInfo.isFlippedY());
        assertTrue(fileInfo.isEtc1s());
        assertFalse(fileInfo.hasAlphaSlices());
    }

    @Test
    public void testBasisGetImageInfo() {
        BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0);

        assertEquals(0, imageInfo.getImageIndex());
        assertEquals(1, imageInfo.getTotalLevels());
        assertEquals(768, imageInfo.getOrigWidth());
        assertEquals(512, imageInfo.getOrigHeight());
        assertEquals(768, imageInfo.getWidth());
        assertEquals(512, imageInfo.getHeight());
        assertEquals(192, imageInfo.getNumBlocksX());
        assertEquals(128, imageInfo.getNumBlocksY());
        assertEquals(24576, imageInfo.getTotalBlocks());
        assertEquals(0, imageInfo.getFirstSliceIndex());
        assertFalse(imageInfo.hasAlphaFlag());
        assertFalse(imageInfo.hasIframeFlag());
    }

    @Test
    public void testBasisTranscodeRgba32() {
        BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0);

        ByteBuffer rgba8888 = BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, BasisuTranscoderTextureFormat.RGBA32);

        // Check if encoding is correct.
        assertEquals(imageInfo.getWidth() * imageInfo.getHeight() * 4, rgba8888.capacity());

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, imageInfo.getWidth(), imageInfo.getHeight());
        TestUtils.saveImagePng(bufferedImage, IMAGE_BASIS_NAME + ".rgba32");
    }

    @Test
    public void testBasisTranscodeEtc2Rgba() {
        BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0);

        ByteBuffer etc2Rgba = BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

        // Check if encoding is correct.
        assertEquals(imageInfo.getTotalBlocks() * 16, etc2Rgba.capacity());

        TestUtils.saveFile(etc2Rgba, IMAGE_BASIS_NAME + ".etc2rgba");
    }

    /**
     * Transcode to all supported formats for quick stability check (the result texture data is not validated!).
     * This is a valid test as desktops should be able to transcode to any supported texture format
     * listed in BasisuTranscoderTextureFormat enum.
     */
    @Test
    public void testBasisTranscodeAll() {
        BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0);

        List<BasisuTranscoderTextureFormat> supportedFormats = new ArrayList<>(
                BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(BasisuTextureFormat.ETC1S));

        for (BasisuTranscoderTextureFormat format : supportedFormats) {
            if ((format == BasisuTranscoderTextureFormat.PVRTC1_4_RGB || format == BasisuTranscoderTextureFormat.PVRTC1_4_RGBA)
                    && !TestUtils.isSquareAndPowerOfTwo(imageInfo.getWidth(), imageInfo.getHeight())) {
                System.out.println("Format " + format + " requires the image to be square and has POW dimensions. Skipped...");
                continue;
            }
            System.out.println("Transcoding to " + format);
            BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, format);
        }
    }

    @Test
    public void testKtx2GetImageLevelInfo() {
        Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0);

        assertEquals(0, imageInfo.getLevelIndex());
        assertEquals(0, imageInfo.getLayerIndex());
        assertEquals(0, imageInfo.getFaceIndex());
        assertEquals(2048, imageInfo.getOrigWidth());
        assertEquals(2048, imageInfo.getOrigHeight());
        assertEquals(2048, imageInfo.getWidth());
        assertEquals(2048, imageInfo.getHeight());
        assertEquals(512, imageInfo.getNumBlocksX());
        assertEquals(512, imageInfo.getNumBlocksY());
        assertEquals(262144, imageInfo.getTotalBlocks());
        assertTrue(imageInfo.getAlphaFlag());
        assertFalse(imageInfo.getIframeFlag());
    }

    @Test
    public void testKtx2GetTotalLayers() {
        int totalLayers = BasisuWrapper.ktx2GetTotalLayers(imageKtx2Buffer);
        assertEquals(0, totalLayers);
    }

    @Test
    public void testKtx2GetTotalMipmapLevels() {
        int mipmapLayers = BasisuWrapper.ktx2GetTotalMipmapLevels(imageKtx2Buffer);
        assertEquals(1, mipmapLayers);
    }

    @Test
    public void testKtx2GetImageWidth() {
        int width = BasisuWrapper.ktx2GetImageWidth(imageKtx2Buffer);
        assertEquals(2048, width);
    }

    @Test
    public void testKtx2GetImageHeight() {
        int height = BasisuWrapper.ktx2GetImageHeight(imageKtx2Buffer);
        assertEquals(2048, height);
    }

    @Test
    public void testKtx2GetTextureFormat() {
        BasisuTextureFormat textureFormat = BasisuWrapper.ktx2GetTextureFormat(imageKtx2Buffer);
        assertEquals(BasisuTextureFormat.UASTC4x4, textureFormat);
    }

    @Test
    public void textKtx2HasAlphaNative() {
        boolean hasAlpha = BasisuWrapper.ktx2HasAlpha(imageKtx2Buffer);
        assertTrue(hasAlpha);
    }

    @Test
    public void testKtx2TranscodeEtc2Rgba() {
        Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0);

        ByteBuffer etc2Rgba = BasisuWrapper.ktx2Transcode(imageKtx2Buffer, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

        // Check if encoding is correct.
        assertEquals(imageInfo.getTotalBlocks() * 16, etc2Rgba.capacity());

        TestUtils.saveFile(etc2Rgba, IMAGE_KTX2_NAME + ".etc2rgba");
    }

    /**
     * Transcode to all supported formats for quick stability check (the result texture data is not validated!).
     * This is a valid test as desktops should be able to transcode to any supported texture format
     * listed in BasisuTranscoderTextureFormat enum.
     */
    @Test
    public void testKtx2TranscodeAll() {
        Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0);
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();

        List<BasisuTranscoderTextureFormat> supportedFormats = new ArrayList<>(
                BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(BasisuTextureFormat.UASTC4x4));

        for (BasisuTranscoderTextureFormat format : supportedFormats) {

            if ((format == BasisuTranscoderTextureFormat.PVRTC1_4_RGB || format == BasisuTranscoderTextureFormat.PVRTC1_4_RGBA)
                    && !TestUtils.isSquareAndPowerOfTwo(width, height)) {
                System.out.println("Format " + format + " requires the image to be square and has POW dimensions. Skipped...");
                continue;
            }
            System.out.println("Transcoding to " + format);
            BasisuWrapper.ktx2Transcode(imageKtx2Buffer, 0, 0, format);
        }
    }
}
