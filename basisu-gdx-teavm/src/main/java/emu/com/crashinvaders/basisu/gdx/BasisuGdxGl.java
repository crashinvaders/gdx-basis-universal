package emu.com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebGL20;
import com.github.xpenatan.gdx.teavm.backends.web.gl.WebGLRenderingContextExt;
import org.teavm.jso.JSBody;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Int32Array;
import org.teavm.jso.typedarrays.Int8Array;
import org.teavm.jso.typedarrays.Uint8Array;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * WebGL compressed texture extension reference.
 * https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Compressed_texture_formats
 * <p/>
 * Both methods need a TeaVM-specific override:
 * - gdx-teavm's own {@code WebGL20#glGetIntegerv} doesn't handle the
 *   {@code GL_COMPRESSED_TEXTURE_FORMATS} array pname, so it has to be queried directly off the
 *   raw WebGL context.
 * - gdx-teavm's own {@code WebGL20#glCompressedTexImage2D} is an unconditional stub that always
 *   throws "compressed textures not supported by WebGL backend", so this calls the raw WebGL
 *   context's compressedTexImage2D() directly instead.
 */
public class BasisuGdxGl {

    private static final int GL_COMPRESSED_TEXTURE_FORMATS = 0x86A3;

    public static int[] getSupportedTextureFormats() {
        Int32Array formats = getCompressedTextureFormats(getGlContext());
        int[] result = new int[formats.getLength()];
        for (int i = 0; i < result.length; i++) {
            result[i] = formats.get(i);
        }
        return result;
    }

    @JSBody(params = "gl", script = "" +
            "gl.getExtension('WEBGL_compressed_texture_astc');" +
            "gl.getExtension('WEBGL_compressed_texture_atc');" +
            "gl.getExtension('WEBGL_compressed_texture_etc');" +
            "gl.getExtension('WEBGL_compressed_texture_etc1');" +
            "gl.getExtension('WEBGL_compressed_texture_pvrtc');" +
            "gl.getExtension('WEBGL_compressed_texture_s3tc');" +
            "return gl.getParameter(0x86A3);")
    private static native Int32Array getCompressedTextureFormats(WebGLRenderingContextExt gl);

    public static void glCompressedTexImage2D(int target, int level, int internalFormat,
                                               int width, int height, int border,
                                               int imageSize, Buffer data) {
        ArrayBufferView pixels = sliceToArrayBufferView((ByteBuffer) data);
        getGlContext().compressedTexImage2D(target, level, internalFormat, width, height, border, pixels);
    }

    /**
     * Builds a WebGL-consumable view over exactly {@code [position, position + remaining)} of the
     * given buffer's backing storage - must include position(): duplicate()'d slices (see
     * TranscodedMipChain) share one backing array across all mip levels and are only distinguished
     * by position/limit, not by a fresh array per level - ignoring it here would silently re-upload
     * level 0's bytes for every other level.
     */
    private static ArrayBufferView sliceToArrayBufferView(ByteBuffer buffer) {
        int offset;
        ArrayBuffer backing;
        if (buffer.isDirect()) {
            Uint8Array whole = Uint8Array.fromJavaBuffer(buffer);
            backing = whole.getBuffer();
            offset = buffer.position();
        } else {
            Int8Array whole = Int8Array.copyFromJavaArray(buffer.array());
            backing = whole.getBuffer();
            offset = buffer.arrayOffset() + buffer.position();
        }
        return new Uint8Array(backing, offset, buffer.remaining());
    }

    private static WebGLRenderingContextExt getGlContext() {
        return ((WebGL20) Gdx.gl).gl;
    }
}
