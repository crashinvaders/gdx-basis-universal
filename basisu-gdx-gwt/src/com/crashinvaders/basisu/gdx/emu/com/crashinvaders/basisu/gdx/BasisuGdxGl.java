package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtGraphics;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.webgl.client.WebGLRenderingContext;

import java.nio.Buffer;
import java.nio.HasArrayBufferView;

/**
 * WebGL compressed texture extension reference.
 * https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Compressed_texture_formats
 */
public class BasisuGdxGl {

    public static int[] getSupportedTextureFormats() {
        return getSupportedTextureFormatsNative(getGlContext());
    }

    private static native int[] getSupportedTextureFormatsNative(WebGLRenderingContext gl) /*-{
            // We need to activate the extensions first.
            gl.getExtension("WEBGL_compressed_texture_astc");
            gl.getExtension("WEBGL_compressed_texture_atc");
            gl.getExtension("WEBGL_compressed_texture_etc");
            gl.getExtension("WEBGL_compressed_texture_etc1");
            gl.getExtension("WEBGL_compressed_texture_pvrtc");
            gl.getExtension("WEBGL_compressed_texture_s3tc");

            // Fetch the supported texture formats.
            var supportedFormats = gl.getParameter(0x86A3); //GL_COMPRESSED_TEXTURE_FORMATS
            var resultValues = new Array(supportedFormats.length);
            for (var i = 0; i < supportedFormats.length; i++) {
                resultValues[i] = supportedFormats[i];
            }
            return resultValues;
		}-*/;

    /**
     * This is a simplified version of JS typed array preparation of GwtGL20#glTexImage2D().
     * It was tested for the GDX Basis Universal code and may not work outside of it.
     */
    public static void glCompressedTexImage2D(int target, int level, int internalFormat,
                                              int width, int height, int border,
                                              int imageSize, Buffer pixels) {

        HasArrayBufferView arrayHolder = (HasArrayBufferView)pixels;
        ArrayBufferView webGLArray = arrayHolder.getTypedArray();
        int remainingBytes = pixels.remaining();
        // Must include pixels.position(): duplicate()'d slices (see TranscodedMipChain) share one
        // backing ArrayBuffer across all mip levels and are only distinguished by position/limit,
        // not by a fresh ArrayBuffer per level - ignoring position here silently re-reads level 0's
        // bytes for every other level. Matches GwtGL20#glTexImage2D's own ByteBuffer handling.
        int byteOffset = webGLArray.byteOffset() + pixels.position();
        ArrayBufferView buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, remainingBytes);

        glCompressedTexImage2DNative(getGlContext(), target, level, internalFormat, width, height, border, imageSize, buffer);
    }

    private static native void glCompressedTexImage2DNative(WebGLRenderingContext gl,
                                                            int target, int level, int internalFormat,
                                                            int width, int height, int border,
                                                            int imageSize, ArrayBufferView pixels) /*-{
        gl.compressedTexImage2D(target, level, internalFormat, width, height, border, pixels);
    }-*/;

    private static WebGLRenderingContext getGlContext() {
        return ((GwtGraphics) Gdx.graphics).getContext();
    }
}
