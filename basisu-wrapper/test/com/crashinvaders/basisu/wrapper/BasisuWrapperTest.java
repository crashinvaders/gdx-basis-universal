package com.crashinvaders.basisu.wrapper;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class BasisuWrapperTest {

    private static final String IMAGE_FILE = "kodim3.basis";

    private byte[] basisBytes;

    @Before
    public void init() throws IOException {
        new SharedLibraryLoader().load("gdx-basis-universal");

        System.out.println("Loading " + IMAGE_FILE);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_FILE)) {
            basisBytes = TestUtils.readToByteArray(is);
        }
    }

    @Test
    public void testValidateHeader() {
        assertTrue(BasisuWrapper.validateHeader(basisBytes));
    }

    @Test
    public void testGetImageInfo() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBytes, 0);

        assertEquals(imageInfo.imageIndex, 0);
        assertEquals(imageInfo.totalLevels, 1);
        assertEquals(imageInfo.origWidth, 768);
        assertEquals(imageInfo.origHeight, 512);
        assertEquals(imageInfo.width, 768);
        assertEquals(imageInfo.height, 512);
        assertEquals(imageInfo.numBlocksX, 192);
        assertEquals(imageInfo.numBlocksY, 128);
        assertEquals(imageInfo.totalBlocks, 24576);
        assertEquals(imageInfo.firstSliceIndex, 0);
        assertFalse(imageInfo.alphaFlag);
        assertFalse(imageInfo.iframeFlag);
    }

    @Test
    public void testTranscodeRgba32() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBytes, 0);

        byte[] rgba8888 = BasisuWrapper.transcode(basisBytes, 0, 0, BasisuTranscoderTextureFormat.RGBA32);

        // Check if encoding is correct.
        assertEquals(rgba8888.length, imageInfo.getWidth() * imageInfo.getHeight() * 4);

        BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, imageInfo.getWidth(), imageInfo.getHeight());
        TestUtils.saveImagePng(bufferedImage, IMAGE_FILE + ".rgba32");
    }

    @Test
    public void testTranscodeEtc2Rgba() {
        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBytes, 0);

        byte[] etc2Rgba = BasisuWrapper.transcode(basisBytes, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

        // Check if encoding is correct.
        assertEquals(etc2Rgba.length, imageInfo.totalBlocks * 16);

        TestUtils.saveFile(etc2Rgba, IMAGE_FILE + ".etc2rgba");
    }


}
