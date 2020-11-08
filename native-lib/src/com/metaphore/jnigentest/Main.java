package com.metaphore.jnigentest;

import com.badlogic.gdx.jnigen.JniGenSharedLibraryLoader;

public class Main {

    /*JNI

    #include "test.h"

    */

    public static native int add(int a, int b); /*
		return a + b;
	*/

    public static native int testNativeCpp(); /*
		Test test;
		return test.getValue();
	*/

    public static native void testBasisNative(); /*
		testBasis();
	*/

    public static void main(String[] args) {
//        new JniGenSharedLibraryLoader("my-native-lib-natives.jar").load("my-native-lib");
        new JniGenSharedLibraryLoader().load("my-native-lib");
        System.out.println("Add: " + add(1, 2));
        System.out.println("Test class: " + testNativeCpp());
        testBasisNative();
    }
}
