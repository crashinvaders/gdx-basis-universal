package com.crashinvaders.basisu.wrapper;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BasisuWrapperTest {

    private static final String IMAGE_BASIS_NAME = "kodim3.etc1s.basis";
    private static final String IMAGE_BASIS_MIPMAP_NAME = "bananacat.mipmap.etc1s.basis";
    private static final String IMAGE_KTX2_NAME = "screen_stuff.uastc.ktx2";

    private static ByteBuffer imageBasisBuffer;
    private static ByteBuffer imageBasisMipmapBuffer;
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

        System.out.println("Loading " + IMAGE_BASIS_MIPMAP_NAME);
        try (InputStream is = BasisuWrapperTest.class.getClassLoader().getResourceAsStream(IMAGE_BASIS_MIPMAP_NAME)) {
            assert is != null;
            byte[] bytes = TestUtils.readToByteArray(is);
            imageBasisMipmapBuffer = TestUtils.asByteBuffer(bytes);
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
    public void testBasisValidateChecksum() {
        assertTrue(BasisuWrapper.basisValidateChecksum(imageBasisBuffer, true));
    }

    @Test
    public void testBasisGetFileInfo() {
        try (BasisuFileInfo fileInfo = BasisuWrapper.basisGetFileInfo(imageBasisBuffer)) {

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
    }

    @Test
    public void testBasisGetImageInfo() {
        try (BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0)) {

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
    }

    @Test
    public void testBasisGetImageLevelInfo() {
        try (BasisuImageLevelInfo levelInfo = BasisuWrapper.basisGetImageLevelInfo(imageBasisMipmapBuffer, 0, 4)) {

            assertEquals(0, levelInfo.getImageIndex());
            assertEquals(4, levelInfo.getLevelIndex());
            assertEquals(6, levelInfo.getOrigWidth());
            assertEquals(6, levelInfo.getOrigHeight());
            assertEquals(8, levelInfo.getWidth());
            assertEquals(8, levelInfo.getHeight());
            assertEquals(2, levelInfo.getNumBlocksX());
            assertEquals(2, levelInfo.getNumBlocksY());
            assertEquals(4, levelInfo.getTotalBlocks());
            assertEquals(4, levelInfo.getFirstSliceIndex());
            assertFalse(levelInfo.hasAlphaFlag());
            assertFalse(levelInfo.hasIframeFlag());
        }
    }

    @Test
    public void testBasisTranscodeRgba32() {
        try (BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0)) {

            ByteBuffer rgba8888 = BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, BasisuTranscoderTextureFormat.RGBA32);

            // Check if encoding is correct.
            assertEquals(imageInfo.getWidth() * imageInfo.getHeight() * 4, rgba8888.capacity());

            BufferedImage bufferedImage = TestUtils.fromRgba8888(rgba8888, imageInfo.getWidth(), imageInfo.getHeight());
            TestUtils.saveImagePng(bufferedImage, IMAGE_BASIS_NAME + ".rgba32");

            BasisuWrapper.disposeNativeBuffer(rgba8888);
        }
    }

    @Test
    public void testBasisTranscodeEtc2Rgba() {
        try (BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0)) {

            ByteBuffer etc2Rgba = BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

            // Check if encoding is correct.
            assertEquals(imageInfo.getTotalBlocks() * 16, etc2Rgba.capacity());

            TestUtils.saveFile(etc2Rgba, IMAGE_BASIS_NAME + ".etc2rgba");

            BasisuWrapper.disposeNativeBuffer(etc2Rgba);
        }
    }

    /**
     * Transcode to all supported formats for quick stability check (the result texture data is not validated!).
     * This is a valid test as desktops should be able to transcode to any supported texture format
     * listed in BasisuTranscoderTextureFormat enum.
     */
    @Test
    public void testBasisTranscodeAll() {
        try (BasisuImageInfo imageInfo = BasisuWrapper.basisGetImageInfo(imageBasisBuffer, 0)) {

            List<BasisuTranscoderTextureFormat> supportedFormats = new ArrayList<>(
                    BasisuTranscoderTextureFormatSupportIndex.getSupportedTextureFormats(BasisuTextureFormat.ETC1S));

            for (BasisuTranscoderTextureFormat format : supportedFormats) {
                if ((format == BasisuTranscoderTextureFormat.PVRTC1_4_RGB || format == BasisuTranscoderTextureFormat.PVRTC1_4_RGBA)
                        && !TestUtils.isSquareAndPowerOfTwo(imageInfo.getWidth(), imageInfo.getHeight())) {
                    System.out.println("Format " + format + " requires the image to be square and has POW dimensions. Skipped...");
                    continue;
                }
                System.out.println("Transcoding to " + format);
                ByteBuffer transcodedBuffer = BasisuWrapper.basisTranscode(imageBasisBuffer, 0, 0, format);
                BasisuWrapper.disposeNativeBuffer(transcodedBuffer);
            }
        }
    }

    @Test
    public void testKtx2GetFileInfo() {
        try (Ktx2FileInfo fileInfo = BasisuWrapper.ktx2GetFileInfo(imageKtx2Buffer)) {

            assertEquals(2048, fileInfo.getImageWidth());
            assertEquals(2048, fileInfo.getImageHeight());
            assertEquals(0, fileInfo.getTotalLayers());
            assertEquals(1, fileInfo.getTotalMipmapLevels());
            assertEquals(BasisuTextureFormat.UASTC4x4, fileInfo.getTextureFormat());
            assertTrue(fileInfo.hasAlpha());
        }
    }

    @Test
    public void testKtx2GetImageLevelInfo() {
        try (Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0)) {

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
    }

    @Test
    public void testKtx2TranscodeEtc2Rgba() {
        try (Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0)) {

            ByteBuffer etc2Rgba = BasisuWrapper.ktx2Transcode(imageKtx2Buffer, 0, 0, BasisuTranscoderTextureFormat.ETC2_RGBA);

            // Check if encoding is correct.
            assertEquals(imageInfo.getTotalBlocks() * 16, etc2Rgba.capacity());

            TestUtils.saveFile(etc2Rgba, IMAGE_KTX2_NAME + ".etc2rgba");

            BasisuWrapper.disposeNativeBuffer(etc2Rgba);
        }
    }

    /**
     * Transcode to all supported formats for quick stability check (the result texture data is not validated!).
     * This is a valid test as desktops should be able to transcode to any supported texture format
     * listed in BasisuTranscoderTextureFormat enum.
     */
    /**
     * Exercises {@link BasisuFileTranscoder} as a persistent session reused across every mipmap
     * level of the same image, instead of BasisuWrapper's stateless per-call methods.
     */
    @Test
    public void testBasisuFileTranscoderSession() {
        try (BasisuFileTranscoder fileTranscoder = new BasisuFileTranscoder(imageBasisMipmapBuffer)) {
            assertTrue(fileTranscoder.validateHeader());
            assertTrue(fileTranscoder.validateChecksum(true));

            try (BasisuFileInfo fileInfo = fileTranscoder.getFileInfo()) {
                int totalLevels = fileInfo.getImageMipmapLevels()[0];
                assertTrue(totalLevels > 1);

                for (int level = 0; level < totalLevels; level++) {
                    try (BasisuImageLevelInfo levelInfo = fileTranscoder.getImageLevelInfo(0, level)) {
                        ByteBuffer rgba8888 = fileTranscoder.transcode(0, level, BasisuTranscoderTextureFormat.RGBA32);
                        assertEquals(levelInfo.getOrigWidth() * levelInfo.getOrigHeight() * 4, rgba8888.capacity());
                        BasisuWrapper.disposeNativeBuffer(rgba8888);
                    }
                }
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testBasisuFileTranscoderDoubleClose() {
        BasisuFileTranscoder fileTranscoder = new BasisuFileTranscoder(imageBasisBuffer);
        fileTranscoder.close();
        fileTranscoder.close();
    }

    /**
     * Verifies that {@link BasisuFileTranscoder#transcodeAllLevels} produces byte-for-byte the
     * same output as transcoding each level individually via {@link BasisuFileTranscoder#transcode}.
     */
    @Test
    public void testBasisuFileTranscoderTranscodeAllLevels() {
        try (BasisuFileTranscoder fileTranscoder = new BasisuFileTranscoder(imageBasisMipmapBuffer)) {
            int totalLevels;
            try (BasisuFileInfo fileInfo = fileTranscoder.getFileInfo()) {
                totalLevels = fileInfo.getImageMipmapLevels()[0];
            }

            TranscodedMipChain mipChain = fileTranscoder.transcodeAllLevels(0, BasisuTranscoderTextureFormat.RGBA32);
            assertEquals(totalLevels, mipChain.getLevelCount());

            for (int level = 0; level < totalLevels; level++) {
                ByteBuffer expected = fileTranscoder.transcode(0, level, BasisuTranscoderTextureFormat.RGBA32);

                ByteBuffer actual = mipChain.data.duplicate();
                actual.position(mipChain.getLevelOffset(level));
                actual.limit(mipChain.getLevelOffset(level) + mipChain.getLevelSize(level));

                assertEquals(expected.capacity(), mipChain.getLevelSize(level));
                assertEquals(expected, actual);

                BasisuWrapper.disposeNativeBuffer(expected);
            }

            BasisuWrapper.disposeNativeBuffer(mipChain.data);
        }
    }

    /**
     * Exercises {@link Ktx2FileTranscoder} as a persistent session, mirroring
     * {@link #testBasisuFileTranscoderSession()}.
     */
    @Test
    public void testKtx2FileTranscoderSession() {
        try (Ktx2FileTranscoder fileTranscoder = new Ktx2FileTranscoder(imageKtx2Buffer)) {
            try (Ktx2FileInfo fileInfo = fileTranscoder.getFileInfo()) {
                int totalLevels = fileInfo.getTotalMipmapLevels();
                assertTrue(totalLevels >= 1);

                for (int level = 0; level < totalLevels; level++) {
                    try (Ktx2ImageLevelInfo levelInfo = fileTranscoder.getImageLevelInfo(0, level)) {
                        ByteBuffer etc2Rgba = fileTranscoder.transcode(0, level, BasisuTranscoderTextureFormat.ETC2_RGBA);
                        assertEquals(levelInfo.getTotalBlocks() * 16, etc2Rgba.capacity());
                        BasisuWrapper.disposeNativeBuffer(etc2Rgba);
                    }
                }
            }
        }
    }

    /**
     * Verifies that {@link Ktx2FileTranscoder#transcodeAllLevels} produces byte-for-byte the
     * same output as transcoding each level individually via {@link Ktx2FileTranscoder#transcode}.
     */
    @Test
    public void testKtx2FileTranscoderTranscodeAllLevels() {
        try (Ktx2FileTranscoder fileTranscoder = new Ktx2FileTranscoder(imageKtx2Buffer)) {
            int totalLevels;
            try (Ktx2FileInfo fileInfo = fileTranscoder.getFileInfo()) {
                totalLevels = fileInfo.getTotalMipmapLevels();
            }

            TranscodedMipChain mipChain = fileTranscoder.transcodeAllLevels(0, BasisuTranscoderTextureFormat.ETC2_RGBA);
            assertEquals(totalLevels, mipChain.getLevelCount());

            for (int level = 0; level < totalLevels; level++) {
                ByteBuffer expected = fileTranscoder.transcode(0, level, BasisuTranscoderTextureFormat.ETC2_RGBA);

                ByteBuffer actual = mipChain.data.duplicate();
                actual.position(mipChain.getLevelOffset(level));
                actual.limit(mipChain.getLevelOffset(level) + mipChain.getLevelSize(level));

                assertEquals(expected.capacity(), mipChain.getLevelSize(level));
                assertEquals(expected, actual);

                BasisuWrapper.disposeNativeBuffer(expected);
            }

            BasisuWrapper.disposeNativeBuffer(mipChain.data);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testKtx2FileTranscoderDoubleClose() {
        Ktx2FileTranscoder fileTranscoder = new Ktx2FileTranscoder(imageKtx2Buffer);
        fileTranscoder.close();
        fileTranscoder.close();
    }

    @Test
    public void testKtx2TranscodeAll() {
        try (Ktx2ImageLevelInfo imageInfo = BasisuWrapper.ktx2GetImageLevelInfo(imageKtx2Buffer, 0, 0)) {

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
                ByteBuffer transcodedBuffer = BasisuWrapper.ktx2Transcode(imageKtx2Buffer, 0, 0, format);
                BasisuWrapper.disposeNativeBuffer(transcodedBuffer);
            }
        }
    }
}
