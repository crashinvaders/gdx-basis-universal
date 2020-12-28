package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.Buffer;
import java.nio.IntBuffer;

/**
 * OpenGL functions that are not supported on all the LibGDX backends and thus may be (re)implemented.
 */
public class BasisuGdxGl {

    /**
     * @return the list of the GL texture formats supported by OpenGL on the running platform.
     */
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

    /**
     * Uploads compressed texture data to the OpenGL.
     * This method exists here only because LibGDX doesn't support it for GWT backend
     * and basisu-gdx-gwt provides its own implementation.
     */
    public static void glCompressedTexImage2D(int target, int level, int internalformat,
                                       int width, int height, int border,
                                       int imageSize, Buffer data) {
        Gdx.gl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    }
}
