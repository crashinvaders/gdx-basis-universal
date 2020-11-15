package com.metaphore.jnigentest;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    /*JNI

    #include <iostream>
//    #include <stdint.h>

    #include "basisu_utils.h"

    */

    public static native int add(int a, int b); /*
		return a + b;
	*/

    public static native boolean validateBasisData(byte[] data, int size); /*
        std::cout << "Boom!" << std::endl;

        uint8_t* basisData = (uint8_t*) data;
        return basisuUtils::validateHeader(basisData, size);
    */

    public static void main(String[] args) throws IOException {
//        new JniGenSharedLibraryLoader("my-native-lib-natives.jar").load("my-native-lib");
        new JniGenSharedLibraryLoader().load("my-native-lib");
        System.out.println("Add: " + add(1, 2));


        InputStream is = Main.class.getClassLoader().getResourceAsStream("kodim3.basis");
        if (is == null) {
            throw new RuntimeException("Cannot load the resource image!");
        }
        byte[] bytes = readToByteArray(is);

        System.out.println("Bytes read: " + bytes.length);

//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
//        byteBuffer.put(bytes, 0, bytes.length);

        System.out.println(validateBasisData(bytes, bytes.length) ? "Success!!!" : "FAILURE :(");
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
}
