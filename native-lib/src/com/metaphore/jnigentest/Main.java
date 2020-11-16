package com.metaphore.jnigentest;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;
import java.nio.Buffer;

public class Main {

    /*JNI

    #include <iostream>
    #include <jni.h>

    #include "jni_utils.h"
    #include "basisu_transcoder.h"
    #include "basisu_utils.h"

    */

    public static native int add(int a, int b); /*
		return a + b;
	*/

    public static native boolean validateBasisData(byte[] data, int size); /*
        std::cout << "Validating basis image..." << std::endl;

        return basisuUtils::validateHeader((uint8_t*)data, size);
    */

    public static native byte[] transcodeBasisData(byte[] data, int size); /*
        std::cout << "Transcoding basis image into plain RGBA..." << std::endl;

//        jniUtils::throwException(env, "This is an exception from JNI.");

        std::vector<uint8_t> rgba;
//        basist::transcoder_texture_format format = basist::transcoder_texture_format::cTFRGBA4444;
        basist::transcoder_texture_format format = basist::transcoder_texture_format::cTFRGBA32;

        if (!basisuUtils::transcode(rgba, (uint8_t*)data, size, 0, 0, format)) {
            std::cout << "Error during image transcoding!" << std::endl;
            jniUtils::throwException(env, "Error during image transcoding!");
            return 0;
        }

        jbyteArray byteArray = env->NewByteArray(rgba.size());
        env->SetByteArrayRegion(byteArray, (jsize)0, (jsize)rgba.size(), (jbyte*)rgba.data());
        // env->DeleteLocalRef(byteArray);
        return byteArray;
    */

    public static void main(String[] args) throws IOException {
//        new JniGenSharedLibraryLoader("my-native-lib-natives.jar").load("my-native-lib");
        new JniGenSharedLibraryLoader().load("my-native-lib");
//        System.loadLibrary("windows64/my-native-lib64.dll");
        System.out.println("Add: " + add(1, 2));

        byte[] basisBytes;
//        try (InputStream is = new FileInputStream("/home/metaphore/tmp/cosmocat_promo.basis")) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("kodim3.basis")) {
            basisBytes = readToByteArray(is);
        }

        System.out.println("Bytes read: " + basisBytes.length);

//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
//        byteBuffer.put(bytes, 0, bytes.length);

        if (!validateBasisData(basisBytes, basisBytes.length)) {
            throw new RuntimeException("Failed to validate basis image!");
        }

        byte[] rgba = transcodeBasisData(basisBytes, basisBytes.length);

//        BufferedImage bufferedImage = new BufferedImage(768, 512, BufferedImage.TYPE_INT_ARGB);
//        for (int y = 0; y < bufferedImage.getHeight(); y++) {
//            final int rowStartIdx = y * bufferedImage.getWidth() * 2;
//            for (int x = 0; x < bufferedImage.getWidth(); x++) {
//                int pixelIdx = rowStartIdx + x * 2;
//                int a = (rgba[pixelIdx + 0] >> 0) & 0x0f; a = a | a << 4;
//                int b = (rgba[pixelIdx + 0] >> 4) & 0x0f; b = b | b << 4;
//                int g = (rgba[pixelIdx + 1] >> 0) & 0x0f; g = g | g << 4;
//                int r = (rgba[pixelIdx + 1] >> 4) & 0x0f; r = r | r << 4;
//                int argb = a << 24 | r << 16 | g << 8 | b;
//                bufferedImage.setRGB(x, y, argb);
//            }
//        }

//        BufferedImage bufferedImage = fromRgba4444(rgba, 768, 512);
        BufferedImage bufferedImage = fromRgba8888(rgba, 768, 512);

//        File outFile = new File("/home/metaphore/tmp/basis-out0.png");
        File outFile = new File("D:\\basis-out0.png");
        ImageIO.write(bufferedImage, "PNG", outFile);
        System.out.println("Decoded image has been written to " + outFile.getAbsolutePath());
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
