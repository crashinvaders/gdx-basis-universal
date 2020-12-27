package com.crashinvaders.basisu.wrapper;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class BasisuWrapperTest {

    private static final String IMAGE_FILE = "kodim3.basis";

    private static ByteBuffer basisBuffer;

    @BeforeClass
    public static void init() throws IOException {
        new SharedLibraryLoader().load("gdx-basis-universal");

        System.out.println("Loading " + IMAGE_FILE);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_FILE)) {
            byte[] bytes = TestUtils.readToByteArray(is);
            basisBuffer = TestUtils.asByteBuffer(bytes);
        }
    }

    @Test
    public void testValidateHeader() {
        assertTrue(BasisuWrapper.validateHeader(basisBuffer));
    }

    @Test
    public void testGetFileInfo() {
        BasisuFileInfo fileInfo = BasisuWrapper.getFileInfo(basisBuffer);

        assertEquals(fileInfo.getTextureType(), BasisuTextureType.REGULAR_2D);
        assertEquals(fileInfo.getTextureFormat(), BasisuTextureFormat.ETC1S);
        assertEquals(fileInfo.getVersion(), 19);
        assertEquals(fileInfo.getTotalHeaderSize(), 100);
        assertEquals(fileInfo.getTotalSelectors(), 14720);
        assertEquals(fileInfo.getSelectorCodebookSize(), 27491);
        assertEquals(fileInfo.getTotalEndpoints(), 2584);
        assertEquals(fileInfo.getEndpointCodebookSize(), 4272);
        assertEquals(fileInfo.getTablesSize(), 2051);
        assertEquals(fileInfo.getSlicesSize(), 51390);
        assertEquals(fileInfo.getUsPerFrame(), 0);
        assertEquals(fileInfo.getTotalImages(), 1);
        assertEquals(fileInfo.getImageMipmapLevels().length, 1);
        assertEquals(fileInfo.getImageMipmapLevels()[0], 1);
        assertEquals(fileInfo.getUserdata0(), 0);
        assertEquals(fileInfo.getUserdata1(), 0);
        assertFalse(fileInfo.isFlippedY());
        assertTrue(fileInfo.isEtc1s());
        assertFalse(fileInfo.hasAlphaSlices());
    }

    @Test
    public void testGetImageInfo() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBuffer, 0);

        assertEquals(imageInfo.getImageIndex(), 0);
        assertEquals(imageInfo.getTotalLevels(), 1);
        assertEquals(imageInfo.getOrigWidth(), 768);
        assertEquals(imageInfo.getOrigHeight(), 512);
        assertEquals(imageInfo.getWidth(), 768);
        assertEquals(imageInfo.getHeight(), 512);
        assertEquals(imageInfo.getNumBlocksX(), 192);
        assertEquals(imageInfo.getNumBlocksY(), 128);
        assertEquals(imageInfo.getTotalBlocks(), 24576);
        assertEquals(imageInfo.getFirstSliceIndex(), 0);
        assertFalse(imageInfo.hasAlphaFlag());
        assertFalse(imageInfo.hasIframeFlag());
    }

    @Test
    public void testTranscodeRgba32() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBuffer, 0);

        ByteBuffer rgba8888 = BasisuWrapper.transcode(basisBuffer, 0, 0, BasisuTranscoderTextureFormat.RGBA32);

        // Check if encoding is correct.
        assertEquals(rgba8888.capacity(), imageInfo.getWidth() * imageInfo.getHeight() * 4);

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, imageInfo.getWidth(), imageInfo.getHeight());
        TestUtils.saveImagePng(bufferedImage, IMAGE_FILE + ".rgba32");
    }

    @Test
    public void testTranscodeEtc2Rgba() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBuffer, 0);

        ByteBuffer etc2Rgba = BasisuWrapper.transcode(basisBuffer, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

        // Check if encoding is correct.
        assertEquals(etc2Rgba.capacity(), imageInfo.getTotalBlocks() * 16);

        TestUtils.saveFile(etc2Rgba, IMAGE_FILE + ".etc2rgba");
    }

    /**
     * Transcode to all supported formats for quick stability check (the result texture data is not validated!).
     * This is a valid test as desktops should be able to transcode to any supported texture format
     * listed in BasisuTranscoderTextureFormat enum.
     */
    @Test
    public void testTranscodeAll() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBuffer, 0);

        for (BasisuTranscoderTextureFormat format : BasisuTranscoderTextureFormat.values()) {
            if ((format == BasisuTranscoderTextureFormat.PVRTC1_4_RGB || format == BasisuTranscoderTextureFormat.PVRTC1_4_RGBA)
                    && !TestUtils.isSquareAndPowerOfTwo(imageInfo.getWidth(), imageInfo.getHeight())) {
                System.out.println("Format " + format + " requires the image to be square and has POW dimensions. Skipped...");
                continue;
            }
            System.out.println("Transcoding to " + format);
            BasisuWrapper.transcode(basisBuffer, 0, 0, format);
        }
    }

    /**
     * Test if transcoder texture format check method works for all the Java declared formats.
     */
    @Test
    public void testSupportedTranscoderTextureFormats() {
        for (BasisuTextureFormat basisTextureFormat : BasisuTextureFormat.values()) {
            BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(basisTextureFormat);
        }
    }
}
