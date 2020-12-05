package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * OpenGL functions that are not supported on all the LibGDX backends and thus may be reimplemented.
 */
public class BasisuGdxGl {

    public static int[] getSupportedTextureFormats() {
        IntBuffer buffer = BufferUtils.newIntBuffer(64);
        Gdx.gl.glGetIntegerv(GL20.GL_NUM_COMPRESSED_TEXTURE_FORMATS, buffer);
        int formatAmount = buffer.get(0);
        if (buffer.capacity() < formatAmount) {
            buffer = BufferUtils.newIntBuffer(formatAmount);
        }
        int[] result = new int[formatAmount];
        Gdx.gl.glGetIntegerv(GL20.GL_COMPRESSED_TEXTURE_FORMATS, buffer);
        for (int i = 0; i < formatAmount; i++) {
            int code = buffer.get(i);
            result[i] = code;
        }
        return result;
    }

    public static void glCompressedTexImage2D(int target, int level, int internalformat,
                                       int width, int height, int border,
                                       int imageSize, Buffer data) {
        Gdx.gl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }
}
