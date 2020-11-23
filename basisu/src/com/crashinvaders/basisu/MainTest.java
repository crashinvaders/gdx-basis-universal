package com.crashinvaders.basisu;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class MainTest {

    /*JNI

    #include <iostream>

    */

    public static native int add(int a, int b); /*
		return a + b;
	*/

    private static native void printByteBuffer(Buffer buffer, int size); /*
        std::cout << "printByteBuffer " << size << std::endl;

        for (size_t i = 0; i < size; i++) {
            std::cout << (int)buffer[i] << " ";
        }
        std::cout << std::endl;
    */

    public static void main(String[] args) throws IOException {
//        new JniGenSharedLibraryLoader("my-native-lib-natives.jar").load("my-native-lib");
        new JniGenSharedLibraryLoader().load("gdx-basisu");

        System.out.println("Add: " + add(1, 2));

        byte[] basisBytes;
//        try (InputStream is = new FileInputStream("/home/metaphore/tmp/cosmocat_promo.basis")) {
        try (InputStream is = MainTest.class.getClassLoader().getResourceAsStream("kodim3.basis")) {
            basisBytes = readToByteArray(is);
        }

        System.out.println("Bytes read: " + basisBytes.length);

//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
//        byteBuffer.put(bytes, 0, bytes.length);

        if (!BasisuWrapper.validateHeader(basisBytes, basisBytes.length)) {
            throw new RuntimeException("Failed to validate basis image!");
        }

        System.out.println("Data was successfully validated!");

        BasisuImageInfo imageInfo = BasisuWrapper.getImageInfo(basisBytes, basisBytes.length, 0);
        System.out.println("Image size: " + imageInfo.width + "x" + imageInfo.height);

        byte[] rgba = BasisuWrapper.transcode(basisBytes, basisBytes.length, 0, BasisuTranscoderTextureFormat.RGBA32);

//        BufferedImage bufferedImage = fromRgba4444(rgba, imageInfo.width, imageInfo.height);
        BufferedImage bufferedImage = fromRgba8888(rgba, imageInfo.width, imageInfo.height);

        File outFile = new File("output/basis-out0.png");
        ImageIO.write(bufferedImage, "PNG", outFile);
        System.out.println("Decoded image has been written to " + outFile.getAbsolutePath());



        // Byte buffer test.
        {
            ByteBuffer buffer = ByteBuffer.allocateDirect(8);
            for (int i = 0; i < 8; i++) {
                buffer.put((byte) (23 + i));
            }
            buffer.position(0);
            printByteBuffer(buffer, buffer.capacity());
        }
    }

    private static byte[] readToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    // RGBA layout. 2 bytes per component.
    private static BufferedImage fromRgba4444(byte[] rgba, int width, int height) {
        if (rgba.length != width * height * 2) {
            throw new IllegalArgumentException("RGBA image data doesn't match the required size for the specified width & height.");
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            final int rowStartIdx = y * bufferedImage.getWidth() * 2;
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixelIdx = rowStartIdx + x * 2;
                int a = (rgba[pixelIdx + 0] >> 0) & 0x0f; a = a | a << 4;
                int b = (rgba[pixelIdx + 0] >> 4) & 0x0f; b = b | b << 4;
                int g = (rgba[pixelIdx + 1] >> 0) & 0x0f; g = g | g << 4;
                int r = (rgba[pixelIdx + 1] >> 4) & 0x0f; r = r | r << 4;
                int argb = a << 24 | r << 16 | g << 8 | b;
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return bufferedImage;
    }

    // RGBA layout. 4 bytes per component.
    private static BufferedImage fromRgba8888(byte[] rgba, int width, int height) {
        if (rgba.length != width * height * 4) {
            throw new IllegalArgumentException("RGBA image data doesn't match the required size for the specified width & height.");
        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            final int rowStartIdx = y * bufferedImage.getWidth() * 4;
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixelIdx = rowStartIdx + x * 4;
                int r = rgba[pixelIdx + 0] & 0xff;
                int g = rgba[pixelIdx + 1] & 0xff;
                int b = rgba[pixelIdx + 2] & 0xff;
                int a = rgba[pixelIdx + 3] & 0xff;
                int argb = a << 24 | r << 16 | g << 8 | b;
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return bufferedImage;
    }
}
