package com.crashinvaders.basisu.gdx;

import java.nio.ByteBuffer;

public class BasisuBufferUtils {

    public static boolean isUnsafeByteBuffer(ByteBuffer buffer) {
        return false;
    }

    public static ByteBuffer newUnsafeByteBuffer(int numBytes) {
        throw new UnsupportedOperationException("Unsafe byte buffers are not supported on GWT.");
    }

    public static void disposeUnsafeByteBuffer(ByteBuffer buffer) {
        throw new UnsupportedOperationException("Unsafe byte buffers are not supported on GWT.");
    }
}
