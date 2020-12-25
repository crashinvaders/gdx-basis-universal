package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;

/**
 * Drop in (temporary?) partial replacement for {@link com.badlogic.gdx.utils.BufferUtils}.
 * The problem with the latter is it has no GWT definition/emulation for unsafe byte buffer related methods
 * and thus it's impossible to compile to GWT any code that contain them (even not use).
 */
public class BasisuBufferUtils {

    public static boolean isUnsafeByteBuffer(ByteBuffer buffer) {
        return BufferUtils.isUnsafeByteBuffer(buffer);
    }

    public static ByteBuffer newUnsafeByteBuffer(int numBytes) {
        return BufferUtils.newUnsafeByteBuffer(numBytes);
    }

    public static void disposeUnsafeByteBuffer(ByteBuffer buffer) {
        BufferUtils.disposeUnsafeByteBuffer(buffer);
    }
}
